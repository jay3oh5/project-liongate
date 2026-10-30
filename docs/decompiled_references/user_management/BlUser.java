package com.zkteconology.android.zkbiobt.MODEL.database.biolock;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class BlUser implements Serializable {
    private Integer CardNo;
    private Long EndTime;
    private Integer Group;
    private String Mark;
    private String Name;
    private String PassWord;
    private Integer Pin;
    private Integer Privilege;
    private Long StartTime;
    private Integer SuperAuthorize;
    private Long UID;

    public BlUser() {
    }

    public BlUser(Long l) {
        this.UID = l;
    }

    public BlUser(Long l, Integer num, Integer num2, String str, Integer num3, Long l2, Long l3, String str2, Integer num4, Integer num5, String str3) {
        this.UID = l;
        this.CardNo = num;
        this.Pin = num2;
        this.PassWord = str;
        this.Group = num3;
        this.StartTime = l2;
        this.EndTime = l3;
        this.Name = str2;
        this.Privilege = num4;
        this.SuperAuthorize = num5;
        this.Mark = str3;
    }

    public Long getUID() {
        return this.UID;
    }

    public void setUID(Long l) {
        this.UID = l;
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

    public String getPassWord() {
        return this.PassWord;
    }

    public void setPassWord(String str) {
        this.PassWord = str;
    }

    public Integer getGroup() {
        return this.Group;
    }

    public void setGroup(Integer num) {
        this.Group = num;
    }

    public Long getStartTime() {
        return this.StartTime;
    }

    public void setStartTime(Long l) {
        this.StartTime = l;
    }

    public Long getEndTime() {
        return this.EndTime;
    }

    public void setEndTime(Long l) {
        this.EndTime = l;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String str) {
        this.Name = str;
    }

    public Integer getPrivilege() {
        return this.Privilege;
    }

    public void setPrivilege(Integer num) {
        this.Privilege = num;
    }

    public Integer getSuperAuthorize() {
        return this.SuperAuthorize;
    }

    public void setSuperAuthorize(Integer num) {
        this.SuperAuthorize = num;
    }

    public String getMark() {
        return this.Mark;
    }

    public void setMark(String str) {
        this.Mark = str;
    }

    public String toString() {
        return "BlUser{UID=" + this.UID + ", CardNo=" + this.CardNo + ", Pin=" + this.Pin + ", PassWord='" + this.PassWord + "', Group=" + this.Group + ", StartTime=" + this.StartTime + ", EndTime=" + this.EndTime + ", Name='" + this.Name + "', Privilege=" + this.Privilege + ", SuperAuthorize=" + this.SuperAuthorize + ", Mark='" + this.Mark + "'}";
    }
}
