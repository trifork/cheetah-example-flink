using System;
using System.Collections.Generic;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using MultipleSideOutputExample.ComponentTest.Models;
using Confluent.Kafka;

namespace MultipleSideOutputExample.ComponentTest;

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
    public void TestFirstMessageOnAllTopics(){
        // Arrange
        // Setting up the writer (Kafka producer), not using a message key.
        var writer = KafkaWriterBuilder.Create<string, MultipleSideOutputExampleInputEvent>(_configuration)
            .WithTopic("MultipleSideOutputExampleInputTopic")
            .WithKeyFunction(model => model.DeviceId)
            .Build();

        // Setting up readers (Kafka consumer)
        var reader_topicA = KafkaReaderBuilder.Create<string, MultipleSideOutputExampleOutputEvent>(_configuration)
            .WithTopic("OutputA-events")
            .WithGroupId("ComponentTest")
            .Build();

        var reader_topicB = KafkaReaderBuilder.Create<string, MultipleSideOutputExampleOutputEvent>(_configuration)
            .WithTopic("OutputB-events")
            .WithGroupId("ComponentTest")
            .Build();

        var reader_topicCD = KafkaReaderBuilder.Create<string, MultipleSideOutputExampleOutputEvent2>(_configuration)
            .WithTopic("OutputCD-events")
            .WithGroupId("ComponentTest")
            .Build();

        // Act
        // Making an input event
        var inputEvent = new MultipleSideOutputExampleInputEvent(){
            DeviceId = "ComponentTest",
            ValueA = 100,
            ValueB = 100,
            ValueC = 100,
            ValueD = 100,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };

        writer.Write(inputEvent);

        // Assert
        // Assuming the job produce the message on each topic
        var messages_topicA = reader_topicA.ReadMessages(1, TimeSpan.FromSeconds(20));
        var messages_topicB = reader_topicB.ReadMessages(1, TimeSpan.FromSeconds(20));
        var messages_topicCD = reader_topicCD.ReadMessages(1, TimeSpan.FromSeconds(20));

        messages_topicA.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.Value == inputEvent.ValueA &&
            message.Timestamp == inputEvent.Timestamp
        );
        reader_topicA.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();

        messages_topicB.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.Value == inputEvent.ValueB &&
            message.Timestamp == inputEvent.Timestamp
        );
        reader_topicB.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();

        messages_topicCD.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.ValueC == inputEvent.ValueC &&
            message.ValueD == inputEvent.ValueD &&
            message.Timestamp == inputEvent.Timestamp
        );
        reader_topicCD.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
    
}