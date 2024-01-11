using System;
using System.Collections.Generic;
using Cheetah.ComponentTest.Kafka;
using Microsoft.Extensions.Configuration;
using Xunit;
using Observability.ComponentTest.Models;
using Observability.ComponentTest.PrometheusMetrics;
using System.Threading;
using System.Threading.Tasks;
using System.Linq;

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
        
        // Here you'll set up one or more writers and readers, which connect to the topic(s) that your job consumes
        // from and publishes to. 
        var writer = KafkaWriterBuilder.Create<string, InputEvent>(configuration)
            .WithTopic("ObservabilityInputTopic") // The topic to consume from
            .WithKeyFunction(model => model.DeviceId) // Optional function to retrieve the message key.
                                                      // If no key is desired, use KafkaWriterBuilder.Create<Null, InputModel>
                                                      // and make this function return null
            .Build();

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
        await Task.Delay(TimeSpan.FromSeconds(20));

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