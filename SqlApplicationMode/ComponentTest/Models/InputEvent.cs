using System.Text.Json.Serialization;

namespace SqlApplicationMode.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class InputEvent
{
    public InputEvent(string deviceId, double value, long timestamp)
    {
        DeviceId = deviceId;
        Value = value;
        Timestamp = timestamp;
    }

    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}