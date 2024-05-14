package cheetah.example.multiplesideoutput.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent {
    private String deviceId;
    public double value;
    private long timestamp;

    // Ensure no sensitive data is logged by overriding toString. Eg. Value is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, Value: ***, Timestamp: %d".formatted(deviceId, timestamp);
    }
}
