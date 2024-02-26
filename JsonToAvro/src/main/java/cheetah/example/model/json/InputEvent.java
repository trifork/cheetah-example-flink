package cheetah.example.model.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.annotation.Nullable;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonCreator;

// https://github.com/apache/flink/blob/e74592ca92f4eac5bef6e5140ae4e8cc2f0bf1a1/flink-runtime/src/main/java/org/apache/flink/runtime/rest/messages/LogInfo.java#L25
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
public class InputEvent implements Serializable {

    @JsonCreator
    public InputEvent(
            @JsonProperty("Aflæsningstidspunkt") final String readingTimestamp,
            @JsonProperty("Akustisk støj") final String acousticNoise,
            @JsonProperty("Enhed") final String unit,
            @JsonProperty("Enhed_1") final String unit1,
            @JsonProperty("Enhed_2") final String unit2,
            @JsonProperty("Enhed_3") final String unit3,
            @JsonProperty("Infokoder") final String infocodes,
            @JsonProperty("Logget volumen 1") final String loggedVolume1,
            @JsonProperty("Målerserienummer") final String meterSerialNumber,
            @JsonProperty("Timetæller") final String hourCounter,
            @JsonProperty("Volumen 1") final String volume1) {
        this.readingTimestamp = Objects.requireNonNull(readingTimestamp);
        this.acousticNoise = Objects.requireNonNull(acousticNoise);
        this.unit = Objects.requireNonNull(unit);
        this.unit1 = Objects.requireNonNull(unit1);
        this.unit2 = Objects.requireNonNull(unit2);
        this.unit3 = Objects.requireNonNull(unit3);
        this.infocodes = Objects.requireNonNull(infocodes);
        this.loggedVolume1 = Objects.requireNonNull(loggedVolume1);
        this.meterSerialNumber = Objects.requireNonNull(meterSerialNumber);
        this.hourCounter = Objects.requireNonNull(hourCounter);
        this.volume1 = Objects.requireNonNull(volume1);
    }

    @JsonProperty("Aflæsningstidspunkt") 
    public String readingTimestamp;
    @JsonProperty("Akustisk støj") 
    public String acousticNoise;
    @JsonProperty("Enhed") 
    public String unit;
    @JsonProperty("Enhed_1") 
    public String unit1;
    @JsonProperty("Enhed_2") 
    public String unit2;
    @JsonProperty("Enhed_3") 
    public String unit3;
    @JsonProperty("Infokoder") 
    public String infocodes;
    @JsonProperty("Logget volumen 1") 
    public String loggedVolume1;
    @JsonProperty("Målerserienummer") 
    public String meterSerialNumber;
    @JsonProperty("Timetæller") 
    public String hourCounter;
    @JsonProperty("Volumen 1") 
    public String volume1;
}
