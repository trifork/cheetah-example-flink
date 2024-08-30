using Newtonsoft.Json;

namespace EnrichStream.ComponentTest.Models;

// Output from the merge, including data from both Stream A and Stream B
public class OutputEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("enrichValue")]
    public double EnrichValue { get; set; }

}