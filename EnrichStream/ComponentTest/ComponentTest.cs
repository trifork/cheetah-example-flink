using System;
using System.Collections.Generic;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using EnrichStream.ComponentTest.Models;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;

namespace EnrichStream.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    private readonly IConfiguration _configuration;

    public ComponentTest()
    {
        var conf = new Dictionary<string, string?>
        {
            { "KAFKA:URL", "localhost:9092" },
            { "KAFKA:OAUTH2:CLIENTID", "default-access" },
            { "KAFKA:OAUTH2:CLIENTSECRET", "default-access-secret" },
            { "KAFKA:OAUTH2:SCOPE", "kafka" },
            { "KAFKA:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token" },
            { "KAFKA:SCHEMAREGISTRYURL", "http://localhost:8081/apis/ccompat/v7" }
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
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(_configuration);
        
        // Create KafkaTestWriters for the input topics and a KafkaTestReader for the output topic
        var enrichEventWriter = kafkaClientFactory.CreateTestWriter<EnrichEvent>("EnrichStreamEnrichTopic");
        var inputEventWriter= kafkaClientFactory.CreateTestWriter<InputEvent>("EnrichStreamInputTopic");
        var outputReader = kafkaClientFactory.CreateTestReader<OutputEvent>("EnrichStreamOutputTopic", "MyGroup");
        
        //Act
        // Write two Enrich Events with two different deviceIds
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
        
        // Write two Input Events to input stream - one with a deviceIds which has been processed on enriching stream, and one which hasn't.
        // Resulting in one message on the output topic
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
        // Verify that the output topic contains the expected message
        var messages = outputReader.ReadMessages(1, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEventB.DeviceId &&
            message.EnrichValue == enrichEventA.Value &&
            message.Value == inputEventB.Value
        );
        outputReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();
    }
}