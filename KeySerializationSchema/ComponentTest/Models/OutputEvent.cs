using Newtonsoft.Json;

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

    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }
    
    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
    
    [JsonProperty("keys")]
    public string Keys { get; set; }

    [JsonProperty("extraField")]
    public string ExtraField { get; set; }
    

}