package cheetah.example.observability.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ObservabilityInputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ObservabilityInputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}
