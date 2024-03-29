using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using FlinkStates.ComponentTest.Models;

namespace FlinkStates.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Flink_States_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        // Create a KafkaTestWriter to write messages to the topic "FlinkStatesInputTopic"
        var writer =
            kafkaClientFactory.CreateTestWriter<string, InputEvent>("FlinkStatesInputTopic", model => model.DeviceId);
        
        // Create KafkaTestReaders to read messages from the topics "FlinkStatesOutputTopic-value"
        var valueReader = kafkaClientFactory.CreateTestReader<string, double>("FlinkStatesOutputTopic-value", "MyGroup");
        var reducingReader = kafkaClientFactory.CreateTestReader<string, double>("FlinkStatesOutputTopic-reducing", "MyGroup");
        var aggregatingReader = kafkaClientFactory.CreateTestReader<string, double>("FlinkStatesOutputTopic-aggregating", "MyGroup");
        var listReader = kafkaClientFactory.CreateTestReader<string, double[]>("FlinkStatesOutputTopic-list", "MyGroup");
        var mapReader = kafkaClientFactory.CreateTestReader<string, double>("FlinkStatesOutputTopic-map", "MyGroup");
        
        // Act
        // Create two different input events
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
        await Task.Delay(TimeSpan.FromSeconds(20));
        var valueMessages = valueReader.ReadMessages(1, TimeSpan.FromSeconds(1));
        var reducingMessages = reducingReader.ReadMessages(2, TimeSpan.FromSeconds(1));
        var aggregatingMessages = aggregatingReader.ReadMessages(2, TimeSpan.FromSeconds(1));
        var listMessages = listReader.ReadMessages(1, TimeSpan.FromSeconds(1));
        var mapMessages = mapReader.ReadMessages(2, TimeSpan.FromSeconds(1));

        // Evaluate the results
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