using System.Collections.Generic;

namespace Cheetah.MetricsTesting.PrometheusMetrics
{
    /// <summary>
    /// Represents a prometheus histogram
    /// </summary>
    public class PrometheusHistogram
    {
        /// <summary>
        /// The count of observations for the histogram
        /// </summary>
        public double Count { get; }
        
        /// <summary>
        /// The quantiles for the histogram
        /// </summary>
        public List<KeyValuePair<string, double>> Quantiles { get; }


        /// <summary>
        /// Creates a new instance of <see cref="PrometheusHistogram"/>
        /// </summary>
        /// <param name="count">The observed count of the histogram</param>
        public PrometheusHistogram(double count)
        {
            Count = count;
            Quantiles = new List<KeyValuePair<string, double>>();
        }

        /// <summary>
        /// Adds a quantile to the histogram
        /// </summary>
        /// <param name="quantile">The quantile to add</param>
        /// <param name="value">The value of the quantile</param>
        public void AddQuantile(string quantile, double value)
        {
            Quantiles.Add(new KeyValuePair<string, double>(quantile, value));
        }
    }
}
