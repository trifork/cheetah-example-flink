using Newtonsoft.Json;

namespace AvroToJson.ComponentTest.Models;

public class OutputEventJson
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}