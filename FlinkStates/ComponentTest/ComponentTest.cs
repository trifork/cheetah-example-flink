using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using FlinkStates.ComponentTest.Models;

namespace FlinkStates.ComponentTest;

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
            .WithTopic("FlinkStatesInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
                                                          // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
                                                          // and make this function return null
            .Build();

        var valueReader = KafkaReaderBuilder.Create<string, double>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-value")
            .WithConsumerGroup("MyGroup")
            .Build();
        var reducingReader = KafkaReaderBuilder.Create<string, double>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-reducing")
            .WithConsumerGroup("MyGroup")
            .Build();
        var aggregatingReader = KafkaReaderBuilder.Create<string, double>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-aggregating")
            .WithConsumerGroup("MyGroup")
            .Build();
        var listReader = KafkaReaderBuilder.Create<string, double[]>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-list")
            .WithConsumerGroup("MyGroup")
            .Build();
        var mapReader = KafkaReaderBuilder.Create<string, double>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-map")
            .WithConsumerGroup("MyGroup")
            .Build();

        // Act
        // Write one or more messages to the writer
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        var inputEvent2 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        await writer.WriteAsync(inputEvent);
        await writer.WriteAsync(inputEvent2);

        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        await Task.Delay(TimeSpan.FromSeconds(20));
        var valueMessages = valueReader.ReadMessages(1, TimeSpan.FromSeconds(1));
        var reducingMessages = reducingReader.ReadMessages(2, TimeSpan.FromSeconds(1));
        var aggregatingMessages = aggregatingReader.ReadMessages(2, TimeSpan.FromSeconds(1));
        var listMessages = listReader.ReadMessages(1, TimeSpan.FromSeconds(1));
        var mapMessages = mapReader.ReadMessages(2, TimeSpan.FromSeconds(1));

        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        valueMessages.Should().ContainSingle(message => message == 34.56);

        reducingMessages.Should().ContainSingle(message => message == 12.34);
        reducingMessages.Should().ContainSingle(message => message == 69.12);

        aggregatingMessages.Should().ContainSingle(message => message == 12.34);
        aggregatingMessages.Should().ContainSingle(message => message == 69.12);

        listMessages.Should().ContainSingle(message => message.Length == 2 && message[0] == 12.34 && message[1] == 56.78);

        mapMessages.Should().ContainSingle(message => message == 12.34);
        mapMessages.Should().ContainSingle(message => message == 69.12);
        
        valueReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        reducingReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        aggregatingReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        listReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        mapReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
    }
}