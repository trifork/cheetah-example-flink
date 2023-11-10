using System.Text.Json.Serialization;

namespace EnrichStream.ComponentTest.Models;

// Output from the merge, including data from both Stream A and Stream B
public class OutputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("enrichValue")]
    public double EnrichValue { get; set; }

}