package cheetah.example.multiplesideoutput.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent2 {
    private String deviceId;
    public double valueC;
    public double valueD;
    private long timestamp;

}
