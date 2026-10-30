package com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock;

import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockHelper;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTransaction;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;

/* JADX INFO: loaded from: classes.dex */
public class UnlockRecord {
    private int cardNo;
    private int doorId;
    private int eventType;
    private int inOutState;
    private long index;
    private String name;
    private int pin;
    private int privilege;
    private int superAuthorize;
    private long timeSecond;
    private String timeSecondString;
    private long uid;
    private int verified;

    public UnlockRecord(BlTransaction blTransaction, BlUser blUser) {
        this.index = blTransaction.getIndex().longValue();
        this.cardNo = blTransaction.getCardNo().intValue();
        this.verified = blTransaction.getVerified().intValue();
        this.doorId = blTransaction.getDoorId().intValue();
        this.eventType = blTransaction.getEventType().intValue();
        this.inOutState = blTransaction.getInOutState().intValue();
        this.timeSecond = blTransaction.getTime_Second().longValue();
        this.timeSecondString = BioLockHelper.getTimeString(this.timeSecond);
        if (blUser != null) {
            this.uid = blUser.getUID().longValue();
            this.pin = blUser.getPin().intValue();
            this.name = blUser.getName();
            this.privilege = blUser.getPrivilege().intValue();
            this.superAuthorize = blUser.getSuperAuthorize().intValue();
            return;
        }
        this.uid = 0L;
        this.pin = 0;
        this.name = "";
        this.privilege = 0;
        this.superAuthorize = 0;
    }

    public UnlockRecord(long j, int i, int i2, int i3, int i4, int i5, int i6, long j2, int i7, String str, int i8, int i9) {
        this.index = j;
        this.cardNo = i;
        this.pin = i2;
        this.verified = i3;
        this.doorId = i4;
        this.eventType = i5;
        this.inOutState = i6;
        this.timeSecond = j2;
        this.uid = i7;
        this.name = str;
        this.privilege = i8;
        this.superAuthorize = i9;
    }

    public String toString() {
        return "UnlockRecord{index=" + this.index + ", cardNo=" + this.cardNo + ", pin=" + this.pin + ", verified=" + this.verified + ", doorId=" + this.doorId + ", eventType=" + this.eventType + ", inOutState=" + this.inOutState + ", timeSecond=" + this.timeSecond + ", uid=" + this.uid + ", name='" + this.name + "', privilege=" + this.privilege + ", superAuthorize=" + this.superAuthorize + '}';
    }

    public long getIndex() {
        return this.index;
    }

    public void setIndex(long j) {
        this.index = j;
    }

    public int getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(int i) {
        this.cardNo = i;
    }

    public int getPin() {
        return this.pin;
    }

    public void setPin(int i) {
        this.pin = i;
    }

    public int getVerified() {
        return this.verified;
    }

    public void setVerified(int i) {
        this.verified = i;
    }

    public int getDoorId() {
        return this.doorId;
    }

    public void setDoorId(int i) {
        this.doorId = i;
    }

    public int getEventType() {
        return this.eventType;
    }

    public void setEventType(int i) {
        this.eventType = i;
    }

    public int getInOutState() {
        return this.inOutState;
    }

    public void setInOutState(int i) {
        this.inOutState = i;
    }

    public long getTimeSecond() {
        return this.timeSecond;
    }

    public void setTimeSecond(long j) {
        this.timeSecond = j;
    }

    public String getTimeSecondString() {
        return this.timeSecondString;
    }

    public void setTimeSecondString(String str) {
        this.timeSecondString = str;
    }

    public long getUid() {
        return this.uid;
    }

    public void setUid(long j) {
        this.uid = j;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public int getPrivilege() {
        return this.privilege;
    }

    public void setPrivilege(int i) {
        this.privilege = i;
    }

    public int getSuperAuthorize() {
        return this.superAuthorize;
    }

    public void setSuperAuthorize(int i) {
        this.superAuthorize = i;
    }
}
