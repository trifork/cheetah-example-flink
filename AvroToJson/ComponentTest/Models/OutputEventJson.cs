using System.Text.Json.Serialization;

namespace AvroToJson.ComponentTest.Models;

public class OutputEventJson
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}