using System.Text.Json.Serialization;

namespace SerializationErrorCatch.ComponentTest.Models;

public class BadEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }
    [JsonPropertyName("value")]
    public double Value { get; set; }
    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
    [JsonPropertyName("badField")] 
    public string BadField { get; set; }
}