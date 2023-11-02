package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** MergeTwoStreamsOutputEvent represents the events being generated. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent implements Serializable {
    private String deviceId;
    private double valueA;
    private double valueB;

    public OutputEvent(EnrichingEvent inputEvent, double valueB) {
        this.deviceId = inputEvent.getDeviceId();
        this.valueA = inputEvent.getValue();
        this.valueB = valueB;
    }
}
