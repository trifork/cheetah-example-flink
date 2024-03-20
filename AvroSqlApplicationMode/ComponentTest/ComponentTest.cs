using System;
using System.Threading.Tasks;
using System.Collections.Generic;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using Cheetah.Kafka.Testing;
using AvroSqlApplicationMode.ComponentTest.Models;

namespace AvroSqlApplicationMode.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();

        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);

        // Create writer and reader using a key as "timestamp"
        //var writer = kafkaClientFactory.CreateAvroTestWriter<string, InputEventAvro>("avroInputTopic", item => item.timestamp.ToString());

        // Create writer and reader with no key
        var writer = kafkaClientFactory.CreateAvroTestWriter<InputEventAvro>("avroInputTopic");

        var reader = kafkaClientFactory.CreateAvroTestReader<InputEventAvro>("sqlSinkTopic");

        // Act
        // Create InputEvent
        var inputEvent = new InputEventAvro()
        {
            deviceId = "deviceId-1",
            value = 12.34,
            timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds(),
            extraField = "ExtraFieldValue"
        };

        await writer.WriteAsync(inputEvent);

        await Task.Delay(1000);

        inputEvent = new InputEventAvro()
        {
            deviceId = "deviceId-1",
            value = 12.34,
            timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds(),
            extraField = "ExtraFieldValue"
        };

        await writer.WriteAsync(inputEvent);

        await Task.Delay(1000);

        inputEvent = new InputEventAvro()
        {
            deviceId = "deviceId-2",
            value = 12.34,
            timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds(),
            extraField = "ExtraFieldValue"
        };

        await writer.WriteAsync(inputEvent);

        // Assert
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(30));

        // Test if there is a message
        messages.Should().HaveCountGreaterThan(0);
    }
}