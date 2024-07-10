using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using ExternalLookup.ComponentTest.Models;
using Confluent.Kafka;

namespace ExternalLookup.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task External_Lookup_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        // Create a KafkaTestWriter to write messages and a KafkaTestReader to read messages
        var writer = kafkaClientFactory.CreateTestWriter<string, InputEvent>("ExternalLookupInputTopic", model => model.DeviceId);
        var reader = kafkaClientFactory.CreateTestReader<Null, OutputEvent>("ExternalLookupOutputTopic", "MyGroup", keyDeserializer: Deserializers.Null);
        
        // Act
        // Create Input event and publish it to Kafka
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        await writer.WriteAsync(inputEvent);
        
        // Assert
        // Verify one message was written to Kafka and that the message is the same as the input event with an additional field
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        messages.Should().ContainSingle(message => 
            message.Value.DeviceId == inputEvent.DeviceId && 
            message.Value.Value == inputEvent.Value &&
            message.Value.Timestamp == inputEvent.Timestamp &&
            message.Value.ExtraField == "External-lookup");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}