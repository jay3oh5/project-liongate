package com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock;

import android.util.Log;
import com.zkteco.android.bluetooth.ZKBluetoothFactory;
import com.zkteco.android.pullsdk.PullController;
import com.zkteco.android.pullsdk.PullHelper;
import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock.TimeZoneSelect;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.SharedPreferencesManager;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTimeZone;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTransaction;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUserAuthorize;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BioLockService {
    private static final int DEFAULT_ARRAY_UNIT = 32;
    private static final int DEFAULT_BUFFER_LENGTH = 4096;
    private static final String DEFAULT_FIELD_NAME = "*";
    private static final String DEFAULT_PULL_PASSWORD = "12312124234";
    private static final String TABLE_TIME_ZONE = "timezone";
    private static final String TABLE_TRANSACTION = "transaction";
    private static final String TABLE_USER = "user";
    private static final String TABLE_USER_AUTHORIZE = "userauthorize";
    private static final String TAG = "BioLockService";

    private static void cleanPullData() {
        ZKBluetoothFactory.getBluetoothManagerBLE().cleanPullDataBuffer();
    }

    public static long connect(String str) {
        cleanPullData();
        String bluetoothAddress = ZKBluetoothFactory.getZKBluetoothManager().getBluetoothAddress();
        if (str == null || str.trim().length() == 0) {
            str = DEFAULT_PULL_PASSWORD;
        }
        return PullController.connectPullByBluetooth(str + "\n" + bluetoothAddress + "-" + SharedPreferencesManager.getInstallMark() + "\n", 2000);
    }

    public static BlUser getCurrentUser(long j) {
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        PullHelper.getDeviceParam(j, bArr, iArr, "UserType,UserName,UserPin");
        String strTrim = new String(bArr).trim();
        ZKLog.d(TAG, strTrim);
        return BioLockHelper.decodeBioUser(strTrim);
    }

    public static String getDeviceVersion(long j) {
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        if (PullHelper.getDeviceParam(j, bArr, iArr, "FirmVer") < 0) {
            return "";
        }
        String strTrim = new String(bArr).trim();
        ZKLog.d(TAG, strTrim);
        if (strTrim.contains("FirmVer=Ver ")) {
            return strTrim.replace("FirmVer=Ver ", "").trim();
        }
        return null;
    }

    public static String getTableStruct(long j) {
        return PullHelper.GetTableStruct(j);
    }

    public static boolean setTableStruct(long j, String str) {
        return PullHelper.SetTableStruct(j, str) == 0;
    }

    public static boolean syncTime(long j) {
        cleanPullData();
        StringBuilder sb = new StringBuilder();
        sb.append("SysTime=");
        sb.append(BioLockHelper.getTimeMillisFrom2K());
        sb.append(",TimeZone=");
        sb.append(BioLockHelper.getTimeZone1());
        sb.append(",TimeZoneMin=");
        sb.append(BioLockHelper.getTimeZoneMin());
        return PullHelper.SetDeviceParam(j, sb.toString()) == 0;
    }

    public static List<BlUser> getUserInfo(long j, boolean z) {
        cleanPullData();
        byte[] bArr = new byte[5120];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        int iGetDeviceData = PullHelper.GetDeviceData(j, bArr, iArr, TABLE_USER, DEFAULT_FIELD_NAME, "", z ? "NewRecord" : "");
        String strTrim = new String(bArr).trim();
        ZKLog.d(TAG, "user count =" + iGetDeviceData);
        return BioLockHelper.decodeUserInfo(strTrim, TABLE_USER);
    }

    public static boolean deleteUser(long j, BlUser blUser) {
        cleanPullData();
        StringBuilder sb = new StringBuilder();
        sb.append("UID=");
        sb.append(blUser.getUID());
        return PullHelper.DeleteDeviceData(j, TABLE_USER, sb.toString(), "") == 0;
    }

    public static BlUserAuthorize getUserTimeZoneId(long j, int i) {
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        PullHelper.GetDeviceData(j, bArr, iArr, TABLE_USER_AUTHORIZE, DEFAULT_FIELD_NAME, "Pin=" + i, "");
        return BioLockHelper.decodeSingleUserAuthorize(new String(bArr).trim(), TABLE_USER_AUTHORIZE);
    }

    public static int openDoor(long j) {
        cleanPullData();
        ZKLog.d(TAG, "openDoor    handle is " + j);
        return PullHelper.controlDevice(j, 1L, 1L, 0L, 5L, 0L, "");
    }

    public static int saveUserData2Lock(long j, BlUser blUser) {
        cleanPullData();
        return PullHelper.SetDeviceData(j, TABLE_USER, BioLockHelper.getUserSaveString(blUser), "");
    }

    public static boolean saveUserAuthorize2Lock(long j, Long l, int i) {
        cleanPullData();
        StringBuilder sb = new StringBuilder();
        sb.append("Pin=");
        sb.append(l);
        sb.append("\tAuthorizeTimezoneId=");
        sb.append(i);
        return PullHelper.SetDeviceData(j, TABLE_USER_AUTHORIZE, sb.toString(), "") == 0;
    }

    public static List<BlTransaction> getRecordsFromLock(long j) {
        cleanPullData();
        byte[] bArr = new byte[8192];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        int iGetDeviceData = PullHelper.GetDeviceData(j, bArr, iArr, TABLE_TRANSACTION, DEFAULT_FIELD_NAME, "", "");
        String strTrim = new String(bArr).trim();
        Log.d(TAG, "ret = " + iGetDeviceData + "  my ans is --" + strTrim);
        return BioLockHelper.decodeLockRecodes(strTrim, TABLE_TRANSACTION);
    }

    public static boolean setTimeInterval(long j, long j2, long j3) {
        cleanPullData();
        String str = "LogStartTime=" + j2 + ",LogEndTime=" + j3;
        ZKLog.d(TAG, "setTimeInterval opera string is  --" + str);
        return PullHelper.SetDeviceParam(j, str) == 0;
    }

    public static BlTimeZone getTimeZoneById(long j, long j2) {
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        int iGetDeviceData = PullHelper.GetDeviceData(j, bArr, iArr, TABLE_TIME_ZONE, DEFAULT_FIELD_NAME, "TimezoneId=" + j2, "");
        String strTrim = new String(bArr).trim();
        ZKLog.d(TAG, strTrim);
        return BioLockHelper.decodeTimeZone(strTrim, TABLE_TIME_ZONE, (int) j2, iGetDeviceData);
    }

    public static boolean saveTimeZone2Lock(long j, int i, int i2, TimeZoneSelect timeZoneSelect) {
        cleanPullData();
        long j2 = (i << 16) + i2;
        long j3 = timeZoneSelect.getIsSelectArray()[0] ? j2 : 0L;
        long j4 = timeZoneSelect.getIsSelectArray()[1] ? j2 : 0L;
        long j5 = timeZoneSelect.getIsSelectArray()[2] ? j2 : 0L;
        long j6 = timeZoneSelect.getIsSelectArray()[3] ? j2 : 0L;
        long j7 = timeZoneSelect.getIsSelectArray()[4] ? j2 : 0L;
        long j8 = timeZoneSelect.getIsSelectArray()[5] ? j2 : 0L;
        if (!timeZoneSelect.getIsSelectArray()[6]) {
            j2 = 0;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("TimezoneId=");
        sb.append(timeZoneSelect.getTimeZoneId());
        sb.append("\tSunTime1=");
        sb.append(j3);
        sb.append("\tMonTime1=");
        sb.append(j4);
        sb.append("\tTueTime1=");
        sb.append(j5);
        sb.append("\tWedTime1=");
        sb.append(j6);
        sb.append("\tThuTime1=");
        sb.append(j7);
        sb.append("\tFriTime1=");
        sb.append(j8);
        sb.append("\tSatTime1=");
        sb.append(j2);
        sb.append("\t");
        return PullHelper.SetDeviceData(j, TABLE_TIME_ZONE, sb.toString(), "") == 0;
    }

    public static boolean changeSupperPwd(long j, String str) {
        StringBuilder sb = new StringBuilder();
        sb.append("SuperPwd=");
        sb.append(str);
        return PullHelper.SetDeviceParam(j, sb.toString()) == 0;
    }

    public static List<BlUserAuthorize> getUserAuthorizeList(long j) {
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        PullHelper.GetDeviceData(j, bArr, iArr, TABLE_USER_AUTHORIZE, DEFAULT_FIELD_NAME, "", "");
        return BioLockHelper.decodeUserAuthorize(new String(bArr).trim(), TABLE_USER_AUTHORIZE);
    }

    public static String getDeviceParam(long j, String str) {
        if (j == 0) {
            return null;
        }
        cleanPullData();
        byte[] bArr = new byte[4096];
        int[] iArr = new int[32];
        iArr[0] = bArr.length;
        Log.i(TAG, "getDeviceParam():BEFORE:" + str);
        int deviceParam = PullHelper.getDeviceParam(j, bArr, iArr, str);
        Log.i(TAG, "getDeviceParam():AFTER:" + str);
        if (deviceParam < 0) {
            return null;
        }
        String strTrim = new String(bArr).trim();
        Log.i(TAG, "getDeviceParam():" + strTrim);
        return strTrim;
    }
}
