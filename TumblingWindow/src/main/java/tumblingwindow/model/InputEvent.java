package tumblingwindow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TumblingWindowInputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent {
    private String deviceId;
    private long timestamp;
    private double value;
}
