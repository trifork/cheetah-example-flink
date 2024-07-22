using System;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using EnrichStream.ComponentTest.Models;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using Confluent.Kafka;

namespace EnrichStream.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    
    [Fact]
    public async Task Merge_Two_Streams_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
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

        var enrichMessageA = new Message<Null, EnrichEvent>()
        {
            Value = enrichEventA
        };
        var enrichMessageB = new Message<Null, EnrichEvent>()
        {
            Value = enrichEventB
        };

        await enrichEventWriter.WriteAsync(enrichMessageA);
        await enrichEventWriter.WriteAsync(enrichMessageB);
        
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
        
        var inputMessageA = new Message<Null, InputEvent>()
        {
            Value = inputEventA
        };
        var inputMessageB = new Message<Null, InputEvent>()
        {
            Value = inputEventB
        };
        
        await inputEventWriter.WriteAsync(inputMessageA);
        await inputEventWriter.WriteAsync(inputMessageB);
        
        // Assert
        // Verify that the output topic contains the expected message
        var messages = outputReader.ReadMessages(1, TimeSpan.FromSeconds(5));
        messages.Should().ContainSingle(message => 
            message.Value.DeviceId == inputEventB.DeviceId &&
            message.Value.EnrichValue == enrichEventA.Value &&
            message.Value.Value == inputEventB.Value
        );
        outputReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(5)).Should().BeTrue();
    }
}