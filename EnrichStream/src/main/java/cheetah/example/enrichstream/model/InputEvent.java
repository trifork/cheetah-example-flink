package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** MergeTwoStreamsInputEventA represents the events to be processed from Stream A. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}