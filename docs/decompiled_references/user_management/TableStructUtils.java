package com.zkteconology.android.zkbiobt.CONTROLLER.service;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.zkteco.android.bluetooth.device.ZkBtDeviceType;
import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.TableStruct;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.MyApplication;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class TableStructUtils {
    private static final String NAME_TABLE_STRUCT = "TableStruct";
    private static final String TAG = "TableStructUtils";
    private static String WorkingTableStruct;

    @Deprecated
    public static String getTableStructByVersion(String str) {
        for (TableStruct tableStruct : parseJSON(getResString(R.raw.tablestruct))) {
            if (tableStruct.getFirmwareVersion().equals(str)) {
                ZKLog.d(TAG, "getTableStructByVersion: struct" + tableStruct.toString());
                return tableStruct.getTableStruct();
            }
        }
        return null;
    }

    public static String getTableStructByVersionFromCache(String str) {
        List<TableStruct> json = parseJSON(getResString(R.raw.tablestruct));
        if (json != null) {
            for (TableStruct tableStruct : json) {
                if (tableStruct.getFirmwareVersion().equals(str)) {
                    WorkingTableStruct = tableStruct.getTableStruct();
                    return WorkingTableStruct;
                }
            }
        }
        List<TableStruct> json2 = parseJSON(readDataFromStructFile(NAME_TABLE_STRUCT));
        if (json2 == null) {
            return null;
        }
        for (TableStruct tableStruct2 : json2) {
            if (tableStruct2.getFirmwareVersion().equals(str)) {
                WorkingTableStruct = tableStruct2.getTableStruct();
                return tableStruct2.getTableStruct();
            }
        }
        return null;
    }

    public static void saveTableStruct(int i, String str, String str2, String str3) {
        TableStruct tableStruct = new TableStruct(i, ZkBtDeviceType.getDeviceNameByCode(i), str, str2, str3);
        List json = parseJSON(readDataFromStructFile(NAME_TABLE_STRUCT));
        if (json == null) {
            json = new ArrayList();
        }
        json.add(tableStruct);
        writeData2StructFile(JSON.toJSONString(json));
        WorkingTableStruct = str3;
    }

    @Deprecated
    public static void initStructCache() {
        File fileStreamPath = MyApplication.getContext().getFileStreamPath(NAME_TABLE_STRUCT);
        if (fileStreamPath == null || fileStreamPath.exists()) {
            return;
        }
        ZKLog.d(TAG, "file not find. start init cache.");
        writeData2StructFile(getResString(R.raw.tablestruct));
    }

    private static boolean checkCache() {
        File fileStreamPath = MyApplication.getContext().getFileStreamPath(NAME_TABLE_STRUCT);
        if (fileStreamPath == null || fileStreamPath.exists()) {
            return true;
        }
        ZKLog.d(TAG, "file not find. start init cache.");
        writeData2StructFile("");
        return false;
    }

    private static void writeData2StructFile(String str) {
        try {
            FileOutputStream fileOutputStreamOpenFileOutput = MyApplication.getContext().openFileOutput(NAME_TABLE_STRUCT, 0);
            fileOutputStreamOpenFileOutput.write(str.getBytes());
            fileOutputStreamOpenFileOutput.close();
            ZKLog.d(TAG, "write TableStruct cache succeed");
        } catch (Exception e) {
            ZKLog.e(TAG, "open & write table struct file error", e);
        }
    }

    private static String readDataFromStructFile(String str) {
        if (!checkCache()) {
            return "";
        }
        try {
            FileInputStream fileInputStreamOpenFileInput = MyApplication.getContext().openFileInput(str);
            byte[] bArr = new byte[fileInputStreamOpenFileInput.available()];
            int i = fileInputStreamOpenFileInput.read(bArr);
            fileInputStreamOpenFileInput.close();
            return i > 0 ? new String(bArr) : "";
        } catch (Exception e) {
            ZKLog.e(TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    private static List<TableStruct> parseJSON(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        try {
            return JSON.parseArray(str, TableStruct.class);
        } catch (Exception unused) {
            return null;
        }
    }

    private static String getResString(int i) {
        InputStream inputStreamOpenRawResource = MyApplication.getContext().getResources().openRawResource(i);
        try {
            byte[] bArr = new byte[inputStreamOpenRawResource.available()];
            int i2 = inputStreamOpenRawResource.read(bArr);
            String str = new String(bArr);
            inputStreamOpenRawResource.close();
            return i2 > 0 ? str : "";
        } catch (Exception e) {
            ZKLog.e(TAG, Log.getStackTraceString(e));
            return "";
        }
    }

    public static String getWorkingTableStruct() {
        return WorkingTableStruct == null ? "" : WorkingTableStruct;
    }

    public static void setWorkingTableStruct(String str) {
        WorkingTableStruct = str;
    }
}
