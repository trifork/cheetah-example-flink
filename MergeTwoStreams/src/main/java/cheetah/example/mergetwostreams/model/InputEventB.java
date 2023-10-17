package cheetah.example.mergetwostreams.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MergeTwoStreamsInputEventB represents the events to be processed from Stream B. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEventB {
    private String deviceId;
    private double value;
    private long timestamp;
}
