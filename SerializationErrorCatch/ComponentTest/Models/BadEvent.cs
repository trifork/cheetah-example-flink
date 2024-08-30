using Newtonsoft.Json;

namespace SerializationErrorCatch.ComponentTest.Models;

public class BadEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }
    [JsonProperty("value")]
    public double Value { get; set; }
    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
    [JsonProperty("badField")] 
    public string BadField { get; set; }
}