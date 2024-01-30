using System.Text.Json.Serialization;

namespace KeySerializationSchema.ComponentTest.Models;

public class OutputEvent
{
    public OutputEvent(string deviceId, double value, long timestamp, string keys, string extraField)
    {
        DeviceId = deviceId;
        Value = value;
        Timestamp = timestamp;
        Keys = keys;
        ExtraField = extraField;
    }

    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }
    
    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
    
    [JsonPropertyName("keys")]
    public string Keys { get; set; }

    [JsonPropertyName("extraField")]
    public string ExtraField { get; set; }
    

}