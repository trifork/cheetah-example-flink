using System;
using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using Cheetah.Kafka.Testing;
using AvroToJson.ComponentTest.Models;
using Cheetah.SchemaRegistry.Testing;
using Cheetah.Kafka;
using Confluent.Kafka;

namespace AvroToJson.ComponentTest;

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
        // Here you can set up clients, writers, and readers as needed
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        var avroKafkaClientFactory = AvroKafkaTestClientFactory.Create(configuration);
        var writer = avroKafkaClientFactory.CreateTestWriter<InputEventAvro>("AvroToJsonInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<Null, OutputEventJson>("AvroToJsonOutputTopic", keyDeserializer: Deserializers.Null);
        
        // Act
        // Use the clients/writers/readers that you created
        var inputEvent = new InputEventAvro()
        {
            deviceId = "deviceId-1",
            value = 12.34,
            timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        await writer.WriteAsync(inputEvent);
        
        // Assert
        // Use the client/writers/readers to assert some properties that your job should comform to
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(10));

        messages.Should().ContainSingle(message => 
            message.Value.DeviceId == inputEvent.deviceId && 
            message.Value.Value == inputEvent.value &&
            message.Value.Timestamp == inputEvent.timestamp);
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(10)).Should().BeTrue();
    }
}