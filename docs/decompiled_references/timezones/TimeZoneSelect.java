package com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock;

import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTimeZone;
import java.util.Arrays;

/* JADX INFO: loaded from: classes.dex */
public class TimeZoneSelect {
    public static final int FRI = 5;
    public static final int MON = 1;
    public static final int SAT = 6;
    public static final int SUN = 0;
    public static final int THU = 4;
    public static final int TUE = 2;
    public static final int WED = 3;
    public boolean[] isSelectArray = new boolean[7];
    private String mEndTime;
    private String mStartTime;
    private int mTimeZoneId;

    public int getTimeZoneId() {
        return this.mTimeZoneId;
    }

    public boolean[] getIsSelectArray() {
        return this.isSelectArray;
    }

    public TimeZoneSelect(BlTimeZone blTimeZone, int i) {
        long jLongValue = 0;
        if (blTimeZone != null) {
            this.mTimeZoneId = blTimeZone.getTimeZoneId().intValue();
            this.isSelectArray[0] = blTimeZone.getSunTime1().longValue() > 0;
            this.isSelectArray[1] = blTimeZone.getMonTime1().longValue() > 0;
            this.isSelectArray[2] = blTimeZone.getTueTime1().longValue() > 0;
            this.isSelectArray[3] = blTimeZone.getWedTime1().longValue() > 0;
            this.isSelectArray[4] = blTimeZone.getThuTime1().longValue() > 0;
            this.isSelectArray[5] = blTimeZone.getFriTime1().longValue() > 0;
            this.isSelectArray[6] = blTimeZone.getSatTime1().longValue() > 0;
            if (blTimeZone.getSunTime1().longValue() > 0) {
                jLongValue = blTimeZone.getSunTime1().longValue();
            } else if (blTimeZone.getMonTime1().longValue() > 0) {
                jLongValue = blTimeZone.getMonTime1().longValue();
            } else if (blTimeZone.getTueTime1().longValue() > 0) {
                jLongValue = blTimeZone.getTueTime1().longValue();
            } else if (blTimeZone.getWedTime1().longValue() > 0) {
                jLongValue = blTimeZone.getWedTime1().longValue();
            } else if (blTimeZone.getThuTime1().longValue() > 0) {
                jLongValue = blTimeZone.getThuTime1().longValue();
            } else if (blTimeZone.getFriTime1().longValue() > 0) {
                jLongValue = blTimeZone.getFriTime1().longValue();
            } else if (blTimeZone.getSatTime1().longValue() > 0) {
                jLongValue = blTimeZone.getSatTime1().longValue();
            }
        } else {
            this.mTimeZoneId = i;
            this.isSelectArray[0] = false;
            this.isSelectArray[1] = false;
            this.isSelectArray[2] = false;
            this.isSelectArray[3] = false;
            this.isSelectArray[4] = false;
            this.isSelectArray[5] = false;
            this.isSelectArray[6] = false;
        }
        translateTime((int) jLongValue);
    }

    private void translateTime(int i) {
        Object objValueOf;
        Object objValueOf2;
        Object objValueOf3;
        Object objValueOf4;
        if (i == 0) {
            this.mStartTime = "00:00";
            this.mEndTime = this.mTimeZoneId == 2 ? "00:00" : "23:59";
            return;
        }
        int i2 = 65535 & i;
        StringBuilder sb = new StringBuilder();
        int i3 = i2 / 100;
        if (i3 < 10) {
            objValueOf = "0" + i3;
        } else {
            objValueOf = Integer.valueOf(i3);
        }
        sb.append(objValueOf);
        sb.append(":");
        int i4 = i2 % 100;
        if (i4 < 10) {
            objValueOf2 = "0" + i4;
        } else {
            objValueOf2 = Integer.valueOf(i4);
        }
        sb.append(objValueOf2);
        this.mEndTime = sb.toString();
        int i5 = (i - i2) >> 16;
        StringBuilder sb2 = new StringBuilder();
        int i6 = i5 / 100;
        if (i6 < 10) {
            objValueOf3 = "0" + i6;
        } else {
            objValueOf3 = Integer.valueOf(i6);
        }
        sb2.append(objValueOf3);
        sb2.append(":");
        int i7 = i5 % 100;
        if (i7 < 10) {
            objValueOf4 = "0" + i7;
        } else {
            objValueOf4 = Integer.valueOf(i7);
        }
        sb2.append(objValueOf4);
        this.mStartTime = sb2.toString();
    }

    public void setTimeZoneId(int i) {
        this.mTimeZoneId = i;
    }

    public String toString() {
        return "TimeZoneSelect{mTimeZoneId=" + this.mTimeZoneId + ", mStartTime='" + this.mStartTime + "', mEndTime='" + this.mEndTime + "', isSelectArray=" + Arrays.toString(this.isSelectArray) + '}';
    }

    public String getStartTime() {
        return this.mStartTime;
    }

    public String getEndTime() {
        return this.mEndTime;
    }
}
