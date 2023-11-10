package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** EnrichStreamOutputEvent represents the events being generated. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent implements Serializable {
    private String deviceId;
    private double value;
    private double enrichValue;

    public OutputEvent(InputEvent inputEvent, EnrichEvent enrichEvent) {
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.enrichValue = enrichEvent.getValue();
    }
}
