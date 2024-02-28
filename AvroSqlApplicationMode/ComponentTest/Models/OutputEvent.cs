using System.Text.Json.Serialization;

namespace AvroSqlApplicationMode.ComponentTest.Models;

// Rename and implement a model that fits the one you expect your job to produce
public class OutputEvent
{
    public OutputEvent(string deviceId, double value, long timestamp, string extraField)
    {
        DeviceId = deviceId;
        Value = value;
        Timestamp = timestamp;
        ExtraField = extraField;
    }

    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }

    [JsonPropertyName("extraField")]
    public string ExtraField { get; set; }
}