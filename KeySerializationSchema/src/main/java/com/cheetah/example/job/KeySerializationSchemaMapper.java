package com.cheetah.example.job;

import com.cheetah.example.model.InputEvent;
import com.cheetah.example.model.OutputEvent;
import org.apache.flink.api.common.functions.MapFunction;

/** KeySerializationSchemaMapper converts from InputEvent to OutputEvent. */
public class KeySerializationSchemaMapper implements MapFunction<InputEvent, OutputEvent> {
    private final String extraField;

    public KeySerializationSchemaMapper(final String extraField) {
        this.extraField = extraField;
    }

    @Override
    public OutputEvent map(final InputEvent InputEvent) {
        return new OutputEvent(InputEvent, extraField);
    }
}
