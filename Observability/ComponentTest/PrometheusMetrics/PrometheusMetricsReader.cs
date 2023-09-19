using OpenSearch.Client;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Globalization;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;

namespace Observability.ComponentTest.PrometheusMetrics
{
    public class PrometheusMetricsReader
    {
        private const string QuantileString = "quantile=\"";
        private HttpClient httpClient;

        public PrometheusMetricsReader(string host, int port)
        {
            httpClient = new HttpClient
            {
                BaseAddress = new Uri("http://" + host + ":" + port)
            };
        }

        public async Task<Dictionary<string, string>> GetMetricsAsync(bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            var metrics = new Dictionary<string, string>();
            using var reader = new StreamReader(stream);
            string line;
            while ((line = reader.ReadLine()) != null)
            {
                if (line.StartsWith("#"))
                {
                    continue;
                }
                if (logMetricsLines)
                {
                    Console.WriteLine(line);
                }
                var split = line.LastIndexOf(' ');
                metrics.Add(line.Substring(0, split - 1), line.Substring(split));
            }
            return metrics;
        }

        public async Task<Dictionary<string, string>> GetMetricsAsync(string contains, bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            var metrics = new Dictionary<string, string>();
            using var reader = new StreamReader(stream);
            string line;
            while ((line = reader.ReadLine()) != null)
            {
                if (line.StartsWith("#") || !line.Contains(contains))
                {
                    continue;
                }
                if (logMetricsLines)
                {
                    Console.WriteLine(line);
                }
                var split = line.LastIndexOf(' ');
                metrics.Add(line.Substring(0, split - 1), line.Substring(split));
            }
            return metrics;
        }

        public async Task<double> GetCounterValueAsync(string name, bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            using var reader = new StreamReader(stream);
            string line;
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
                if (line.StartsWith("#"))
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
                    sum += double.Parse(line.Substring(lineSplit), NumberStyles.Any, CultureInfo.InvariantCulture);
                }
            }
            return sum;
        }

        public async Task<List<PrometheusHistogram>> GetHistogramValueAsync(string name, bool logMetricsLines = false)
        {
            var stream = await httpClient.GetStreamAsync("");
            using var reader = new StreamReader(stream);
            string line;
            List<PrometheusHistogram> histograms = new List<PrometheusHistogram>();
            PrometheusHistogram histogram = null;
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
                if (line.StartsWith("#"))
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
                    if (line.Substring(0, lineSplit).EndsWith("_count"))
                    {
                        var spaceSplit = line.LastIndexOf(' ');
                        var count = double.Parse(line.Substring(spaceSplit), NumberStyles.Any, CultureInfo.InvariantCulture);
                        histogram = new PrometheusHistogram(count);
                        histograms.Add(histogram);
                    }
                    else
                    {
                        var quantileSplit = line.LastIndexOf(QuantileString);
                        var quantile = line.Substring(quantileSplit + QuantileString.Length, line.LastIndexOf('\"') - (quantileSplit + QuantileString.Length));
                        var spaceSplit = line.LastIndexOf(' ');
                        var value = double.Parse(line.Substring(spaceSplit + 1), NumberStyles.Any, CultureInfo.InvariantCulture);
                        histogram.AddQuantile(quantile, value);
                    }
                }
            }
            return histograms;
        }
    }
}
