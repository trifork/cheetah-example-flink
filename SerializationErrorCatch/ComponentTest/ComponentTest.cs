using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using SerializationErrorCatch.ComponentTest.Models;

namespace SerializationErrorCatch.ComponentTest;

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
            { "KAFKA:OAUTH2:SCOPE", "kafka" },
            { "KAFKA:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token" }
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact]
    public async Task SerializationErrorCatchJob_ComponentTest()
    {
        // Arrange
        var kafkaClientFactory = KafkaTestClientFactory.Create(_configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("SerializationErrorCatchInputTopic");
        var badWriter = kafkaClientFactory.CreateTestWriter<BadEvent>("SerializationErrorCatchInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<OutputEvent>("SerializationErrorCatchOutputTopic");
        
        // Act
        // Act
        // Create an InputEvent
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        // Create a BadEvent
        var badInputEvent = new BadEvent()
        {
            DeviceId = "deviceId-1",
            Value = 111.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds(),
            BadField = "BadFieldValue"
        };
        
        // Write the InputEvent and BadEvent to the SerializationErrorCatchInputTopic topic
        await badWriter.WriteAsync(badInputEvent);
        await writer.WriteAsync(inputEvent);
        await badWriter.WriteAsync(badInputEvent);
        await badWriter.WriteAsync(badInputEvent);
        
        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(5));
        
        // Assert 1 message was written to the SerializationErrorCatchOutputTopic topic
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.ExtraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }
}