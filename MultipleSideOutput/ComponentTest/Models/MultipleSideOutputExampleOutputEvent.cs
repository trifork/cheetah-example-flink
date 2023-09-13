using System.Text.Json.Serialization;

namespace MultipleSideOutputExample.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class MultipleSideOutputExampleOutputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}