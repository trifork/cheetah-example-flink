using System.Text.Json.Serialization;

namespace MergeTwoStreams.ComponentTest.Models;

// Output from the merge, including data from both Stream A and Stream B
public class OutputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("valueA")]
    public double ValueA { get; set; }

    [JsonPropertyName("valueB")]
    public double ValueB { get; set; }

}