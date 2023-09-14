package cheetah.example.model;

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
public class MergeTwoStreamsOutputEvent implements Serializable {
    public MergeTwoStreamsOutputEvent(MergeTwoStreamsInputEventA inputEventA){
        this.deviceId = inputEventA.getDeviceId();
        this.valueA = inputEventA.getValue();
    }

    private String deviceId;
    private double valueA;
    private double valueB;
}
