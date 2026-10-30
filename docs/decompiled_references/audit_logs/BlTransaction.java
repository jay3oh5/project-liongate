package com.zkteconology.android.zkbiobt.MODEL.database.biolock;

/* JADX INFO: loaded from: classes.dex */
public class BlTransaction {
    private Integer CardNo;
    private Integer DoorId;
    private Integer EventType;
    private Integer InOutState;
    private Long Index;
    private Integer Pin;
    private Long Time_Second;
    private Integer Verified;

    public BlTransaction() {
    }

    public BlTransaction(Long l) {
        this.Index = l;
    }

    public BlTransaction(Long l, Integer num, Integer num2, Integer num3, Integer num4, Integer num5, Integer num6, Long l2) {
        this.Index = l;
        this.CardNo = num;
        this.Pin = num2;
        this.Verified = num3;
        this.DoorId = num4;
        this.EventType = num5;
        this.InOutState = num6;
        this.Time_Second = l2;
    }

    public Long getIndex() {
        return this.Index;
    }

    public void setIndex(Long l) {
        this.Index = l;
    }

    public Integer getCardNo() {
        return this.CardNo;
    }

    public void setCardNo(Integer num) {
        this.CardNo = num;
    }

    public Integer getPin() {
        return this.Pin;
    }

    public void setPin(Integer num) {
        this.Pin = num;
    }

    public Integer getVerified() {
        return this.Verified;
    }

    public void setVerified(Integer num) {
        this.Verified = num;
    }

    public Integer getDoorId() {
        return this.DoorId;
    }

    public void setDoorId(Integer num) {
        this.DoorId = num;
    }

    public Integer getEventType() {
        return this.EventType;
    }

    public void setEventType(Integer num) {
        this.EventType = num;
    }

    public Integer getInOutState() {
        return this.InOutState;
    }

    public void setInOutState(Integer num) {
        this.InOutState = num;
    }

    public Long getTime_Second() {
        return this.Time_Second;
    }

    public void setTime_Second(Long l) {
        this.Time_Second = l;
    }

    public String toString() {
        return "BlTransaction{Index=" + this.Index + ", CardNo=" + this.CardNo + ", Pin=" + this.Pin + ", Verified=" + this.Verified + ", DoorId=" + this.DoorId + ", EventType=" + this.EventType + ", InOutState=" + this.InOutState + ", Time_Second=" + this.Time_Second + '}';
    }
}
