using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using Cheetah.OpenSearch.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using OpenSearch.Client;
using Xunit;
using TransformAndStore.ComponentTest.Models;

namespace TransformAndStore.ComponentTest;

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
            { "KAFKA:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token" },
            {"OPENSEARCH:URL", "http://localhost:9200"},
            {"OPENSEARCH:AuthMode", "oauth2"},
            {"OPENSEARCH:OAUTH2:CLIENTID", "default-access"},
            {"OPENSEARCH:OAUTH2:CLIENTSECRET", "default-access-secret"},
            {"OPENSEARCH:OAUTH2:SCOPE", "opensearch"},
            {"OPENSEARCH:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token"}
            
            
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Create a KafkaTestClientFactory to create a KafkaTestWriter
        var kafkaClientFactory = KafkaTestClientFactory.Create(_configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("TransformAndStoreInputTopic");
        
        // Create a OpenSearchTestClient to count the initial number of documents in the index
        const string indexName = "transformandstore-index_*";
        var openSearchClient= OpenSearchTestClient.Create(_configuration);
        var initialDocCount = await openSearchClient.CountAsync<object>(q => q.Index(indexName));
        
        // Act
        // Write three messages to the writer
        var inputEventTooLow = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 32.45,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };
        
        var inputEventGood = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 74.88,
            Timestamp = DateTimeOffset.UnixEpoch.AddSeconds(1).ToUnixTimeMilliseconds()
        };
        
        var inputEventTooHigh = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 120.60,
            Timestamp = DateTimeOffset.UnixEpoch.AddSeconds(2).ToUnixTimeMilliseconds()
        };
        
        await writer.WriteAsync(inputEventTooLow);
        await writer.WriteAsync(inputEventGood);
        await writer.WriteAsync(inputEventTooHigh);
        
        // Add delay to make sure the Job have add time to store data in OpenSearch
        await Task.Delay(TimeSpan.FromSeconds(2));
        
        // Count of objects with specified index name
        var docCount = await openSearchClient.CountAsync<object>(q => q.Index(indexName));
        docCount.Count.Should().Be(3 + initialDocCount.Count);
    }
}