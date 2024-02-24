package fepa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OutputEvent represents the events being generated. */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SQLevent {

    public SQLevent(InputEvent inputEvent){
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.timestamp = inputEvent.getTimestamp();
    }

    private String deviceId;

    private double value;

    private long timestamp;
}
