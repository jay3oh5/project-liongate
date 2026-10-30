package com.zkteconology.android.zkbiobt.CONTROLLER.service;

import android.support.annotation.NonNull;
import com.alibaba.fastjson.JSON;
import com.zkteco.android.tool.ZKLog;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class PullFormatUtils {
    private static final String TAG = "PullFormatUtils";
    private List<Field> fieldList;

    public PullFormatUtils(@NonNull String str, String str2) {
        for (String str3 : str.split("\n")) {
            if (str3.startsWith(str2)) {
                this.fieldList = decodeTable(str3);
                return;
            }
        }
    }

    private List<Field> decodeTable(String str) {
        ArrayList arrayList = new ArrayList();
        String[] strArrSplit = str.split(",");
        for (int i = 1; i < strArrSplit.length; i++) {
            arrayList.add(new Field(strArrSplit[i]));
        }
        return arrayList;
    }

    public String transformPull2Json(String str) {
        String[] strArrSplit = str.split("\r\n");
        String[] strArrSplit2 = strArrSplit[0].split(",");
        StringBuilder sb = new StringBuilder("[");
        for (String str2 : strArrSplit) {
            if (!str2.equals(strArrSplit[0])) {
                String[] strArrSplit3 = str2.split(",", -2);
                sb.append("{");
                for (int i = 0; i < strArrSplit3.length; i++) {
                    Field mathFiled = getMathFiled(i, strArrSplit2[i]);
                    if (mathFiled != null) {
                        sb.append(mathFiled.getOutputNameString());
                        sb.append(":");
                        sb.append(mathFiled.getFormatValue(strArrSplit3[i]));
                        sb.append(",");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("},");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]");
        ZKLog.d(TAG, sb.toString());
        return sb.toString();
    }

    private Field getMathFiled(int i, String str) {
        if (str.equals(this.fieldList.get(i).getName())) {
            return this.fieldList.get(i);
        }
        for (Field field : this.fieldList) {
            if (str.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private class Field {
        private static final int TYPE_BASE64 = 3;
        private static final int TYPE_BASE64_LONG = 33;
        private static final int TYPE_INT = 1;
        private static final int TYPE_STRING = 2;
        private static final int TYPE_STRING_LONG = 22;
        String name;
        int type;

        public Field(String str) {
            String[] strArrSplit = str.split("=");
            this.name = strArrSplit[0];
            this.type = getDataType(strArrSplit[1].charAt(0));
        }

        private int getDataType(char c) {
            if (c == "i".charAt(0)) {
                return 1;
            }
            if (c == "s".charAt(0)) {
                return 2;
            }
            if (c == "S".charAt(0)) {
                return 22;
            }
            if (c == "b".charAt(0)) {
                return 3;
            }
            return c == "B".charAt(0) ? 33 : -1;
        }

        public String getFormatValue(String str) {
            if (this.type == 1) {
                return str;
            }
            return "\"" + str + "\"";
        }

        /* JADX INFO: Access modifiers changed from: private */
        public String getName() {
            return this.name;
        }

        public String getOutputNameString() {
            return "\"" + this.name + "\"";
        }
    }

    public static String decode(String str, String str2) {
        try {
            if (str.split("\r\n").length == 1) {
                return null;
            }
            return new PullFormatUtils(TableStructUtils.getWorkingTableStruct(), str2).transformPull2Json(str);
        } catch (Exception e) {
            ZKLog.e(TAG, "change pull format 2 json error", e);
            return null;
        }
    }

    public static String decodeSingle(String str, String str2) {
        String strDecode = decode(str, str2);
        if (strDecode == null) {
            return strDecode;
        }
        String strReplace = strDecode.replace("[", "").replace("]", "");
        ZKLog.d(TAG, strReplace);
        return strReplace;
    }

    public static <T> List<T> decodeHelper(@NonNull String str, @NonNull String str2, @NonNull Class<T> cls) {
        if (str.split("\r\n").length == 1) {
            return null;
        }
        try {
            return JSON.parseArray(new PullFormatUtils(TableStructUtils.getWorkingTableStruct(), str2).transformPull2Json(str), cls);
        } catch (Exception unused) {
            return null;
        }
    }
}
