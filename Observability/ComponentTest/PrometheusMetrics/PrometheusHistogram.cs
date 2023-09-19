using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Observability.ComponentTest.PrometheusMetrics
{
    public class PrometheusHistogram
    {
        public double Count { get; }
        public List<KeyValuePair<string, double>> Quantiles { get; }


        public PrometheusHistogram(double count)
        {
            this.Count = count;
            this.Quantiles = new List<KeyValuePair<string,double>>();
        }

        public void AddQuantile(string quantile, double value)
        {
            Quantiles.Add(new KeyValuePair<string, double>(quantile, value));
        }
    }
}
