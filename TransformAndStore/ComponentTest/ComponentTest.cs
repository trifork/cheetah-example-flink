using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.ComponentTest.Kafka;
using Cheetah.ComponentTest.OpenSearch;
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
            {"KAFKA:AUTHENDPOINT", "http://localhost:8080/realms/local-development/protocol/openid-connect/token"},
            {"KAFKA:CLIENTID", "default-access" },
            {"KAFKA:CLIENTSECRET", "default-access-secret" },
            {"KAFKA:URL", "localhost:9092"},
            {"KAFKA:AUTHSCOPE", "kafka opensearch"},
            {"OPENSEARCH:URL", "http://localhost:9200"},
            {"OPENSEARCH:CLIENTID", "default-access"},
            {"OPENSEARCH:CLIENTSECRET", "default-access-secret"},
            {"OPENSEARCH:OAUTHSCOPE", "kafka opensearch"},
            {"OPENSEARCH:AUTHENDPOINT", "http://localhost:8080/realms/local-development/protocol/openid-connect/token"}
            
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
        // Setting up the writer (Kafka producer), to produce messages on topic "TransformAndStoreInputTopic"
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(_configuration)
            .WithTopic("TransformAndStoreInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId)
            .Build();


        const string indexName = "transformandstore-index_*";
        var openSearchClient = OpenSearchClientFactory.Create(_configuration);
        var initialDocCount = await openSearchClient.CountIndexedDocumentsAsync(indexName);

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
        
        // Refresh and count of objects with specified index name
        await openSearchClient.RefreshIndexAsync(indexName);
        var count = await openSearchClient.CountIndexedDocumentsAsync(indexName);
        count.Should().Be(3 + initialDocCount);
    }
}