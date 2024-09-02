using Newtonsoft.Json;

namespace TumblingWindow.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class InputEvent
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public double Value { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}