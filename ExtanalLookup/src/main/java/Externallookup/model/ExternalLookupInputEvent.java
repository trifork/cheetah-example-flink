package Externallookup.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ExternalLookupInputEvent represents the events to be processed. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalLookupInputEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}