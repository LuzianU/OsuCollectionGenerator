package com.luzianu.database;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TimingPoint {
    public double bpm;
    public double offset;
    public boolean inherited;

    public TimingPoint(byte[] bytes) {
        bpm = ByteBuffer.wrap(bytes, 0, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        offset = ByteBuffer.wrap(bytes, 8, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
        inherited = bytes[16] != 0;
    }

    @Override
    public String toString() {
        return "TimingPoint{" +
               "bpm=" + bpm +
               ", offset=" + offset +
               ", inherited=" + inherited +
               '}';
    }
}
