using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;

namespace Cheetah.MetricsTesting.PrometheusMetrics
{
    /// <summary>
    /// Utility class used to read metrics from a prometheus endpoint
    /// </summary>
    public class PrometheusMetricsReader : IDisposable
    {
        private const string QuantileString = "quantile=\"";
        private readonly HttpClient httpClient;

        /// <summary>
        /// Creates a reader allowing to read from a prometheus endpoint
        /// </summary>
        /// <param name="host">The host to connect to</param>
        /// <param name="port">The port to connect to, defaults to 9249</param>
        public PrometheusMetricsReader(string host, int port = 9249)
        {
            httpClient = new HttpClient
            {
                BaseAddress = new Uri("http://" + host + ":" + port)
            };
        }

        /// <summary>
        /// Returns all metrics containing the input string, returned by the metrics endpoint.
        /// Multiple metrics with the same name, can happen if running multiple taskmanagers or setting taskmanager.numberOfTaskSlots higher than 1
        /// </summary>
        /// <param name="contains">The string which metrics should contain, if empty returns all metrics</param>
        /// <param name="logMetricsLines">Íf set to true, all lines not starting with #, containing the input string are logged to Console</param>
        /// <returns>All metrics returned by the metrics endpoint</returns>
        public async Task<Dictionary<string, string>> GetMetricsAsync(string contains = "", bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            var metrics = new Dictionary<string, string>();
            using var reader = new StreamReader(stream);
            string? line;
            while ((line = reader.ReadLine()) != null)
            {
                if (line.StartsWith('#'))
                {
                    continue;
                }

                if (!string.IsNullOrEmpty(contains) && !line.Contains(contains))
                {
                    continue;
                }

                if (logMetricsLines)
                {
                    Console.WriteLine(line);
                }
                var split = line.LastIndexOf(' ');
                metrics.Add(line[..(split - 1)], line[split..]);
            }
            return metrics;
        }

        /// <summary>
        /// Get the sum of metrics, for metrics with the given name
        /// Multiple metrics with the same name, can happen if running multiple taskmanagers or setting taskmanager.numberOfTaskSlots higher than 1
        /// </summary>
        /// <param name="name">The name of the metric</param>
        /// <param name="logMetricsLines">Íf set to true, all lines not starting with #, containing the input string are logged to Console</param>
        /// <returns>The sum of the filtered metrics</returns>
        public async Task<double> GetCounterValueAsync(string name, bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            using var reader = new StreamReader(stream);
            string? line;
            double sum = 0;
            bool found = false;
            while ((line = reader.ReadLine()) != null)
            {
                if (line.StartsWith("# HELP"))
                {
                    var split = line.Split(" ");
                    if (split.Length > 4)
                    {
                        found = split[3] == name;
                    }
                    else
                    {
                        found = false;
                    }
                }
                if (line.StartsWith('#'))
                {
                    continue;
                }
                if (found)
                {

                    if (logMetricsLines)
                    {
                        Console.WriteLine(line);
                    }
                    var lineSplit = line.LastIndexOf(' ');
                    sum += double.Parse(line[lineSplit..], NumberStyles.Any, CultureInfo.InvariantCulture);
                }
            }
            return sum;
        }

        /// <summary>
        /// Returns a list of histograms, each containing their list of quantiles
        /// Multiple metrics with the same name, can happen if running multiple taskmanagers or setting taskmanager.numberOfTaskSlots higher than 1
        /// </summary>
        /// <param name="name">The name of the histogram</param>
        /// <param name="logMetricsLines">Íf set to true, all lines not starting with #, containing the input string are logged to Console</param>
        /// <returns>A list of histograms, each containing their list of quantiles</returns>
        public async Task<List<PrometheusHistogram>> GetHistogramValueAsync(string name, bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            using var reader = new StreamReader(stream);
            string? line;
            List<PrometheusHistogram> histograms = new List<PrometheusHistogram>();
            PrometheusHistogram? histogram = null;
            bool found = false;
            while ((line = await reader.ReadLineAsync()) != null)
            {
                if (line.StartsWith("# HELP"))
                {
                    var split = line.Split(" ");
                    if (split.Length > 4)
                    {
                        found = split[3] == name;
                    }
                    else
                    {
                        found = false;
                    }
                }
                if (line.StartsWith('#'))
                {
                    continue;
                }
                if (found)
                {
                    if (logMetricsLines)
                    {
                        Console.WriteLine(line);
                    }
                    var lineSplit = line.LastIndexOf('{');
                    if (line[..lineSplit].EndsWith("_count"))
                    {
                        var spaceSplit = line.LastIndexOf(' ');
                        var count = double.Parse(line.AsSpan(spaceSplit + 1), NumberStyles.Any, CultureInfo.InvariantCulture);
                        histogram = new PrometheusHistogram(count);
                        histograms.Add(histogram);
                    }
                    else
                    {
                        var quantileSplit = line.LastIndexOf(QuantileString, StringComparison.InvariantCulture);
                        var quantile = line[(quantileSplit + QuantileString.Length)..line.LastIndexOf('\"')];
                        var spaceSplit = line.LastIndexOf(' ');
                        var value = double.Parse(line.AsSpan(spaceSplit + 1), NumberStyles.Any, CultureInfo.InvariantCulture);
                        histogram?.AddQuantile(quantile, value);
                    }
                }
            }
            return histograms;
        }

        /// <summary>
        /// Disposes the <see cref="HttpClient"/> used by this reader
        /// </summary>
        public void Dispose()
        {
            httpClient.Dispose();
        }
    }
}
