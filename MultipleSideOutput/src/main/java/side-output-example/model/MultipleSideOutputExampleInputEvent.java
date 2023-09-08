package side-output-example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MultipleSideOutputExampleInputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MultipleSideOutputExampleInputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}
