using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using MultipleSideOutputExample.ComponentTest.Models;
using Confluent.Kafka;

namespace MultipleSideOutputExample.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Multiple_Side_Output_Component_Test(){
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("MultipleSideOutputExampleInputTopic");
        var readerTopicA = kafkaClientFactory.CreateTestReader<OutputEvent>("OutputA-events");
        var readerTopicB = kafkaClientFactory.CreateTestReader<OutputEvent>("OutputB-events");
        var readerTopicCD = kafkaClientFactory.CreateTestReader<OutputEvent2>("OutputCD-events");
        
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