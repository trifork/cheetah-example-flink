using System;
using System.Collections.Generic;
using cheetah.example.model.avrorecord;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using jsonToAvro.ComponentTest.Models;
using Cheetah.SchemaRegistry.Testing;

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
        var avroKafkaClientFactory = AvroKafkaTestClientFactory.Create(configuration);
        
        // Create writer and reader
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("jsonToAvroInputTopic");
        var reader = avroKafkaClientFactory.CreateTestReader<OutputEventAvro>("jsonToAvroOutputTopic");
        
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
            message.Value.deviceId == inputEvent.DeviceId && 
            message.Value.value == inputEvent.Value &&
            message.Value.timestamp == inputEvent.Timestamp);
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}