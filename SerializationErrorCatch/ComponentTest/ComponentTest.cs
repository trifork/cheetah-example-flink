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
    [Fact]
    public async Task SerializationErrorCatchJob_ComponentTest()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("SerializationErrorCatchInputTopic");
        var badWriter = kafkaClientFactory.CreateTestWriter<BadEvent>("SerializationErrorCatchInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<OutputEvent>("SerializationErrorCatchOutputTopic");
        
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