using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Observability.ComponentTest.PrometheusMetrics;
using Xunit;
using SerializationErrorCatch.ComponentTest.Models;

namespace SerializationErrorCatch.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    readonly IConfiguration _configuration;

    public ComponentTest()
    {
        // These will be overriden by environment variables from compose
        var conf = new Dictionary<string, string>()
        {
            {"KAFKA:AUTHENDPOINT", "http://localhost:1752/oauth2/token"},
            {"KAFKA:CLIENTID", "ClientId" },
            {"KAFKA:CLIENTSECRET", "1234" },
            {"KAFKA:URL", "localhost:9092"}
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact]
    public async Task SerializationErrorCatchJob_ComponentTest()
    {
        // Arrange
        // writer is used to write messages to the SerializationErrorCatchInputTopic topic. 
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("SerializationErrorCatchInputTopic")
            .WithKeyFunction(model => model.DeviceId) 
            .Build();

        // badWriter is used to write messages to the SerializationErrorCatchInputTopic topic to trigger deserialization error.
        var badWriter = KafkaWriterBuilder.Create<string, BadEvent>(_configuration)
            .WithTopic("SerializationErrorCatchInputTopic")
            .WithKeyFunction(model => model.DeviceId) 
            .Build();
        
        // reader is used to read messages from the SerializationErrorCatchOutputTopic topic.
        var reader = KafkaReaderBuilder.Create<string, OutputEvent>(_configuration)
            .WithTopic("SerializationErrorCatchOutputTopic")     // The topic being published to from the job
            .WithConsumerGroup("MyGroup")           // The consumer group used for reading from the topic
            .Build();
        
        // metricsReader is used to read metrics from the job
        var metricsReader = new PrometheusMetricsReader("serializationerrorcatch-taskmanager", 9249);
        
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
        
        // Write the InputEvent and BadEvent to the SerializationErrorCatchInputTopic topic
        await badWriter.WriteAsync(badInputEvent);
        await writer.WriteAsync(inputEvent);
        await badWriter.WriteAsync(badInputEvent);
        await badWriter.WriteAsync(badInputEvent);
        
        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(5));
        
        // Assert that the FailedMessagesProcessed metric is 3
        var gauge = await metricsReader.GetCounterValueAsync("FailedMessagesProcessed");
        Assert.Equal(3, gauge);
        
        // Assert 1 message was written to the SerializationErrorCatchOutputTopic topic
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.ExtraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}