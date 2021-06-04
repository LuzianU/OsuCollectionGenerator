package com.luzianu.beatmap;

public class Stream {
    private int firstDelta;
    private int totalHitObjects;
    private int totalTime;
    private double baseBpm;
    private StreamType streamType = StreamType.NON_COMPLEX;

    public Stream(int firstDelta, double baseBpm) {
        this.firstDelta = firstDelta;
        this.baseBpm = baseBpm;
    }

    public void add(int delta) {
        totalHitObjects++;
        totalTime += delta;
    }

    public void addLastObject() {
        totalTime += totalTime / totalHitObjects;
        totalHitObjects++;
    }

    public int getBpm() {
        return (int) (15000 / (1.0 * totalTime / totalHitObjects) + .5);
    }

    public double getBaseBpm() {
        return baseBpm;
    }

    @Override
    public String toString() {
        return "Stream{" +
               "totalHitObjects=" + totalHitObjects +
               ", bpm=" + getBpm() +
               ", baseBpm=" + getBaseBpm() +
               ", streamType=" + streamType +
               '}';
    }

    public int getFirstDelta() {
        return firstDelta;
    }

    public int getTotalHitObjects() {
        return totalHitObjects;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public enum StreamType {
        THIRD,
        SIXTH,
        EIGHTH,
        TWELFTH,
        SIXTEENTH,
        NON_COMPLEX
    }
}
