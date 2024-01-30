using System.Text.Json.Serialization;

namespace KeySerializationSchema.ComponentTest.Models;

public class InputEvent
{
    public InputEvent(string deviceId, double value, long timestamp, string keys)
    {
        DeviceId = deviceId;
        Value = value;
        Keys = keys;
        Timestamp = timestamp;
    }

    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
    
    [JsonPropertyName("keys")]
    public string Keys { get; set; }
}