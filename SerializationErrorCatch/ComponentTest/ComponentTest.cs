using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using Cheetah.MetricsTesting.PrometheusMetrics;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using SerializationErrorCatch.ComponentTest.Models;
using Confluent.Kafka;

namespace SerializationErrorCatch.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task SerializationErrorCatchJob_ComponentTest()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        // Create a PrometheusMetricsReader
        var metricsReader = new PrometheusMetricsReader("serializationerrorcatch-taskmanager", 9249);
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("SerializationErrorCatchInputTopic");
        var badWriter = kafkaClientFactory.CreateTestWriter<BadEvent>("SerializationErrorCatchInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<OutputEvent>("SerializationErrorCatchOutputTopic");
        
        // Act
        // Create an InputEvent
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        // Create a BadEvent
        var badInputEvent = new BadEvent()
        {
            DeviceId = "deviceId-1",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };

        var message = new Message<Null, InputEvent>()
        {
            Value = inputEvent
        };
        var badMessage = new Message<Null, BadEvent>()
        {
            Value = badInputEvent
        };
        
        // Write the InputEvent and BadEvent to the SerializationErrorCatchInputTopic topic
        await badWriter.WriteAsync(badMessage);
        await writer.WriteAsync(message);
        await badWriter.WriteAsync(badMessage);
        await badWriter.WriteAsync(badMessage);
        
        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(5));

        // Assert
        // Assert metric failed_messages_processed is 3
        var gauge = await metricsReader.GetCounterValueAsync("FailedMessagesProcessed");
        Assert.Equal(3, gauge);
        
        // Assert 1 message was written to the SerializationErrorCatchOutputTopic topic
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.Value.DeviceId == inputEvent.DeviceId && 
            message.Value.Value == inputEvent.Value &&
            message.Value.Timestamp == inputEvent.Timestamp &&
            message.Value.ExtraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}