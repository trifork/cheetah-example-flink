using System.Text.Json.Serialization;

namespace MultipleSideOutputExample.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class MultipleSideOutputExampleOutputEvent2
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("valueC")]
    public double ValueC { get; set; }

    [JsonPropertyName("valueD")]
    public double ValueD { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}