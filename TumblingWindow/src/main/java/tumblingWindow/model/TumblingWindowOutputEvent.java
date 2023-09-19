package tumblingWindow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TumblingWindowOutputEvent represents the events being generated. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TumblingWindowOutputEvent {
    public TumblingWindowOutputEvent(TumblingWindowInputEvent inputEvent, String extraFieldValue){
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.timestamp = inputEvent.getTimestamp();
        this.extraField = extraFieldValue;
    }
    private String deviceId;
    private double value;
    private long timestamp;
    private String extraField;
}
