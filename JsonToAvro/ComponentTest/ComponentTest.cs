using System;
using System.Collections.Generic;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using jsonToAvro.ComponentTest.Models;

namespace jsonToAvro.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public void Json_To_Avro_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        // Create writer and reader
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("jsonToAvroInputTopic");
        var reader = kafkaClientFactory.CreateAvroTestReader<OutputEventAvro>("jsonToAvroOutputTopic");
        
        // Act
        // Create InputEvent
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        writer.WriteAsync(inputEvent);
        
        // Assert
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(10));
        
        messages.Should().ContainSingle(message => 
            message.deviceId == inputEvent.DeviceId && 
            message.value == inputEvent.Value &&
            message.timestamp == inputEvent.Timestamp &&
            message.extraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}