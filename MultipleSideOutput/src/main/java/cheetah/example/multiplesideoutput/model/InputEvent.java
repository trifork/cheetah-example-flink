package cheetah.example.multiplesideoutput.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MultipleSideOutputExampleInputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent {
    private String deviceId;
    private double valueA;
    private double valueB;
    private double valueC;
    private double valueD;
    private long timestamp;

    // Ensure no sensitive data is logged by overriding toString. Eg. ValueA is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, ValueA: ***, ValueB: %s, ValueC: %s, ValueD: %s, Timestamp: %d".formatted(deviceId, valueB, valueC, valueD, timestamp);
    }
}
