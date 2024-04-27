package com.mash.aoptracktime.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class ThreadUtils {
    public void sleep(TimeUnit unit, long timeout) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}