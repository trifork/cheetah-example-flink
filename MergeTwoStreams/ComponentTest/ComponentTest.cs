using System;
using System.Collections.Generic;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using MergeTwoStreams.ComponentTest.Models;
using System.Threading;

namespace MergeTwoStreams.ComponentTest;

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
    public void Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var writerA = KafkaWriterBuilder.Create<string, MergeTwoStreamsInputEvent>(_configuration)
            .WithTopic("MergeTwoStreamsInputTopicA") // The topic to consume from (Stream A) in the job
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
            .Build();

        var writerB = KafkaWriterBuilder.Create<string, MergeTwoStreamsInputEvent>(_configuration)
            .WithTopic("MergeTwoStreamsInputTopicB") // The topic to consume from (Stream B) in the job
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
            .Build();

        var reader = KafkaReaderBuilder.Create<string, MergeTwoStreamsOutputEvent>(_configuration)
            .WithTopic("MergeTwoStreamsOutputTopic")    // The topic being published to from the job
            .WithConsumerGroup("MyGroup")                     // The consumer group used for reading from the topic
            .Build();

        // Act
        // Write two messages with different deviceIds to stream A
        var inputEventA = new MergeTwoStreamsInputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        var inputEventD = new MergeTwoStreamsInputEvent()
        {
            DeviceId = "deviceId-2",
            Value = 90.12,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        writerA.WriteAsync(inputEventA);
        writerA.WriteAsync(inputEventD);

        // Wait to make sure the elements on stream A have been processed before writing to stream B
        Thread.Sleep(500);

        // Write two messages to stream B - one with a deviceIds which has been processed on stream A, and one which hasn't. Resulting in one message on the output topic
        var inputEventC = new MergeTwoStreamsInputEvent()
        {
            DeviceId = "deviceId-3",
            Value = 32.10,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        var inputEventB = new MergeTwoStreamsInputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        writerB.WriteAsync(inputEventC);
        writerB.WriteAsync(inputEventB);

        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(5));
        
        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEventA.DeviceId &&
            message.ValueA == inputEventA.Value &&
            message.ValueB == inputEventB.Value);
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();

    }
}