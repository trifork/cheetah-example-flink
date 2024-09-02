using Newtonsoft.Json;

namespace EnrichStream.ComponentTest.Models;

// Inputs used to enrich stream based on deviceId
public class EnrichEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}