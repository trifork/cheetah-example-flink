package cheetah.example.serializationerrorsideoutput.model;

import com.trifork.cheetah.processing.util.deserialization.MaybeParsable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * InputEvent represents the events to be processed.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputEvent implements MaybeParsable {
    private String deviceId;
    private double value;
    private long timestamp;
}
