package com.luzianu;

import com.luzianu.database.IntDoublePair;
import com.luzianu.database.TimingPoint;

import java.util.List;

public class Beatmap {
    public String artist;
    public String artistUnicode;
    public String title;
    public String titleUnicode;
    public String creator;
    public String difficulty;
    public String audioFileName;
    public String md5;
    public String nameOfOsuFile;
    public byte rankedStatus;
    public short numberOfHitcircles;
    public short numberOfSliders;
    public short numberOfSpinners;
    public long lastModificationTime;
    public float approachRate;
    public float circleSize;
    public float hpDrain;
    public float overallDifficulty;
    public double sliderVelocity;
    public List<IntDoublePair> starRatingStandard;
    public List<IntDoublePair> starRatingTaiko;
    public List<IntDoublePair> starRatingCtb;
    public List<IntDoublePair> starRatingMania;
    public int drainTimeInS;
    public int drainTimeInMs;
    public int audioPreview;
    public List<TimingPoint> timingPoints;
    public int difficultyId;
    public int beatmapId;
    public int threadId;
    public byte gradeInStandard;
    public byte gradeInTaiko;
    public byte gradeInCtb;
    public byte gradeInMania;
    public short localBeatmapOffset;
    public float stackLeniency;
    public byte mode;
    public String songSource;
    public String songTags;
    public short onlineOffset;
    public String fontUsedInTitle;
    public boolean isBeatmapUnplayed;
    public long lastTimeBeatmapPlayed;
    public boolean isBeatmapOsz2;
    public String folderName;
    public long lastTimeBeatmapChecked;
    public boolean ignoreBeatmapSound;
    public boolean ignoreBeatmapSkin;
    public boolean disableStoryboard;
    public boolean disableVideo;
    public boolean visualOverride;
    public byte maniaScrollSpeed;

    public Beatmap(String artist, String artistUnicode, String title, String titleUnicode, String creator,
                   String difficulty, String audioFileName, String md5, String nameOfOsuFile, byte rankedStatus,
                   short numberOfHitcircles, short numberOfSliders, short numberOfSpinners, long lastModificationTime,
                   float approachRate, float circleSize, float hpDrain, float overallDifficulty, double sliderVelocity,
                   List<IntDoublePair> starRatingStandard, List<IntDoublePair> starRatingTaiko,
                   List<IntDoublePair> starRatingCtb, List<IntDoublePair> starRatingMania, int drainTimeInS,
                   int drainTimeInMs, int audioPreview, List<TimingPoint> timingPoints, int difficultyId,
                   int beatmapId, int threadId, byte gradeInStandard, byte gradeInTaiko, byte gradeInCtb,
                   byte gradeInMania, short localBeatmapOffset, float stackLeniency, byte mode, String songSource,
                   String songTags, short onlineOffset, String fontUsedInTitle, boolean isBeatmapUnplayed,
                   long lastTimeBeatmapPlayed, boolean isBeatmapOsz2, String folderName, long lastTimeBeatmapChecked,
                   boolean ignoreBeatmapSound, boolean ignoreBeatmapSkin, boolean disableStoryboard,
                   boolean disableVideo, boolean visualOverride, byte maniaScrollSpeed) {
        this.artist = artist;
        this.artistUnicode = artistUnicode;
        this.title = title;
        this.titleUnicode = titleUnicode;
        this.creator = creator;
        this.difficulty = difficulty;
        this.audioFileName = audioFileName;
        this.md5 = md5;
        this.nameOfOsuFile = nameOfOsuFile;
        this.rankedStatus = rankedStatus;
        this.numberOfHitcircles = numberOfHitcircles;
        this.numberOfSliders = numberOfSliders;
        this.numberOfSpinners = numberOfSpinners;
        this.lastModificationTime = lastModificationTime;
        this.approachRate = approachRate;
        this.circleSize = circleSize;
        this.hpDrain = hpDrain;
        this.overallDifficulty = overallDifficulty;
        this.sliderVelocity = sliderVelocity;
        this.starRatingStandard = starRatingStandard;
        this.starRatingTaiko = starRatingTaiko;
        this.starRatingCtb = starRatingCtb;
        this.starRatingMania = starRatingMania;
        this.drainTimeInS = drainTimeInS;
        this.drainTimeInMs = drainTimeInMs;
        this.audioPreview = audioPreview;
        this.timingPoints = timingPoints;
        this.difficultyId = difficultyId;
        this.beatmapId = beatmapId;
        this.threadId = threadId;
        this.gradeInStandard = gradeInStandard;
        this.gradeInTaiko = gradeInTaiko;
        this.gradeInCtb = gradeInCtb;
        this.gradeInMania = gradeInMania;
        this.localBeatmapOffset = localBeatmapOffset;
        this.stackLeniency = stackLeniency;
        this.mode = mode;
        this.songSource = songSource;
        this.songTags = songTags;
        this.onlineOffset = onlineOffset;
        this.fontUsedInTitle = fontUsedInTitle;
        this.isBeatmapUnplayed = isBeatmapUnplayed;
        this.lastTimeBeatmapPlayed = lastTimeBeatmapPlayed;
        this.isBeatmapOsz2 = isBeatmapOsz2;
        this.folderName = folderName;
        this.lastTimeBeatmapChecked = lastTimeBeatmapChecked;
        this.ignoreBeatmapSound = ignoreBeatmapSound;
        this.ignoreBeatmapSkin = ignoreBeatmapSkin;
        this.disableStoryboard = disableStoryboard;
        this.disableVideo = disableVideo;
        this.visualOverride = visualOverride;
        this.maniaScrollSpeed = maniaScrollSpeed;
    }

    @Override
    public String toString() {
        return "Beatmap{" +
               "artist='" + artist + '\'' +
               ", artistUnicode='" + artistUnicode + '\'' +
               ", title='" + title + '\'' +
               ", titleUnicode='" + titleUnicode + '\'' +
               ", creator='" + creator + '\'' +
               ", difficulty='" + difficulty + '\'' +
               ", audioFileName='" + audioFileName + '\'' +
               ", md5='" + md5 + '\'' +
               ", nameOfOsuFile='" + nameOfOsuFile + '\'' +
               ", rankedStatus=" + rankedStatus +
               ", numberOfHitcircles=" + numberOfHitcircles +
               ", numberOfSliders=" + numberOfSliders +
               ", numberOfSpinners=" + numberOfSpinners +
               ", lastModificationTime=" + lastModificationTime +
               ", approachRate=" + approachRate +
               ", circleSize=" + circleSize +
               ", hpDrain=" + hpDrain +
               ", overallDifficulty=" + overallDifficulty +
               ", sliderVelocity=" + sliderVelocity +
               ", starRatingStandard=" + starRatingStandard +
               ", starRatingTaiko=" + starRatingTaiko +
               ", starRatingCtb=" + starRatingCtb +
               ", starRatingMania=" + starRatingMania +
               ", drainTimeInS=" + drainTimeInS +
               ", drainTimeInMs=" + drainTimeInMs +
               ", audioPreview=" + audioPreview +
               ", timingPoints=" + timingPoints +
               ", difficultyId=" + difficultyId +
               ", beatmapId=" + beatmapId +
               ", threadId=" + threadId +
               ", gradeInStandard=" + gradeInStandard +
               ", gradeInTaiko=" + gradeInTaiko +
               ", gradeInCtb=" + gradeInCtb +
               ", gradeInMania=" + gradeInMania +
               ", localBeatmapOffset=" + localBeatmapOffset +
               ", stackLeniency=" + stackLeniency +
               ", mode=" + mode +
               ", songSource='" + songSource + '\'' +
               ", songTags='" + songTags + '\'' +
               ", onlineOffset=" + onlineOffset +
               ", fontUsedInTitle='" + fontUsedInTitle + '\'' +
               ", isBeatmapUnplayed=" + isBeatmapUnplayed +
               ", lastTimeBeatmapPlayed=" + lastTimeBeatmapPlayed +
               ", isBeatmapOsz2=" + isBeatmapOsz2 +
               ", folderName='" + folderName + '\'' +
               ", lastTimeBeatmapChecked=" + lastTimeBeatmapChecked +
               ", ignoreBeatmapSound=" + ignoreBeatmapSound +
               ", ignoreBeatmapSkin=" + ignoreBeatmapSkin +
               ", disableStoryboard=" + disableStoryboard +
               ", disableVideo=" + disableVideo +
               ", visualOverride=" + visualOverride +
               ", maniaScrollSpeed=" + maniaScrollSpeed +
               '}';
    }
}
