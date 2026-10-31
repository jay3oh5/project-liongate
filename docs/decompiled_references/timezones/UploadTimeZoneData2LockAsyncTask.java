package com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zkteco.android.pullsdk.PullController;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioBtDaoWrapper;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock.TimeZoneSelect;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockService;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUserAuthorize;
import com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class UploadTimeZoneData2LockAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private static String TAG = "com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.UploadTimeZoneData2LockAsyncTask";
    private Context mContext;
    private String mEndTimeString;
    private String mStartTimeString;
    private TimeZoneSelect mTimeZoneSelect;
    private long pullHandle;

    public UploadTimeZoneData2LockAsyncTask(Context context, long j, TimeZoneSelect timeZoneSelect, String str, String str2) {
        this.mContext = context;
        this.pullHandle = j;
        this.mTimeZoneSelect = timeZoneSelect;
        this.mStartTimeString = str;
        this.mEndTimeString = str2;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Boolean doInBackground(Void... voidArr) {
        Log.d(TAG, "-------- start upload -------");
        int iDecodeTimeString2Number = Utils.decodeTimeString2Number(this.mStartTimeString);
        int iDecodeTimeString2Number2 = Utils.decodeTimeString2Number(this.mEndTimeString);
        boolean zSaveTimeZone2Lock = BioLockService.saveTimeZone2Lock(this.pullHandle, iDecodeTimeString2Number, iDecodeTimeString2Number2, this.mTimeZoneSelect);
        Log.d(TAG, " upload starttime " + iDecodeTimeString2Number + " endTime " + iDecodeTimeString2Number2 + " mTimeZoneSelect " + this.mTimeZoneSelect + " result is " + zSaveTimeZone2Lock);
        boolean z = true;
        for (Map.Entry<Long, Integer> entry : BlTimeZoneActivity.mCurrentStateChangeMap.entrySet()) {
            boolean zSaveUserAuthorize2Lock = BioLockService.saveUserAuthorize2Lock(PullController.getPullHandle(), Long.valueOf(Long.parseLong(entry.getKey().toString())), entry.getValue().intValue());
            Log.d(TAG, " uploading UserAuthorize pin " + Long.parseLong(entry.getKey().toString()) + " timezoneId " + entry.getValue() + " result is " + zSaveUserAuthorize2Lock);
            z = zSaveUserAuthorize2Lock;
        }
        Log.d(TAG, "-------- end upload ------- ");
        if (zSaveTimeZone2Lock && z) {
            new BioBtDaoWrapper(BlUserAuthorize.class).deleteAll();
            Log.d(TAG, "------All upload success , clear database -------");
            BlTimeZoneActivity.mCurrentStateChangeMap.clear();
            BlTimeZoneActivity.isFirst = true;
        }
        return Boolean.valueOf(zSaveTimeZone2Lock && z);
    }
}
