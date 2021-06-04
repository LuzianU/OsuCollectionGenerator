package com.luzianu.beatmap;

public class TimingPoint {
    public int time;
    public double beatLength;
    public int meter = 4;
    public boolean uninherited = true;

    public TimingPoint(String[] arr) throws NumberFormatException {
        if (arr.length > 0) {
            // decimal to int
            if (arr[0].contains("."))
                arr[0] = arr[0].split("\\.")[0];
            time = Integer.parseInt(arr[0]);
        }
        if (arr.length > 1)
            beatLength = Double.parseDouble(arr[1]);
        if (arr.length > 2)
            meter = Integer.parseInt(arr[2]);
        if (arr.length > 6)
            if (arr[6].equals("0"))
                uninherited = false;
            else if (arr[6].equals("1"))
                uninherited = true;
            else
                System.err.println("peppy send help.");
    }

    public double getBpm() {
        ///return 240000.0 / meter / beatLength; idk what meter does it's literally not working
        return 60000 / beatLength;
    }

    @Override
    public String toString() {
        return "TimingPoint{" +
               "time=" + time +
               ", beatLength=" + beatLength +
               ", meter=" + meter +
               ", uninherited=" + uninherited +
               '}';
    }
}
