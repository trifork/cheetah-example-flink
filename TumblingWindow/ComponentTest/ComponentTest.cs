using System;
using System.Collections.Generic;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using TumblingWindow.ComponentTest.Models;
using System.Linq;
using System.Threading.Tasks;

namespace TumblingWindow.ComponentTest;

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
    public async Task Tumbling_Window_Component_Test()
    {
        // Arrange
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("TumblingWindowInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
                                                          // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
                                                          // and make this function return null
            .Build();

        var reader = KafkaReaderBuilder.Create<string, Models.EventWindow>(_configuration)
            .WithTopic("TumblingWindowOutputTopic")
            .WithConsumerGroup("MyGroup")
            .Build();
        
        // Act
        // Write one or more messages to the writer
        var inputEvent1 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        var inputEvent2 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        var inputEvent3 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 910.1112,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };

        var inputEvent4 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 910.1112,
            Timestamp = DateTimeOffset.Now.AddMinutes(10).ToUnixTimeMilliseconds()
        };

        
        await writer.WriteAsync(inputEvent1, inputEvent2, inputEvent3);

        await writer.WriteAsync(inputEvent4, inputEvent4);

        
        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent1.DeviceId &&
            message.Values.Count == 3 &&
            message.Values.All(item => new double[]{inputEvent1.Value,
                                                    inputEvent2.Value,
                                                    inputEvent3.Value}
                                                    .Contains(item)));
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }


}