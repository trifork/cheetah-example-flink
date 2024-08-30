using Newtonsoft.Json;

namespace MultipleSideOutputExample.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class OutputEvent2
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("valueC")]
    public double ValueC { get; set; }

    [JsonProperty("valueD")]
    public double ValueD { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}