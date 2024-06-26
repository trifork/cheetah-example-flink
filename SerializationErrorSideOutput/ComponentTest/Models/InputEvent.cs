﻿using System.Text.Json.Serialization;

namespace SerializationErrorSideOutput.ComponentTest.Models;

// Rename and implement a model that fits what your job expects to receive
public class InputEvent
{
    [JsonPropertyName("deviceId")]
    public string DeviceId { get; set; }

    [JsonPropertyName("value")]
    public double Value { get; set; }

    [JsonPropertyName("timestamp")]
    public long Timestamp { get; set; }
}