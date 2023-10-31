using System;
using System.Collections.Generic;
using System.Threading.Tasks;
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
    public async Task Multiple_Side_Output_Component_Test(){
        // Arrange
        // Setting up the writer (Kafka producer), not using a message key.
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("MultipleSideOutputExampleInputTopic")
            .WithKeyFunction(model => model.DeviceId)
            .Build();

        // Setting up readers (Kafka consumer)
        var readerTopicA = KafkaReaderBuilder.Create<string, OutputEvent>(_configuration)
            .WithTopic("OutputA-events")
            .WithConsumerGroup("ComponentTest")
            .Build();

        var readerTopicB = KafkaReaderBuilder.Create<string, OutputEvent>(_configuration)
            .WithTopic("OutputB-events")
            .WithConsumerGroup("ComponentTest")
            .Build();

        var readerTopicCD = KafkaReaderBuilder.Create<string, OutputEvent2>(_configuration)
            .WithTopic("OutputCD-events")
            .WithConsumerGroup("ComponentTest")
            .Build();

        // Act
        // Making an input event
        var inputEvent = new InputEvent(){
            DeviceId = "ComponentTest",
            ValueA = 100,
            ValueB = 100,
            ValueC = 100,
            ValueD = 100,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };

        await writer.WriteAsync(inputEvent);
        
        // Assert
        // Assuming the job produce the message on each topic
        await Task.Delay(TimeSpan.FromSeconds(20));
        var messagesTopicA = readerTopicA.ReadMessages(1, TimeSpan.FromSeconds(1));
        var messagesTopicB = readerTopicB.ReadMessages(1, TimeSpan.FromSeconds(1));
        var messagesTopicCd = readerTopicCD.ReadMessages(1, TimeSpan.FromSeconds(1));

        messagesTopicA.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.Value == inputEvent.ValueA &&
            message.Timestamp == inputEvent.Timestamp
        );
        readerTopicA.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();

        messagesTopicB.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.Value == inputEvent.ValueB &&
            message.Timestamp == inputEvent.Timestamp
        );
        readerTopicB.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();

        messagesTopicCd.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.ValueC == inputEvent.ValueC &&
            message.ValueD == inputEvent.ValueD &&
            message.Timestamp == inputEvent.Timestamp
        );
        readerTopicCD.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();
    }
}