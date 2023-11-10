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
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("SerializationErrorCatchInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
                                                          // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
                                                          // and make this function return null
            .Build();

        var badWriter = KafkaWriterBuilder.Create<string, BadEvent>(_configuration)
            .WithTopic("SerializationErrorCatchInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
            // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
            // and make this function return null
            .Build();
        
        var reader = KafkaReaderBuilder.Create<string, OutputEvent>(_configuration)
            .WithTopic("SerializationErrorCatchOutputTopic")     // The topic being published to from the job
            .WithConsumerGroup("MyGroup")           // The consumer group used for reading from the topic
            .Build();
        
        var metricsReader = new PrometheusMetricsReader("serializationerrorcatch-taskmanager", 9249);
        
        // Act
        // Write one or more messages to the writer
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        var badInputEvent = new BadEvent()
        {
            DeviceId = "deviceId-1",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };
        
        await writer.WriteAsync(inputEvent);
        await badWriter.WriteAsync(badInputEvent);
        
        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(20));
        
        var gauge = await metricsReader.GetCounterValueAsync("FailedMessagesProcessed");
        Assert.Equal(1, gauge);
        
        // Assert 
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.ExtraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}