using System;
using System.Collections.Generic;

using Microsoft.Extensions.Configuration;
using Xunit;
using Observability.ComponentTest.Models;
using System.Threading;
using System.Threading.Tasks;
using System.Linq;
using Cheetah.Kafka.Testing;
using Cheetah.MetricsTesting.PrometheusMetrics;

namespace Observability.ComponentTest;

[Trait("TestType", "IntegrationTests")]
public class ComponentTest
{
    [Fact]
    public async Task Observability_Component_Test()
    {
        // Arrange
        // Setup configuration. Configuration from appsettings.json is overridden by environment variables.
        var configuration = new ConfigurationBuilder()
            .AddJsonFile("appsettings.json")
            .AddEnvironmentVariables()
            .Build();
        
        // Create a KafkaTestClientFactory to create KafkaTestWriter
        var kafkaClientFactory = KafkaTestClientFactory.Create(configuration);
        
        var writer = kafkaClientFactory.CreateTestWriter<InputEvent>("ObservabilityInputTopic");
        
        // create metric reader
        var metricsReader = new PrometheusMetricsReader("observability-job-taskmanager", 9249);

        // Act
        // Write one or more messages to the writer
        var inputEvent = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 12.34,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        var inputEvent2 = new InputEvent()
        {
            DeviceId = "deviceId-1",
            Value = 56.78,
            Timestamp = DateTimeOffset.UnixEpoch.ToUnixTimeMilliseconds()
        };

        await writer.WriteAsync(inputEvent);
        await writer.WriteAsync(inputEvent2);
        await writer.WriteAsync(inputEvent2);
        await writer.WriteAsync(inputEvent);

        //Wait, to ensure processing is done
        await Task.Delay(TimeSpan.FromSeconds(10));

        //Assert
        //Read counter values.
        //Note that counters in flink are represented as Gauges
        var counter = await metricsReader.GetCounterValueAsync("CountOfMessages");
        Assert.Equal(4, counter);

        //Read gauges values. 
        var gauge = await metricsReader.GetCounterValueAsync("MessagesProcessed");
        Assert.Equal(4, gauge);


        //Read histogram values. The only reason this works with more than 1 taskmanager.numberOfTaskSlots, is because both tasks has seen both a low and a high value
        var histograms = await metricsReader.GetHistogramValueAsync("ValueSpread");
        histograms.ForEach(histogram =>
        {
            Assert.Equal(34, histogram.Quantiles.Where(v => v.Key.Contains("0.5")).Select(v => v.Value).FirstOrDefault());
            Assert.Equal(56, histogram.Quantiles.Where(v => v.Key.Contains("0.99")).Select(v => v.Value).FirstOrDefault());
        });
    }
}