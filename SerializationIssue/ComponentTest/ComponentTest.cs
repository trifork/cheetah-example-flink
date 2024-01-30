using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using Cheetah.Kafka.Testing;
using SerializationIssue.ComponentTest.Models;

namespace SerializationIssue.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Set up the configuration
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Arrange
        // Set up clients, writers, and readers as needed
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("SerializationIssueInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<OutputEvent>("SerializationIssueOutputTopic");
        
        // Act
        // Use the clients/writers/readers that you created
        var inputEvent = new InputEvent("deviceId-1", 12.34, DateTimeOffset.Now.ToUnixTimeMilliseconds(), new List<string>(){"hej", "med", "dig"});
        await writer.WriteAsync(inputEvent);
        
        // Assert
        // Use the client/writers/readers to assert some properties that your job should comform to
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(10));
        
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.List.Count == inputEvent.List.Count &&
            message.ExtraField == "ExtraFieldValue");
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(10)).Should().BeTrue();

        // Assert.Fail("This is really just here to make the test fail and ensure that you either decide to implement a component test or actively decide not to");
    }
}