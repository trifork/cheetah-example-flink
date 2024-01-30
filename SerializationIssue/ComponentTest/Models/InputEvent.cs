using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace SerializationIssue.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class InputEvent
{
    public InputEvent(string deviceId, double value, long timestamp, List<string> list)
    {
        DeviceId = deviceId;
        Value = value;
        Timestamp = timestamp;
        List = list;
    }

    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
    
    [JsonPropertyName("list")]
    public List<string> List { get; set; }
}