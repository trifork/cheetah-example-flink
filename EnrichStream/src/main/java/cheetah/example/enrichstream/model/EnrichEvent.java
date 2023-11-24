package cheetah.example.enrichstream.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** EnrichStreamInputEventA represents the events to be processed from Stream A. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrichEvent {
    private String deviceId;
    private double value;
    private long timestamp;
}
