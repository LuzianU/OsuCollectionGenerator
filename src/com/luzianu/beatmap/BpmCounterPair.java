package com.luzianu.beatmap;

/**
 * Helper class
 */
public class BpmCounterPair {
    public int bpm;
    public int counter;

    public BpmCounterPair(int bpm, int counter) {
        this.bpm = bpm;
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "[" +
               "bpm=" + bpm +
               ", counter=" + counter +
               ']';
    }
}
