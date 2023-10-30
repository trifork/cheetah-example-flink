package cheetah.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FlinkStatesInputEvent represents the events to be processed.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlinkStatesInputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}