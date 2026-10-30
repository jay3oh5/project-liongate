package com.zkteconology.android.zkbiobt.MODEL.database.biolock;

import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class BlUserAuthorize implements Serializable {
    private Integer AuthorizeDoorId;
    private Integer AuthorizeTimeZoneId;
    private Long Pin;

    public BlUserAuthorize() {
    }

    public BlUserAuthorize(Long l) {
        this.Pin = l;
    }

    public BlUserAuthorize(Long l, Integer num, Integer num2) {
        this.Pin = l;
        this.AuthorizeTimeZoneId = num;
        this.AuthorizeDoorId = num2;
    }

    public Long getPin() {
        return this.Pin;
    }

    public void setPin(Long l) {
        this.Pin = l;
    }

    public Integer getAuthorizeTimeZoneId() {
        return this.AuthorizeTimeZoneId;
    }

    public void setAuthorizeTimeZoneId(Integer num) {
        this.AuthorizeTimeZoneId = num;
    }

    public Integer getAuthorizeDoorId() {
        return this.AuthorizeDoorId;
    }

    public void setAuthorizeDoorId(Integer num) {
        this.AuthorizeDoorId = num;
    }

    /* JADX INFO: renamed from: clone, reason: merged with bridge method [inline-methods] */
    public BlUserAuthorize m12clone() {
        return new BlUserAuthorize(this.Pin, this.AuthorizeTimeZoneId, this.AuthorizeDoorId);
    }

    public String toString() {
        return "BlUserAuthorize{Pin=" + this.Pin + ", AuthorizeTimeZoneId=" + this.AuthorizeTimeZoneId + ", AuthorizeDoorId=" + this.AuthorizeDoorId + '}';
    }
}
