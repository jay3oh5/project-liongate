package com.zkteconology.android.zkbiobt.MODEL.database.biolock;

/* JADX INFO: loaded from: classes.dex */
public class BlTimeZone {
    private Long FriTime1;
    private Long FriTime2;
    private Long FriTime3;
    private Long Hol1Time1;
    private Long Hol1Time2;
    private Long Hol1Time3;
    private Long Hol2Time1;
    private Long Hol2Time2;
    private Long Hol2Time3;
    private Long Hol3Time1;
    private Long Hol3Time2;
    private Long Hol3Time3;
    private Long MonTime1;
    private Long MonTime2;
    private Long MonTime3;
    private Long SatTime1;
    private Long SatTime2;
    private Long SatTime3;
    private Long SunTime1;
    private Long SunTime2;
    private Long SunTime3;
    private Long ThuTime1;
    private Long ThuTime2;
    private Long ThuTime3;
    private Integer TimeZoneId;
    private Long TueTime1;
    private Long TueTime2;
    private Long TueTime3;
    private Long WedTime1;
    private Long WedTime2;
    private Long WedTime3;
    private Long id;

    public BlTimeZone() {
    }

    public BlTimeZone(Long l) {
        this.id = l;
    }

    public BlTimeZone(Long l, Integer num, Long l2, Long l3, Long l4, Long l5, Long l6, Long l7, Long l8, Long l9, Long l10, Long l11, Long l12, Long l13, Long l14, Long l15, Long l16, Long l17, Long l18, Long l19, Long l20, Long l21, Long l22, Long l23, Long l24, Long l25, Long l26, Long l27, Long l28, Long l29, Long l30, Long l31) {
        this.id = l;
        this.TimeZoneId = num;
        this.SunTime1 = l2;
        this.SunTime2 = l3;
        this.SunTime3 = l4;
        this.MonTime1 = l5;
        this.MonTime2 = l6;
        this.MonTime3 = l7;
        this.TueTime1 = l8;
        this.TueTime2 = l9;
        this.TueTime3 = l10;
        this.WedTime1 = l11;
        this.WedTime2 = l12;
        this.WedTime3 = l13;
        this.ThuTime1 = l14;
        this.ThuTime2 = l15;
        this.ThuTime3 = l16;
        this.FriTime1 = l17;
        this.FriTime2 = l18;
        this.FriTime3 = l19;
        this.SatTime1 = l20;
        this.SatTime2 = l21;
        this.SatTime3 = l22;
        this.Hol1Time1 = l23;
        this.Hol1Time2 = l24;
        this.Hol1Time3 = l25;
        this.Hol2Time1 = l26;
        this.Hol2Time2 = l27;
        this.Hol2Time3 = l28;
        this.Hol3Time1 = l29;
        this.Hol3Time2 = l30;
        this.Hol3Time3 = l31;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long l) {
        this.id = l;
    }

    public Integer getTimeZoneId() {
        return this.TimeZoneId;
    }

    public void setTimeZoneId(Integer num) {
        this.TimeZoneId = num;
    }

    public Long getSunTime1() {
        return this.SunTime1;
    }

    public void setSunTime1(Long l) {
        this.SunTime1 = l;
    }

    public Long getSunTime2() {
        return this.SunTime2;
    }

    public void setSunTime2(Long l) {
        this.SunTime2 = l;
    }

    public Long getSunTime3() {
        return this.SunTime3;
    }

    public void setSunTime3(Long l) {
        this.SunTime3 = l;
    }

    public Long getMonTime1() {
        return this.MonTime1;
    }

    public void setMonTime1(Long l) {
        this.MonTime1 = l;
    }

    public Long getMonTime2() {
        return this.MonTime2;
    }

    public void setMonTime2(Long l) {
        this.MonTime2 = l;
    }

    public Long getMonTime3() {
        return this.MonTime3;
    }

    public void setMonTime3(Long l) {
        this.MonTime3 = l;
    }

    public Long getTueTime1() {
        return this.TueTime1;
    }

    public void setTueTime1(Long l) {
        this.TueTime1 = l;
    }

    public Long getTueTime2() {
        return this.TueTime2;
    }

    public void setTueTime2(Long l) {
        this.TueTime2 = l;
    }

    public Long getTueTime3() {
        return this.TueTime3;
    }

    public void setTueTime3(Long l) {
        this.TueTime3 = l;
    }

    public Long getWedTime1() {
        return this.WedTime1;
    }

    public void setWedTime1(Long l) {
        this.WedTime1 = l;
    }

    public Long getWedTime2() {
        return this.WedTime2;
    }

    public void setWedTime2(Long l) {
        this.WedTime2 = l;
    }

    public Long getWedTime3() {
        return this.WedTime3;
    }

    public void setWedTime3(Long l) {
        this.WedTime3 = l;
    }

    public Long getThuTime1() {
        return this.ThuTime1;
    }

    public void setThuTime1(Long l) {
        this.ThuTime1 = l;
    }

    public Long getThuTime2() {
        return this.ThuTime2;
    }

    public void setThuTime2(Long l) {
        this.ThuTime2 = l;
    }

    public Long getThuTime3() {
        return this.ThuTime3;
    }

    public void setThuTime3(Long l) {
        this.ThuTime3 = l;
    }

    public Long getFriTime1() {
        return this.FriTime1;
    }

    public void setFriTime1(Long l) {
        this.FriTime1 = l;
    }

    public Long getFriTime2() {
        return this.FriTime2;
    }

    public void setFriTime2(Long l) {
        this.FriTime2 = l;
    }

    public Long getFriTime3() {
        return this.FriTime3;
    }

    public void setFriTime3(Long l) {
        this.FriTime3 = l;
    }

    public Long getSatTime1() {
        return this.SatTime1;
    }

    public void setSatTime1(Long l) {
        this.SatTime1 = l;
    }

    public Long getSatTime2() {
        return this.SatTime2;
    }

    public void setSatTime2(Long l) {
        this.SatTime2 = l;
    }

    public Long getSatTime3() {
        return this.SatTime3;
    }

    public void setSatTime3(Long l) {
        this.SatTime3 = l;
    }

    public Long getHol1Time1() {
        return this.Hol1Time1;
    }

    public void setHol1Time1(Long l) {
        this.Hol1Time1 = l;
    }

    public Long getHol1Time2() {
        return this.Hol1Time2;
    }

    public void setHol1Time2(Long l) {
        this.Hol1Time2 = l;
    }

    public Long getHol1Time3() {
        return this.Hol1Time3;
    }

    public void setHol1Time3(Long l) {
        this.Hol1Time3 = l;
    }

    public Long getHol2Time1() {
        return this.Hol2Time1;
    }

    public void setHol2Time1(Long l) {
        this.Hol2Time1 = l;
    }

    public Long getHol2Time2() {
        return this.Hol2Time2;
    }

    public void setHol2Time2(Long l) {
        this.Hol2Time2 = l;
    }

    public Long getHol2Time3() {
        return this.Hol2Time3;
    }

    public void setHol2Time3(Long l) {
        this.Hol2Time3 = l;
    }

    public Long getHol3Time1() {
        return this.Hol3Time1;
    }

    public void setHol3Time1(Long l) {
        this.Hol3Time1 = l;
    }

    public Long getHol3Time2() {
        return this.Hol3Time2;
    }

    public void setHol3Time2(Long l) {
        this.Hol3Time2 = l;
    }

    public Long getHol3Time3() {
        return this.Hol3Time3;
    }

    public void setHol3Time3(Long l) {
        this.Hol3Time3 = l;
    }

    public BlTimeZone(int i) {
        this.TimeZoneId = Integer.valueOf(i);
        this.SunTime1 = 0L;
        this.MonTime1 = 0L;
        this.TueTime1 = 0L;
        this.WedTime1 = 0L;
        this.ThuTime1 = 0L;
        this.FriTime1 = 0L;
        this.SatTime1 = 0L;
    }
}
