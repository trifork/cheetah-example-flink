package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrichingEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}
