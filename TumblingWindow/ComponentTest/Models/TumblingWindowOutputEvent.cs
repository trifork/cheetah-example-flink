using System.Text.Json.Serialization;
using System.Collections.Generic;

namespace TumblingWindow.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class TumblingWindowOutputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public List<double> Values { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}