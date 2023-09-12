using System.Text.Json.Serialization;

namespace MultipleSideOutputExample.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class MultipleSideOutputExampleInputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("valueA")]
    public double ValueA { get; set; }
    
    [JsonPropertyName("valueB")]
    public double ValueB { get; set; }
    
    [JsonPropertyName("valueC")]
    public double ValueC { get; set; }
    
    [JsonPropertyName("valueD")]
    public double ValueD { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}