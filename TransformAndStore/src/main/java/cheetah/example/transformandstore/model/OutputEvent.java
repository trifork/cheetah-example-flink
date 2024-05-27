package cheetah.example.transformandstore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OutputEvent represents the events being generated.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
    private String status;

    public OutputEvent(InputEvent inputEvent, String status) {
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.timestamp = inputEvent.getTimestamp();
        this.status = status;
    }

    public String getYearStringFromTimestamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy");
        return df.format(new Date(getTimestamp()));
    }

    // Ensure no sensitive data is logged by overriding toString. Eg. Value is printed as ***
    @Override
    public String toString() {
        return "DeviceId: %s, Value: ***, Timestamp: %d, Status: %s".formatted(deviceId, timestamp, status);
    }
}
