package com.cheetah.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
        this.list = inputEvent.getList();
        this.extraField = extraFieldValue;
    }
    private String deviceId;
    private double value;
    private long timestamp;
    private List<String> list;
    private String extraField;
}
