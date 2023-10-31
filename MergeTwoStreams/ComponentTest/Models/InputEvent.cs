using System.Text.Json.Serialization;

namespace MergeTwoStreams.ComponentTest.Models;

// Inputs used to merge two streams based on deviceId
public class InputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}