package com.luzianu.database;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IntDoublePair {
    public int _int;
    public double _double;

    public IntDoublePair(byte[] bytes) {
        _int = ByteBuffer.wrap(bytes, 1, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        _double = ByteBuffer.wrap(bytes, 6, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    @Override
    public String toString() {
        return "IntDoublePair{" +
               "_int=" + _int +
               ", _double=" + _double +
               '}';
    }
}
