package com.luzianu;

import com.luzianu.beatmap.BeatmapInfo;
import com.luzianu.beatmap.BpmCounterPair;
import com.luzianu.beatmap.Stream;
import com.luzianu.beatmap.TimingPoint;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.luzianu.UserVariable.*;

public class Analyzer {
    /**
     * Analyzes the beatmap
     *
     * @param file .osu beatmap file
     * @return beatmapInfo with all it's calculated values
     */
    public static BeatmapInfo analyze(File file) {
        try {
            BeatmapInfo beatmapInfo = new BeatmapInfo();

            List<Integer> hitObjects = new ArrayList<>();
            List<TimingPoint> timingPoints = new ArrayList<>();

            // read all timing points and hit objects from the given .osu file
            try {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

                for (int i = lines.indexOf("[TimingPoints]") + 1; i < lines.size(); i++) {
                    String line = lines.get(i);

                    if (line.trim().isEmpty() || line.trim().startsWith("["))
                        break;

                    TimingPoint timingPoint = new TimingPoint(line.split(","));
                    if (timingPoint.uninherited) {
                        timingPoints.add(timingPoint);
                        //System.out.println("timing point added: " + timingPoint.getBpm());
                    }
                }

                beatmapInfo.timingPoints = timingPoints;
                // no timing points --> empty map --> stop
                if (timingPoints.size() == 0)
                    return beatmapInfo;

                for (int i = lines.indexOf("[HitObjects]") + 1; i < lines.size(); i++) {
                    String line = lines.get(i);

                    if (line.trim().isEmpty() || line.startsWith("["))
                        break;

                    int hitObject = Integer.parseInt(line.split(",")[2]);
                    hitObjects.add(hitObject);
                }
            } catch (Exception e) {
                System.err.println("could not read " + file.getName());
            }

            manageHitObjects(beatmapInfo, hitObjects,
                             Main.userVariables.get(MIN_STREAM_BPM).value,
                             Main.userVariables.get(MAX_STREAM_BPM).value);

            beatmapInfo = doSorting(beatmapInfo);

            // since streams with lower than minStreamBpm are disregarded
            // and the 1/3 streams could be overlooked, search for them again
            // but only if the most common bpm's 1/3 streams are below the minStreamBpm
            if (beatmapInfo.mostCommonBpm != -1 && beatmapInfo.mostCommonBpm * .75 * (1 - Main.userVariables.get(DELTA_VARIATION).value / 100.0) < Main.userVariables.get(MIN_STREAM_BPM).value) {
                int newMinStreamBpm = (int) (beatmapInfo.mostCommonBpm * .75 * (1 - Main.userVariables.get(DELTA_VARIATION).value / 100.0));

                manageHitObjects(beatmapInfo, hitObjects, newMinStreamBpm,
                                 Main.userVariables.get(MAX_STREAM_BPM).value);

                beatmapInfo = doSorting(beatmapInfo);
                beatmapInfo = checkIfComplex(beatmapInfo);
                beatmapInfo = checkIfChange(beatmapInfo);
            } else if (beatmapInfo.mostCommonBpm != -1) {
                beatmapInfo = checkIfComplex(beatmapInfo);
                beatmapInfo = checkIfChange(beatmapInfo);
            }

            beatmapInfo = sortOutMaps(beatmapInfo);

            return beatmapInfo;
        } catch (Exception e) {
            System.err.println("There was an error reading " + file.getName());
        }
        return null;
    }

    /**
     * Finds all streams of the given beatmap
     *
     * @param beatmapInfo  which map to calculate it for
     * @param hitObjects   list of the time in ms when each hit object appears. This list should be in ascending order.
     * @param minStreamBpm ignore streams below this threshold. This is used to ignore jumps.
     * @param maxStreamBpm ignore streams above this threshold
     * @return beatmapInfo with calculated streams and totalHitObject values
     */
    private static BeatmapInfo manageHitObjects(BeatmapInfo beatmapInfo, List<Integer> hitObjects,
                                                double minStreamBpm, double maxStreamBpm) {
        int timingPointIndex = 0; // unused
        int prevHitObject = -1;
        boolean isStream = false;
        Stream currentStream = null;
        ArrayList<Stream> streams = new ArrayList<>();

        for (int hitObject : hitObjects) {
            if (prevHitObject == -1) {
                prevHitObject = hitObject;
                continue;
            }

            // manages timingPointIndex
            while (timingPointIndex < beatmapInfo.timingPoints.size() - 1 && beatmapInfo.timingPoints.get(timingPointIndex + 1).time < hitObject)
                timingPointIndex++;

            // time in ms between this and previous hit object
            int delta = hitObject - prevHitObject;

            // calculate max and min delta between hit objects based on newMinStreamBpm
            int maxDelta = (int) (15000.0 / minStreamBpm);
            int minDelta = (int) (15000.0 / (maxStreamBpm == 0 ? Integer.MAX_VALUE : maxStreamBpm));

            // stream found between this and previous hit object
            if (delta <= maxDelta && delta >= minDelta) {
                if (!isStream) { // previous hit object is the first one of the stream
                    isStream = true;
                    currentStream = new Stream(delta, beatmapInfo.timingPoints.get(timingPointIndex).getBpm());
                    streams.add(currentStream);
                    currentStream.add(delta);
                } else { // all hit objects in between the start and end
                    if (checkIfInRange(currentStream.getFirstDelta(), delta)) { // same stream
                        currentStream.add(delta);
                    } else {
                        // last one of the stream
                        currentStream.addLastObject();
                        isStream = false;
                    }
                }
            } else if (isStream) { // this hit object is the last one of the stream
                isStream = false;
                currentStream.addLastObject();
            }

            prevHitObject = hitObject;
        }

        // band aid fix to fix a map ending with a stream
        if (isStream)
            currentStream.addLastObject();

        beatmapInfo.streams = streams;
        beatmapInfo.totalHitObjects = hitObjects.size();

        return beatmapInfo;
    }

    /**
     * Calculate the percentage of complex stream types and then add them all together with their respective multiplier
     * applied. If the sum is greater than or equal to the bpmComplexThreshold the map gets flagged as a complex map.
     * Note: This weighted percentage can get bigger than 100
     * Note: Only 1/3, 1/6, 1/8, 1/12, 1/16 streams are considered as complex.
     * This method should be called before {@link #checkIfChange(BeatmapInfo)}
     *
     * @param beatmapInfo which map to calculate it for
     * @return beatmapInfo with calculated complexPercentage, complexMap and isComplex values
     */
    private static BeatmapInfo checkIfComplex(BeatmapInfo beatmapInfo) {
        double complexPercentage = 0;
        int totalStreamHitObjects = 0;
        for (Stream stream : beatmapInfo.streams) {
            totalStreamHitObjects += stream.getTotalHitObjects();
        }

        HashMap<Integer, Integer> complexMap = new HashMap<>();

        for (Stream stream : beatmapInfo.streams) {
            // ignore stream with most common bpm. Without this maps like Reol - Think Alone are determined as complex.
            if (checkIfInRange(stream.getBpm(), beatmapInfo.mostCommonBpm))
                continue;

            // check for complex streams and manage them
            if (checkIfInRange(stream.getBpm(), stream.getBaseBpm() * 3 / 4.0)) {
                stream.setStreamType(Stream.StreamType.THIRD);
                int key = (int) (stream.getBaseBpm() * 3 / 4.0);
                int count = complexMap.getOrDefault(key, 0);
                complexMap.put(key, count + stream.getTotalHitObjects());
                complexPercentage += Main.userVariables.get(WEIGHT_3).value * stream.getTotalHitObjects() / totalStreamHitObjects;
            } else if (checkIfInRange(stream.getBpm(), stream.getBaseBpm() * 6 / 4.0)) {
                stream.setStreamType(Stream.StreamType.SIXTH);
                int key = (int) (stream.getBaseBpm() * 6 / 4.0);
                int count = complexMap.getOrDefault(key, 0);
                complexMap.put(key, count + stream.getTotalHitObjects());
                complexPercentage += Main.userVariables.get(WEIGHT_6).value * stream.getTotalHitObjects() / totalStreamHitObjects;
            } else if (checkIfInRange(stream.getBpm(), stream.getBaseBpm() * 8 / 4.0)) {
                stream.setStreamType(Stream.StreamType.EIGHTH);
                int key = (int) (stream.getBaseBpm() * 8 / 4.0);
                int count = complexMap.getOrDefault(key, 0);
                complexMap.put(key, count + stream.getTotalHitObjects());
                complexPercentage += Main.userVariables.get(WEIGHT_8).value * stream.getTotalHitObjects() / totalStreamHitObjects;
            } else if (checkIfInRange(stream.getBpm(), stream.getBaseBpm() * 12 / 4.0)) {
                stream.setStreamType(Stream.StreamType.TWELFTH);
                int key = (int) (stream.getBaseBpm() * 12 / 4.0);
                int count = complexMap.getOrDefault(key, 0);
                complexMap.put(key, count + stream.getTotalHitObjects());
                complexPercentage += Main.userVariables.get(WEIGHT_12).value * stream.getTotalHitObjects() / totalStreamHitObjects;
            } else if (checkIfInRange(stream.getBpm(), stream.getBaseBpm() * 16 / 4.0)) {
                stream.setStreamType(Stream.StreamType.SIXTEENTH);
                int key = (int) (stream.getBaseBpm() * 16 / 4.0);
                int count = complexMap.getOrDefault(key, 0);
                complexMap.put(key, count + stream.getTotalHitObjects());
                complexPercentage += Main.userVariables.get(WEIGHT_16).value * stream.getTotalHitObjects() / totalStreamHitObjects;
            }
        }

        complexPercentage *= 100;

        beatmapInfo.complexPercentage = complexPercentage;
        beatmapInfo.complexMap = complexMap;

        beatmapInfo.isComplex = complexPercentage >= Main.userVariables.get(BPM_COMPLEX_THRESHOLD).value;

        return beatmapInfo;
    }

    /**
     * Calculates the percentage of all non-complex and non-most-common bpm streams.
     * If the sum is greater than or equal to the bpmChangeThreshold the map gets flagged as a change map.
     *
     * @param beatmapInfo which map to calculate it for
     * @return beatmapInfo with calculated bpmChangePercentage, changeMap and isBpmChange values
     */
    private static BeatmapInfo checkIfChange(BeatmapInfo beatmapInfo) {
        boolean isBpmChange = false;
        double bpmChangePercentage = 0;
        int totalChangeHitObjects = 0;
        HashMap<Integer, Integer> changeMap = new HashMap<>();
        BpmCounterPair mostCommonChangeBpm = null;

        // changeMap = (bpmMap \ complexMap) \
        for (int streamBpm : beatmapInfo.bpmMap.keySet()) { // all streams
            boolean found = false;
            for (int complexBpm : beatmapInfo.complexMap.keySet()) { // complex streams
                if (checkIfInRange(streamBpm, complexBpm) &&
                    beatmapInfo.bpmMap.get(streamBpm).equals(beatmapInfo.complexMap.get(complexBpm))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changeMap.put(streamBpm, beatmapInfo.bpmMap.get(streamBpm));
                totalChangeHitObjects += beatmapInfo.bpmMap.get(streamBpm);
                if (mostCommonChangeBpm == null || mostCommonChangeBpm.counter < beatmapInfo.bpmMap.get(streamBpm)) {
                    mostCommonChangeBpm = new BpmCounterPair(streamBpm, beatmapInfo.bpmMap.get(streamBpm));
                }
            }

        }

        for (int changeBpm : changeMap.keySet()) {
            if (changeBpm == mostCommonChangeBpm.bpm)  // exclude most common one
                continue;
            bpmChangePercentage += 100.0 * changeMap.get(changeBpm) / totalChangeHitObjects;
        }

        if (bpmChangePercentage >= Main.userVariables.get(BPM_CHANGE_THRESHOLD).value)
            isBpmChange = true;

        beatmapInfo.bpmChangePercentage = bpmChangePercentage;
        beatmapInfo.changeMap = changeMap;
        beatmapInfo.isBpmChange = isBpmChange;

        return beatmapInfo;
    }

    /**
     * Checks if the values are within the deltaVariation margin
     *
     * @param actualValue actual value
     * @param targetValue target value
     * @return if the actualValue is within deltaVariation percent of the targetValue
     */
    private static boolean checkIfInRange(int actualValue, double targetValue) {
        return actualValue * (1 + Main.userVariables.get(DELTA_VARIATION).value / 100.0) > targetValue &&
               actualValue * (1 - Main.userVariables.get(DELTA_VARIATION).value / 100.0) < targetValue;
    }

    /**
     * Sorts all streams and groups the ones within deltaVariation together.
     * This method should be called before {@link #checkIfComplex(BeatmapInfo)} and {@link #checkIfChange(BeatmapInfo)}
     *
     * @param beatmapInfo which map to calculate it for
     * @return beatmapInfo with calculated mostCommonBpm, bpmMap and bpmPercentage values
     */
    private static BeatmapInfo doSorting(BeatmapInfo beatmapInfo) {
        HashMap<Integer, Integer> unsortedBpmMap = new HashMap<>();

        // count the streams
        int totalStreamHitObjects = 0;
        for (Stream stream : beatmapInfo.streams) {
            int count = unsortedBpmMap.getOrDefault(stream.getBpm(), 0);
            unsortedBpmMap.put(stream.getBpm(), count + stream.getTotalHitObjects());
            totalStreamHitObjects += stream.getTotalHitObjects();
        }

        // now sort the hash map again but this time group bpms within a margin of x%
        List<BpmCounterPair> orderedBpmCounterPairs = new ArrayList<>();
        unsortedBpmMap.entrySet().stream().sorted((k1, k2) -> k2.getValue().compareTo(k1.getValue()))
                      .forEach(k -> orderedBpmCounterPairs.add(new BpmCounterPair(k.getKey(), k.getValue())));

        // o^2 should be fine
        HashMap<Integer, Integer> bpmMap = new HashMap<>();
        for (BpmCounterPair x : orderedBpmCounterPairs) {
            if (x.counter == 0)
                continue;
            int currentBpm = x.bpm;
            for (BpmCounterPair y : orderedBpmCounterPairs) {
                if (y.counter == 0)
                    continue;

                // check if inside the x% margin
                if (checkIfInRange(currentBpm, y.bpm)) {
                    int count = bpmMap.getOrDefault(currentBpm, 0);
                    bpmMap.put(currentBpm, count + y.counter);
                    y.counter = 0;
                }
            }
        }

        // order again
        List<BpmCounterPair> ordered = new ArrayList<>();
        bpmMap.entrySet().stream().sorted((k1, k2) -> k2.getValue().compareTo(k1.getValue()))
              .forEach(k -> ordered.add(new BpmCounterPair(k.getKey(), k.getValue())));

        // calculate stream percentage
        double streamPercentage = Math.min(100, Math.max(0, 100.0 * totalStreamHitObjects / beatmapInfo.totalHitObjects));

        if (ordered.size() > 0)
            beatmapInfo.mostCommonBpm = ordered.get(0).bpm;

        beatmapInfo.bpmMap = bpmMap;
        beatmapInfo.streamPercentage = streamPercentage;

        return beatmapInfo;
    }

    /**
     * Sets the isAccepted value of the beatmapInfo if the map should be put into one of the collections
     *
     * @param beatmapInfo which map to calculate it for
     * @return beatmapInfo with the calculated isAccepted value
     */
    public static BeatmapInfo sortOutMaps(BeatmapInfo beatmapInfo) {
        // sort out stream percentage
        if (beatmapInfo.streamPercentage > Main.userVariables.get(MAX_STREAM_PERCENTAGE).value ||
            beatmapInfo.streamPercentage < Main.userVariables.get(MIN_STREAM_PERCENTAGE).value)
            return beatmapInfo;

        // sort out stream length ONLY if the map is not complex
        if (!beatmapInfo.isComplex) {
            boolean foundMin = false;
            for (Stream stream : beatmapInfo.streams) {
                if (stream.getTotalHitObjects() > ((Main.userVariables.get(MAX_STREAM_LENGTH_ALL).value == 0) ?
                        Integer.MAX_VALUE : Main.userVariables.get(MAX_STREAM_LENGTH_ALL).value))
                    return beatmapInfo;
                if (stream.getTotalHitObjects() >= Main.userVariables.get(MIN_STREAM_LENGTH_AT_LEAST).value) {
                    foundMin = true;
                }
            }
            if (!foundMin)
                return beatmapInfo;
        }

        beatmapInfo.isAccepted = true;

        return beatmapInfo;
    }
}
