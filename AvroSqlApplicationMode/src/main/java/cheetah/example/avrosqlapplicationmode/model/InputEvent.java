package cheetah.example.avrosqlapplicationmode.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** InputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}