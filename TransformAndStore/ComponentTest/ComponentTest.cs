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
    [Fact]
    public async Task Should_BeImplemented_When_ServiceIsCreated()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create a KafkaTestWriter
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("TransformAndStoreInputTopic");
        
        // Create a OpenSearchTestClient to count the initial number of documents in the index
        const string indexName = "transformandstore-index_*";
        var openSearchClient= OpenSearchTestClient.Create(configuration);
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