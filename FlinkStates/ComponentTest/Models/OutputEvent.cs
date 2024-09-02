using Newtonsoft.Json;

namespace FlinkStates.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class FlinkStatesOutputEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }

    [JsonProperty("extraField")]
    public string ExtraField { get; set; }
}