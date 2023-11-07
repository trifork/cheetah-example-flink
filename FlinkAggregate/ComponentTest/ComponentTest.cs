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
    public async Task Flink_States_Component_Test()
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

        var aggregatingReader = KafkaReaderBuilder.Create<string, double>(_configuration)
            .WithTopic("FlinkStatesOutputTopic-aggregating")
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

        var inputEvent3 = new InputEvent()
        {
             DeviceId = "deviceId-1",
             Value = 99.99,
             Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };



        await writer.WriteAsync(inputEvent);
        await writer.WriteAsync(inputEvent2);
        await writer.WriteAsync(inputEvent3);

        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        await Task.Delay(TimeSpan.FromSeconds(20));
        var aggregatingMessages = aggregatingReader.ReadMessages(3, TimeSpan.FromSeconds(1));

        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        aggregatingMessages.Should().ContainSingle(message => message == 12.34);
        aggregatingMessages.Should().ContainSingle(message => message == 69.12);
        aggregatingMessages.Should().ContainSingle(message => message == 169.11);

        aggregatingReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
    }
}