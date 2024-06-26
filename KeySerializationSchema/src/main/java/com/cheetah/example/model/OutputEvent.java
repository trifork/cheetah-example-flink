package com.cheetah.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OutputEvent represents the events being generated. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OutputEvent {
    public OutputEvent(InputEvent inputEvent, String extraFieldValue){
        this.deviceId = inputEvent.getDeviceId();
        this.value = inputEvent.getValue();
        this.timestamp = inputEvent.getTimestamp();
        this.keys = inputEvent.getKeys();
        this.extraField = extraFieldValue;
    }

    private String deviceId;
    private double value;
    private long timestamp;
    private String keys;
    private String extraField;
}
