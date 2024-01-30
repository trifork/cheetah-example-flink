package com.cheetah.example.function;

import com.cheetah.example.model.InputEvent;
import org.apache.flink.api.common.functions.RichFilterFunction;

public class FilterMessage extends RichFilterFunction<InputEvent> {
    @Override
    public boolean filter(InputEvent inputEvent) {
        return !inputEvent.getList().isEmpty();
    }
}
