using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using Cheetah.Kafka.Testing;
using fepa.ComponentTest.Models;

namespace fepa.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{

    readonly IConfiguration _configuration;

    public ComponentTest()
    {
        // These will be overriden by environment variables from compose
        var conf = new Dictionary<string, string>()
        {
            {"KAFKA:AUTHENDPOINT", "http://localhost:1752/oauth2/token"},
            {"KAFKA:CLIENTID", "ClientId" },
            {"KAFKA:CLIENTSECRET", "1234" },
            {"KAFKA:URL", "localhost:9092"}
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Setup configuration
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();

        // Arrange
        // Set up clients, writers, and readers as needed
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("fepaInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<OutputEvent>("fepaOutputTopic");
        
        // Act
        // Use the clients/writers/readers that you created
        var inputEvent = new InputEvent("deviceId-1", 12.34, DateTimeOffset.Now.ToUnixTimeMilliseconds());
        await writer.WriteAsync(inputEvent);
        
        // Assert
        // Use the client/writers/readers to assert some properties that your job should comform to
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(10));
        
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent.DeviceId && 
            message.Value == inputEvent.Value &&
            message.Timestamp == inputEvent.Timestamp &&
            message.ExtraField == "ExtraFieldValue");


        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(10)).Should().BeTrue();

        Assert.Fail("This is really just here to make the test fail and ensure that you either decide to implement a component test or actively decide not to");

    }
}