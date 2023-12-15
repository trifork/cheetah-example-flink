using System;
using System.Collections.Generic;
using Cheetah.ComponentTest.Kafka;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using EnrichStream.ComponentTest.Models;
using System.Threading.Tasks;

namespace EnrichStream.ComponentTest;

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
    public async Task Merge_Two_Streams_Component_Test()
    {
        // Arrange
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var enrichEventWriter = KafkaWriterBuilder.Create<string, EnrichEvent>(_configuration)
            .WithTopic("EnrichStreamEnrichTopic") // The topic to consume from (Stream A) in the job
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
            .Build();

        var inputEventWriter = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("EnrichStreamInputTopic") // The topic to consume from (Stream B) in the job
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
            .Build();

        var outputReader = KafkaReaderBuilder.Create<string, OutputEvent>(_configuration)
            .WithTopic("EnrichStreamOutputTopic")    // The topic being published to from the job
            .WithConsumerGroup("MyGroup")                     // The consumer group used for reading from the topic
            .Build();

        // Act
        // Write two messages with different deviceIds to enriching stream
        var enrichEventA = new EnrichEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        var enrichEventB = new EnrichEvent()
        {
            DeviceId = "deviceId-2",
            Value = 90.12,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        await enrichEventWriter.WriteAsync(enrichEventA);
        await enrichEventWriter.WriteAsync(enrichEventB);

        // Wait to make sure the elements on enriching stream have been processed before writing to input stream
        await Task.Delay(500);

        // Write two messages to input stream - one with a deviceIds which has been processed on enriching stream, and one which hasn't. Resulting in one message on the output topic
        var inputEventA = new InputEvent()
        {
            DeviceId = "deviceId-3",
            Value = 32.10,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        var inputEventB = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        await inputEventWriter.WriteAsync(inputEventA);
        await inputEventWriter.WriteAsync(inputEventB);

        // Assert
        // Then consume using the reader, supplying how many output messages your input messages expected to generate
        // as well as the maximum duration it is allowed to take for those messages to be produced
        var messages = outputReader.ReadMessages(1, TimeSpan.FromSeconds(5));
        
        // Then evaluate whether your messages are as expected, and that there are only as many as you expected 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEventB.DeviceId &&
            message.EnrichValue == enrichEventA.Value &&
            message.Value == inputEventB.Value
        );
        outputReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();
    }
}