using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using Cheetah.MetricsTesting.PrometheusMetrics;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using SerializationErrorCatch.ComponentTest.Models;

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
        var badReader = kafkaClientFactory.CreateTestReader<BadEvent>("SerializationErrorCatchOutputTopicUnParsed");
        
        // Act
        // Create an InputEvent
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        // Create a BadEvent
        var badInputEvent1 = new BadEvent()
        {
            DeviceId = "deviceId-1",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };        // Create a BadEvent
        var badInputEvent2 = new BadEvent()
        {
            DeviceId = "deviceId-2",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };        // Create a BadEvent
        var badInputEvent3 = new BadEvent()
        {
            DeviceId = "deviceId-3",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };
        
        // Write the InputEvent and BadEvent to the SerializationErrorCatchInputTopic topic
        await badWriter.WriteAsync(badInputEvent1);
        await writer.WriteAsync(inputEvent);
        await badWriter.WriteAsync(badInputEvent2);
        await badWriter.WriteAsync(badInputEvent3);
        
        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(5));

        // Assert
        // Assert metric failed_messages_processed is 3
       var gauge = await metricsReader.GetCounterValueAsync("un_parsed_events");
       Assert.Equal(3, gauge);
        
        // Assert 1 message was written to the SerializationErrorCatchOutputTopic topic
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(5));
        var badMessages = badReader.ReadMessages(3, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.ExtraField == "ExtraFieldValue");

        badMessages.Should().ContainSingle(message =>
            message.DeviceId == badInputEvent1.DeviceId &&
            message.Value == badInputEvent1.Value &&
            message.Timestamp == badInputEvent1.Timestamp &&
            message.BadField == badInputEvent1.BadField);

        badMessages.Should().ContainSingle(message =>
            message.DeviceId == badInputEvent2.DeviceId &&
            message.Value == badInputEvent2.Value &&
            message.Timestamp == badInputEvent2.Timestamp &&
            message.BadField == badInputEvent2.BadField);

        badMessages.Should().Contain(message =>
            message.DeviceId == badInputEvent3.DeviceId &&
            message.Value == badInputEvent3.Value &&
            message.Timestamp == badInputEvent3.Timestamp &&
            message.BadField == badInputEvent3.BadField);
            
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
        badReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}