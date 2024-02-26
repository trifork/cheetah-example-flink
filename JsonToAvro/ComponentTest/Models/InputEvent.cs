using System.Text.Json.Serialization;

namespace jsonToAvro.ComponentTest.Models;

public class InputEvent
{
        [JsonPropertyName("Aflæsningstidspunkt")]
        public DateTime Aflsningstidspunkt;

        [JsonPropertyName("Akustisk støj")]
        public string Akustiskstj;

        [JsonPropertyName("Enhed")]
        public string Enhed;

        [JsonPropertyName("Enhed_1")]
        public string Enhed_1;

        [JsonPropertyName("Enhed_2")]
        public string Enhed_2;

        [JsonPropertyName("Enhed_3")]
        public string Enhed_3;

        [JsonPropertyName("Infokoder")]
        public string Infokoder;

        [JsonPropertyName("Logget volumen 1")]
        public string Loggetvolumen1;

        [JsonPropertyName("Målerserienummer")]
        public string Mlerserienummer;

        [JsonPropertyName("Timetæller")]
        public string Timetller;

        [JsonPropertyName("Volumen 1")]
        public string Volumen1;
}