package com.luzianu.io;

import com.luzianu.Beatmap;
import com.luzianu.UserInterface;
import com.luzianu.database.IntDoublePair;
import com.luzianu.database.TimingPoint;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.luzianu.io.Input.*;

public class OsuDb {

    public int version;
    public int folderCount;
    public boolean accountUnlocked;
    public String playerName;
    public List<Beatmap> beatmaps = new ArrayList<>();

    @Override
    public String toString() {
        return "OsuDb{" +
               "version=" + version +
               ", folderCount=" + folderCount +
               ", accountUnlocked=" + accountUnlocked +
               ", playerName='" + playerName + '\'' +
               ", numberOfBeatmaps=" + beatmaps.size() +
               '}';
    }

    public static class Reader {
        public static OsuDb read(File file, UserInterface ui) throws IOException {
            OsuDb osuDb = new OsuDb();

            long allocatedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            long presumableFreeMemory = Runtime.getRuntime().maxMemory() - allocatedMemory;
            byte[] buffer = null;
            if (file.length() < presumableFreeMemory * .9) {
                try {
                    buffer = new byte[(int) file.length()];
                    try (FileInputStream fis = new FileInputStream(file)) {
                        fis.read(buffer);
                    }
                } catch (OutOfMemoryError ignored) {
                    System.err.println("osu!.db file is too big to be loaded into memory");
                }
            }

            try (InputStream is = buffer != null ? new ByteArrayInputStream(buffer) : new FileInputStream(file)) {
                int version = readInt(is);
                osuDb.version = version;

                int folderCount = readInt(is);
                osuDb.folderCount = folderCount;

                boolean accountUnlocked = readBoolean(is);
                osuDb.accountUnlocked = accountUnlocked;

                skipDateTime(is);

                String playerName = readString(is);
                osuDb.playerName = playerName;

                int numberOfBeatmaps = readInt(is);

                int percentage = 0;
                for (int i = 0; i < numberOfBeatmaps; i++) {
                    try {
                        Beatmap beatmap = readBeatmap(is, version);
                        osuDb.beatmaps.add(beatmap);
                        if (percentage < (int) (100.0 * i / numberOfBeatmaps)) {
                            percentage = (int) (100.0 * i / numberOfBeatmaps);
                            ui.setReadProgressbarValue(percentage);
                            //System.out.println(percentage + "%");
                        }
                    } catch (IOException e) {
                        System.err.println("could not read beatmap at index " + i);
                    }
                }

                System.out.println(osuDb);
            }

            ui.swapToButtonGenerate();

            return osuDb;
        }

        private static Beatmap readBeatmap(InputStream is, int version) throws IOException {
            int sizeInBytes = -1;
            if (version < 20191106) {
                sizeInBytes = readInt(is);
            }

            String artist = readString(is);

            String artistUnicode = readString(is);

            String title = readString(is);

            String titleUnicode = readString(is);

            String creator = readString(is);

            String difficulty = readString(is);

            String audioFileName = readString(is);

            String md5 = readString(is);

            String nameOfOsuFile = readString(is);
            nameOfOsuFile = nameOfOsuFile.replaceAll("[<>:\"/\\\\|?*].*", "").trim();

            byte rankedStatus = readByte(is);

            short numberOfHitcircles = readShort(is);

            short numberOfSliders = readShort(is);

            short numberOfSpinners = readShort(is);

            long lastModificationTime = readLong(is);

            float approachRate;
            float circleSize;
            float hpDrain;
            float overallDifficulty;
            if (version < 20140609) {
                approachRate = readByte(is);
                circleSize = readByte(is);
                hpDrain = readByte(is);
                overallDifficulty = readByte(is);
            } else {
                approachRate = readSingle(is);
                circleSize = readSingle(is);
                hpDrain = readSingle(is);
                overallDifficulty = readSingle(is);
            }

            double sliderVelocity = readDouble(is);

            List<IntDoublePair> starRatingStandard = new ArrayList<>();
            if (version >= 20140609) {
                int followingIntDoublePairs = readInt(is);
                for (int j = 0; j < followingIntDoublePairs; j++) {
                    starRatingStandard.add(readIntDoublePair(is));
                }
            }
            List<IntDoublePair> starRatingTaiko = new ArrayList<>();
            if (version >= 20140609) {
                int followingIntDoublePairs = readInt(is);
                for (int j = 0; j < followingIntDoublePairs; j++) {
                    starRatingStandard.add(readIntDoublePair(is));
                }
            }
            List<IntDoublePair> starRatingCtb = new ArrayList<>();
            if (version >= 20140609) {
                int followingIntDoublePairs = readInt(is);
                for (int j = 0; j < followingIntDoublePairs; j++) {
                    starRatingStandard.add(readIntDoublePair(is));
                }
            }
            List<IntDoublePair> starRatingMania = new ArrayList<>();
            if (version >= 20140609) {
                int followingIntDoublePairs = readInt(is);
                for (int j = 0; j < followingIntDoublePairs; j++) {
                    starRatingStandard.add(readIntDoublePair(is));
                }
            }

            int drainTimeInS = readInt(is);

            int drainTimeInMs = readInt(is);

            int audioPreview = readInt(is);

            List<TimingPoint> timingPoints = new ArrayList<>();
            int followingTimingPoints = readInt(is);
            for (int j = 0; j < followingTimingPoints; j++) {
                timingPoints.add(readTimingPoint(is));
            }

            int difficultyId = readInt(is);

            int beatmapId = readInt(is);

            int threadId = readInt(is);

            byte gradeInStandard = readByte(is);

            byte gradeInTaiko = readByte(is);

            byte gradeInCtb = readByte(is);

            byte gradeInMania = readByte(is);

            short localBeatmapOffset = readShort(is);

            float stackLeniency = readSingle(is);

            byte mode = readByte(is);

            String songSource = readString(is);

            String songTags = readString(is);

            short onlineOffset = readShort(is);

            String fontUsedInTitle = readString(is);

            boolean isBeatmapUnplayed = readBoolean(is);

            long lastTimeBeatmapPlayed = readLong(is);

            boolean isBeatmapOsz2 = readBoolean(is);

            String folderName = readString(is);
            folderName = folderName.replaceAll("[<>:\"/\\\\|?*].*", "").trim();

            long lastTimeBeatmapChecked = readLong(is);

            boolean ignoreBeatmapSound = readBoolean(is);

            boolean ignoreBeatmapSkin = readBoolean(is);

            boolean disableStoryboard = readBoolean(is);

            boolean disableVideo = readBoolean(is);

            boolean visualOverride = readBoolean(is);

            if (version < 20140609) {
                short unknown = readShort(is);
            }

            int unknown = readInt(is);
            byte maniaScrollSpeed = readByte(is);

            Beatmap beatmap = new Beatmap(artist, artistUnicode, title, titleUnicode, creator, difficulty, audioFileName,
                                          md5, nameOfOsuFile, rankedStatus, numberOfHitcircles, numberOfSliders,
                                          numberOfSpinners, lastModificationTime, approachRate, circleSize, hpDrain,
                                          overallDifficulty, sliderVelocity, starRatingStandard, starRatingTaiko,
                                          starRatingCtb, starRatingMania, drainTimeInS, drainTimeInMs, audioPreview,
                                          timingPoints, difficultyId, beatmapId, threadId, gradeInStandard, gradeInTaiko,
                                          gradeInCtb, gradeInMania, localBeatmapOffset, stackLeniency, mode, songSource,
                                          songTags, onlineOffset, fontUsedInTitle, isBeatmapUnplayed,
                                          lastTimeBeatmapPlayed, isBeatmapOsz2, folderName, lastTimeBeatmapChecked,
                                          ignoreBeatmapSound, ignoreBeatmapSkin, disableStoryboard, disableVideo,
                                          visualOverride, maniaScrollSpeed);

            return beatmap;
        }
    }
}
