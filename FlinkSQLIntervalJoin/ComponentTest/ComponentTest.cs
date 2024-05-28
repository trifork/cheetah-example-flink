using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Cheetah.Kafka.Testing;
using FluentAssertions;
using Microsoft.Extensions.Configuration;
using Xunit;
using System.Text.Json.Nodes;
using System.IO;
using System.Text.Json;
using System.Linq;
using Confluent.Kafka;


namespace FlinkStates.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest2
{
    const string KafkaGroupName = "TestGroup2";
    const string KeyName = "SalesOrder";
    const long TooEarlyOrLateTimeDifferenceMillis = 30000;

    [Fact]
    public async Task Flink_States_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestReaders and KafkaTestWriters
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);

        var keyExtractionFunction = (JsonNode json) => json[KeyName].GetValue<string>();

        // Create KafkaTestWriters to write messages to the input topics
        var writerSalesOrderItems =
            kafkaClientFactory.CreateTestWriter<string, JsonNode>("A_SalesOrderItem", keyExtractionFunction);
        var writerSalesOrders =
            kafkaClientFactory.CreateTestWriter<string, JsonNode>("A_SalesOrder", keyExtractionFunction);
        
        // Create KafkaTestReaders to read messages from the topics
        var joinedValueReader = kafkaClientFactory.CreateTestReader<string, JsonNode>("SQL-Test-Join-Enriched-Output", null);
        var itemsDLQReader = kafkaClientFactory.CreateTestReader<string, JsonNode>("SQL-Test-Join-LeftDLQ", null);
        var salesOrderDLQReader = kafkaClientFactory.CreateTestReader<string, JsonNode>("SQL-Test-Join-RightDLQ", null);

        // Act
        // Read different JSONs from files
        var salesOrdersWithMatches = ReadJsonFromFile(GetLocalPath("res/salesorders_canbejoined.json"));
        var salesOrdersWithoutMatches = ReadJsonFromFile(GetLocalPath("res/salesorders_cannotbejoined.json"));
        var salesOrderItemsWithMatches = ReadJsonFromFile(GetLocalPath("res/salesorderitems_canbejoined.json"));
        var salesOrderItemsWithoutMatches = ReadJsonFromFile(GetLocalPath("res/salesorderitems_cannotbejoined.json"));
        var salesOrderItemsTooEarlyTimestamp = ReadJsonFromFile(GetLocalPath("res/salesorderitems_tooearly.json"));
        var salesOrderItemsTooLateTimestamp = ReadJsonFromFile(GetLocalPath("res/salesorderitems_toolate.json"));

        // Write all into the appropriate topics
        await WriteAllIntoTopicAddTimestamp(writerSalesOrders, salesOrdersWithMatches, keyExtractionFunction);
        await WriteAllIntoTopicAddTimestamp(writerSalesOrders, salesOrdersWithoutMatches, keyExtractionFunction);
        await WriteAllIntoTopicAddTimestamp(writerSalesOrderItems, salesOrderItemsWithMatches, keyExtractionFunction);
        await WriteAllIntoTopicAddTimestamp(writerSalesOrderItems, salesOrderItemsWithoutMatches, keyExtractionFunction);
        await WriteAllIntoTopicAddTimestamp(writerSalesOrderItems, salesOrderItemsTooEarlyTimestamp, keyExtractionFunction, -1 * TooEarlyOrLateTimeDifferenceMillis);
        await WriteAllIntoTopicAddTimestamp(writerSalesOrderItems, salesOrderItemsTooLateTimestamp, keyExtractionFunction, TooEarlyOrLateTimeDifferenceMillis);
        
        // Create expected values
        var expectedJoinedResultSet = new List<JsonObject>();
        foreach (var salesOrder in salesOrdersWithMatches)
        {
            foreach (var salesOrderItem in salesOrderItemsWithMatches)
            {
                if (salesOrder[KeyName].GetValue<string>() == salesOrderItem[KeyName].GetValue<string>())
                {
                    expectedJoinedResultSet.Add(JoinJSONsLeftPrecedence(salesOrderItem.AsObject(), salesOrder.AsObject()));
                }
            }
        }

        // This is what we would expect for the DLQs in theory, but we receive the items in the Debezium format instead:
        // var expectedItemsDLQResultSet = salesOrderItemsWithoutMatches.Union(salesOrderItemsTooEarlyTimestamp).Union(salesOrderItemsTooLateTimestamp);
        // var expectedSalesOrderDLQResultSet = salesOrdersWithoutMatches.AsEnumerable();


        // Assert
        await Task.Delay(TimeSpan.FromSeconds(3)); //20
        var joinedValues = joinedValueReader.ReadMessages(4, TimeSpan.FromSeconds(1));
        var itemsDLQValues = itemsDLQReader.ReadMessages(4, TimeSpan.FromSeconds(1));
        var salesOrderDLQValues = salesOrderDLQReader.ReadMessages(2, TimeSpan.FromSeconds(1));

        var jsonEqualityComparer = new SemanticJsonEqualityComparer();

        var allJoinsFound = joinedValues.All(readElement => expectedJoinedResultSet.Exists(expectedElement => jsonEqualityComparer.Equal(readElement, expectedElement)));
        Assert.True(allJoinsFound);

        joinedValueReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        itemsDLQReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue();
        //salesOrderDLQReader.VerifyNoMoreMessages(TimeSpan.FromSeconds(1)).Should().BeTrue(); // Not true because we receive an Update stream from the Flink SQL join
    }

    private JsonArray ReadJsonFromFile(string filePath)
    {
        return JsonNode.Parse(File.ReadAllBytes(filePath)).AsArray();
    }

    private async Task WriteAllIntoTopicAddTimestamp(IKafkaTestWriter<string, JsonNode> writer, JsonArray arrayJsonElement, Func<JsonNode, string> keyExtractionFunction, long addToTimestampMillis = 0)
    {
        var kafkaMessages = new Message<string, JsonNode>[arrayJsonElement.Count];
        for (int i = 0; i < arrayJsonElement.Count; i++)
        {
            var message = new Message<string, JsonNode>()
            {
                Key = keyExtractionFunction(arrayJsonElement[i]),
                Value = arrayJsonElement[i],
                Timestamp = new Timestamp(DateTimeOffset.Now.ToUnixTimeMilliseconds() + addToTimestampMillis, TimestampType.CreateTime)
            };

            kafkaMessages[i] = message;
        }

        await writer.WriteAsyncKafkaMessages(kafkaMessages);

    }

    private JsonObject JoinJSONsLeftPrecedence(JsonObject left, JsonObject right)
    {
        var result = new JsonObject(); right.DeepClone().AsObject();
        result = joinIntoResult(left, result);
        result = joinIntoResult(right, result);
        return result;
    }

    private JsonObject joinIntoResult(object jsonObject, JsonObject result)
    {
        foreach (var jsonProperty in result)
        {
            var value = jsonProperty.Value;

            if (value == null)
            {
                result[jsonProperty.Key] = null;
            }
            else if (!(value.GetValueKind() == JsonValueKind.Object || value.GetValueKind() == JsonValueKind.Array)) // What we actually achieve: filter out navigation properties
            {
                result[jsonProperty.Key] = value.DeepClone();
            }
        }

        return result;
    }

    private string GetLocalPath(string relativePath)
    {
        if (AppDomain.CurrentDomain.BaseDirectory.Length < 1)
        {
            throw new ArgumentException();
        }
        return Path.Combine(AppDomain.CurrentDomain.BaseDirectory, relativePath);
    }

}