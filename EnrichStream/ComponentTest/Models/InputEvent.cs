using Newtonsoft.Json;

namespace EnrichStream.ComponentTest.Models;

public class InputEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}