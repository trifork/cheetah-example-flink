using Newtonsoft.Json;

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

    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
    
    [JsonProperty("keys")]
    public string Keys { get; set; }
}