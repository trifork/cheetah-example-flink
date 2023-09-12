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

    //[Fact]
    public void Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var writer = KafkaWriterBuilder.Create<string, MultipleSideOutputExampleInputEvent>(_configuration)
            .WithTopic("MultipleSideOutputExampleInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
                                                          // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
                                                          // and make this function return null
            .Build();

        var reader = KafkaReaderBuilder.Create<string, MultipleSideOutputExampleOutputEvent>(_configuration)
            .WithTopic("MultipleSideOutputExampleOutputTopic")
            .WithGroupId("MyGroup")
            .Build();
        
        // Act
        // Write one or more messages to the writer
        var inputEvent = new MultipleSideOutputExampleInputEvent()
        {
            DeviceId = "deviceId-1",
            ValueA = 12.34,
            ValueB = 22.34,
            ValueC = 32.34,
            ValueD = 42.34,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        
        writer.Write(inputEvent);
        
        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.ValueA &&
            message.Timestamp == inputEvent.Timestamp);
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();

        Assert.True(false, "This is really just here to make the test fail and ensure that you either decide to implement a component test or actively decide not to");
    }

    [Fact]
    public void Test_First_Message_On_All_Topics(){
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
            DeviceId = "Test_First_Message_On_All_Topics",
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

        messages_topicB.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.Value == inputEvent.ValueB &&
            message.Timestamp == inputEvent.Timestamp
        );

        messages_topicCD.Should().ContainSingle(message =>
            message.DeviceId == inputEvent.DeviceId &&
            message.ValueC == inputEvent.ValueC &&
            message.ValueD == inputEvent.ValueD &&
            message.Timestamp == inputEvent.Timestamp
        );
    }
}