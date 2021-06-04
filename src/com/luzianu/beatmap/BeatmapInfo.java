package com.luzianu.beatmap;

import java.util.HashMap;
import java.util.List;

public class BeatmapInfo {
    public String title;
    public String artist;
    public String creator;
    public String difficulty;
    public String md5;

    public int totalHitObjects;
    public int mostCommonBpm = -1;
    public double streamPercentage;
    public boolean isBpmChange;
    public double bpmChangePercentage;
    public boolean isComplex;
    public List<Stream> streams;
    public HashMap<Integer, Integer> bpmMap;
    public double complexPercentage;
    public HashMap<Integer, Integer> complexMap;
    public List<TimingPoint> timingPoints;
    public HashMap<Integer, Integer> changeMap;

    public boolean isAccepted = false;
}
