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
public class InputEvent {
    private String deviceId;
    private double value;
    private long timestamp;

    // Ensure no sensitive data is logged by overriding toString. Eg. Value is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, Value: ***, Timestamp: %d".formatted(deviceId, timestamp);
    }
}
