using System.Text.Json.Serialization;

namespace EnrichStream.ComponentTest.Models;

// Inputs used to enrich stream based on deviceId
public class EnrichEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}