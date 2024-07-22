using System;
using System.Collections.Generic;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using TumblingWindow.ComponentTest.Models;
using System.Linq;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using Confluent.Kafka;

namespace TumblingWindow.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact] 
    public async Task Tumbling_Window_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("TumblingWindowInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<EventWindow>("TumblingWindowOutputTopic");
        
        // Act
        // Write messages to the writer
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

        var message1 = new Message<Null, InputEvent>()
        {
            Value = inputEvent1
        };
        var message2 = new Message<Null, InputEvent>()
        {
            Value = inputEvent2
        };
        var message3 = new Message<Null, InputEvent>()
        {
            Value = inputEvent3
        };
        var message4 = new Message<Null, InputEvent>()
        {
            Value = inputEvent4
        };
        
        await writer.WriteAsync(message1, message2, message3);
        await writer.WriteAsync(message4, message4);
        
        // Assert
        // Read messages from the reader
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        // Evaluate the messages 
        messages.Should().ContainSingle(message => 
            message.Value.DeviceId == inputEvent1.DeviceId &&
            message.Value.Values.Count == 3 &&
            message.Value.Values.All(item => new double[]{inputEvent1.Value,
                    inputEvent2.Value,
                    inputEvent3.Value}
                .Contains(item)));
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }


}