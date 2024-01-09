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
    readonly IConfiguration _configuration;

    public ComponentTest()
    {
        // These will be overriden by environment variables from compose
        var conf = new Dictionary<string, string>()
        {
            { "KAFKA:URL", "localhost:9092" },
            { "KAFKA:OAUTH2:CLIENTID", "default-access" },
            { "KAFKA:OAUTH2:CLIENTSECRET", "default-access-secret" },
            { "KAFKA:OAUTH2:SCOPE", "kafka schema-registry" },
            { "KAFKA:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token" },
            { "KAFKA:SCHEMAREGISTRYURL", "http://localhost:8081/apis/ccompat/v7" }
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact]
    public void Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(_configuration);
        
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