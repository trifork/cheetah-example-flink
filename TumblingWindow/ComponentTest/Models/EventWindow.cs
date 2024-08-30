using Newtonsoft.Json;
using System.Collections.Generic;

namespace TumblingWindow.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class EventWindow
{
    [JsonProperty("deviceId")]
    public string DeviceId { get; set; }

    [JsonProperty("value")]
    public List<double> Values { get; set; }

    [JsonProperty("timestamp")]
    public long Timestamp { get; set; }
}