using System;
using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using Cheetah.Kafka.Testing;
using KeySerializationSchema.ComponentTest.Models;
using Confluent.Kafka;

namespace KeySerializationSchema.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Setup configuration
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Arrange
        // Set up clients, writers, and readers
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("KeySerializationSchemaInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<string, OutputEvent>("KeySerializationSchemaOutputTopic", keyDeserializer: Deserializers.Utf8);
        
        // Act
        // Send two messages to the input topic
        var inputEventKey1 = new InputEvent("deviceId-1", 12.34, DateTimeOffset.Now.ToUnixTimeMilliseconds(), "key1");
        var inputEventKey2 = new InputEvent("deviceId-2", 19.53, DateTimeOffset.Now.ToUnixTimeMilliseconds(), "key2");
        await writer.WriteAsync(inputEventKey1);
        await writer.WriteAsync(inputEventKey2);
        
        // Assert
        // Assert the messages are received on the output topic
        var messages = reader.ReadMessages(2, TimeSpan.FromSeconds(10));
        
        messages.Should().Contain(message => 
            message.Value.DeviceId == inputEventKey1.DeviceId && 
            message.Value.Value == inputEventKey1.Value &&
            message.Value.Timestamp == inputEventKey1.Timestamp &&
            message.Value.Keys == inputEventKey1.Keys &&
            message.Value.ExtraField == "ExtraFieldValue" &&
            message.Key == inputEventKey1.Keys);
        
        messages.Should().Contain(message =>
            message.Value.DeviceId == inputEventKey2.DeviceId && 
            message.Value.Value == inputEventKey2.Value &&
            message.Value.Timestamp == inputEventKey2.Timestamp &&
            message.Value.Keys == inputEventKey2.Keys &&
            message.Value.ExtraField == "ExtraFieldValue" &&
            message.Key == inputEventKey2.Keys);
        
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(10)).Should().BeTrue();
    }
}