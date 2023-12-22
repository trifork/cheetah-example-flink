package cheetah.example.flinkstates.model;

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
public class InputEvent {
    private String deviceId;
    private long timestamp;
    private double value;

    @Override
    public String toString() {
        return "InputEvent{" +
                "deviceId='" + deviceId + '\'' +
                ", timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
}
