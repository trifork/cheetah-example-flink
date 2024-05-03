package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** EnrichStreamInputEventB represents the events to be processed from Stream B. */
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
