package com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock;

import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.PullFormatUtils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTimeZone;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTransaction;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUserAuthorize;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/* JADX INFO: loaded from: classes.dex */
public class BioLockHelper {
    private static final String TAG = "BioLockHelper";

    public static String getUserSaveString(BlUser blUser) {
        return "UID=" + blUser.getUID() + "\tPin=" + blUser.getPin() + "\tName=" + blUser.getName() + "\tPrivilege=" + blUser.getPrivilege() + "\tMark=" + blUser.getMark();
    }

    public static long getTimeMillisFrom2K() {
        return System.currentTimeMillis() / 1000;
    }

    public static long getTimeMillisFrom2K(int[] iArr, boolean z) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(iArr[0], iArr[1] - 1, iArr[2]);
        long timeInMillis = calendar.getTimeInMillis() / 1000;
        if (z) {
            timeInMillis += 86399;
        }
        ZKLog.d(TAG, "getTimeMillisFrom2K   time = " + timeInMillis + "   data[0] = " + iArr[0] + "  data[1] =  " + iArr[1] + "   data[2] = " + iArr[2]);
        return timeInMillis;
    }

    public static String getTimeString(long j) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(j * 1000);
        String str = String.format(Locale.CHINA, "%04d/%02d/%02d %02d:%02d", Integer.valueOf(calendar.get(1)), Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)), Integer.valueOf(calendar.get(11)), Integer.valueOf(calendar.get(12)));
        ZKLog.d(TAG, "Time String is" + str);
        return str;
    }

    public static BlUser decodeBioUser(String str) {
        try {
            String[] strArrSplit = str.split(",");
            if (strArrSplit.length < 2) {
                return null;
            }
            BlUser blUser = new BlUser();
            blUser.setPrivilege(Integer.valueOf(Integer.parseInt(strArrSplit[0].replace("UserType=", ""))));
            blUser.setName(strArrSplit[1].replace("UserName=", ""));
            blUser.setPin(Integer.valueOf(Integer.parseInt(strArrSplit[2].replace("UserPin=", ""))));
            return blUser;
        } catch (Exception e) {
            ZKLog.e(TAG, "the data wrong", e);
            return null;
        }
    }

    public static List<BlUser> decodeUserInfo(String str, String str2) {
        List<BlUser> listDecodeHelper = PullFormatUtils.decodeHelper(str, str2, BlUser.class);
        if (listDecodeHelper != null) {
            Iterator<BlUser> it = listDecodeHelper.iterator();
            while (it.hasNext()) {
                ZKLog.d(TAG, it.next().toString());
            }
        }
        return listDecodeHelper;
    }

    public static List<BlTransaction> decodeLockRecodes(String str, String str2) {
        List<BlTransaction> listDecodeHelper = PullFormatUtils.decodeHelper(str, str2, BlTransaction.class);
        if (listDecodeHelper != null) {
            Iterator<BlTransaction> it = listDecodeHelper.iterator();
            while (it.hasNext()) {
                ZKLog.d(TAG, it.next().toString());
            }
        }
        return listDecodeHelper;
    }

    public static BlUserAuthorize decodeSingleUserAuthorize(String str, String str2) {
        List listDecodeHelper = PullFormatUtils.decodeHelper(str, str2, BlUserAuthorize.class);
        BlUserAuthorize blUserAuthorize = (listDecodeHelper == null || listDecodeHelper.size() <= 0) ? null : (BlUserAuthorize) listDecodeHelper.get(0);
        if (blUserAuthorize != null) {
            ZKLog.d(TAG, blUserAuthorize.toString());
        }
        return blUserAuthorize;
    }

    public static List<BlUserAuthorize> decodeUserAuthorize(String str, String str2) {
        List<BlUserAuthorize> listDecodeHelper = PullFormatUtils.decodeHelper(str, str2, BlUserAuthorize.class);
        if (listDecodeHelper != null) {
            Iterator<BlUserAuthorize> it = listDecodeHelper.iterator();
            while (it.hasNext()) {
                ZKLog.d(TAG, it.next().toString());
            }
        }
        return listDecodeHelper;
    }

    public static BlTimeZone decodeTimeZone(String str, String str2, int i, int i2) {
        if (isInvalidData(str)) {
            if (i2 != 0) {
                return null;
            }
            new BlTimeZone(Long.valueOf(i));
        }
        List listDecodeHelper = PullFormatUtils.decodeHelper(str, str2, BlTimeZone.class);
        BlTimeZone blTimeZone = (listDecodeHelper == null || listDecodeHelper.size() <= 0) ? null : (BlTimeZone) listDecodeHelper.get(0);
        if (blTimeZone != null) {
            ZKLog.d(TAG, blTimeZone.toString());
            return blTimeZone;
        }
        if (i2 != 0) {
            return null;
        }
        ZKLog.d(TAG, " TimeZone create empty object ");
        return new BlTimeZone(i);
    }

    public static int getTimeZone1() {
        TimeZone timeZone = TimeZone.getDefault();
        return timeZone.getOffset(GregorianCalendar.getInstance(timeZone).getTimeInMillis()) / 3600000;
    }

    public static int getTimeZoneMin() {
        TimeZone timeZone = TimeZone.getDefault();
        return Math.abs(timeZone.getOffset(GregorianCalendar.getInstance(timeZone).getTimeInMillis()) / 60000) % 60;
    }

    private static boolean isInvalidData(String str) {
        return str.split("\r\n").length <= 1;
    }
}
