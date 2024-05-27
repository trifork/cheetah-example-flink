package cheetah.example.serializationerrorsideoutput.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OutputEvent represents the events being generated.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent {
    public OutputEvent(InputEvent inputEvent, String extraFieldValue) {
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.timestamp = inputEvent.getTimestamp();
        this.extraField = extraFieldValue;
    }

    private String deviceId;
    private double value;
    private long timestamp;
    private String extraField;

    // Ensure no sensitive data is logged by overriding toString. Eg. Value is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, Value: ***, Timestamp: %d, ExtraField: %s".formatted(deviceId, timestamp, extraField);
    }
}