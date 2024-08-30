using Newtonsoft.Json;

namespace MultipleSideOutputExample.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class InputEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("valueA")]
    public double ValueA { get; set; }
    
    [JsonProperty("valueB")]
    public double ValueB { get; set; }
    
    [JsonProperty("valueC")]
    public double ValueC { get; set; }
    
    [JsonProperty("valueD")]
    public double ValueD { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}