using System;
using System.Collections.Generic;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using TumblingWindow.ComponentTest.Models;
using System.Linq;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;

namespace TumblingWindow.ComponentTest;

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
            { "KAFKA:OAUTH2:TOKENENDPOINT", "http://localhost:1852/realms/local-development/protocol/openid-connect/token" }
        };
        _configuration = new ConfigurationBuilder()
            .AddInMemoryCollection(conf)
            .AddEnvironmentVariables()
            .Build();
    }

    [Fact] 
    public async Task Tumbling_Window_Component_Test()
    {
        // Arrange
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(_configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("TumblingWindowInputTopic");
        var reader = kafkaClientFactory.CreateTestReader<EventWindow>("TumblingWindowOutputTopic");
        
        // Act
        // Write messages to the writer
        var inputEvent1 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        var inputEvent2 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };
        var inputEvent3 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 910.1112,
            Timestamp = DateTimeOffset.Now.ToUnixTimeMilliseconds()
        };

        var inputEvent4 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 910.1112,
            Timestamp = DateTimeOffset.Now.AddMinutes(10).ToUnixTimeMilliseconds()
        };
        
        await writer.WriteAsync(inputEvent1, inputEvent2, inputEvent3);
        await writer.WriteAsync(inputEvent4, inputEvent4);
        
        // Assert
        // Read messages from the reader
        var messages = reader.ReadMessages(1, TimeSpan.FromSeconds(20));
        
        // Evaluate the messages 
        messages.Should().ContainSingle(message => 
            message.DeviceId == inputEvent1.DeviceId &&
            message.Values.Count == 3 &&
            message.Values.All(item => new double[]{inputEvent1.Value,
                    inputEvent2.Value,
                    inputEvent3.Value}
                .Contains(item)));
        reader.VerifyNoMoreMessages(TimeSpan.FromSeconds(20)).Should().BeTrue();
    }


}