package cheetah.example.tumblingwindow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TumblingWindowOutputEvent represents the events being generated. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventWindow {
    private String deviceId;
    private long startTime;
    private long endTime;
    private Double[] value;

    // Ensure no sensitive data is logged by overriding toString. Eg. Value is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, StartTime: %d, EndTime: %d, Value: ***".formatted(deviceId, startTime, endTime);
    }
}
