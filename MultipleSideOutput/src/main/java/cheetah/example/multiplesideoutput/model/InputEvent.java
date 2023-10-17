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
}
