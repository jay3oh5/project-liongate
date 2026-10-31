package com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock;

import android.os.AsyncTask;
import com.zkteco.android.pullsdk.PullController;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockService;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTimeZone;

/* JADX INFO: loaded from: classes.dex */
public class LoadTimeZoneDataAsyncTask extends AsyncTask<Integer, Void, BlTimeZone> {
    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public BlTimeZone doInBackground(Integer... numArr) {
        return BioLockService.getTimeZoneById(PullController.getPullHandle(), numArr[0].intValue());
    }
}
