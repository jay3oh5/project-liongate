package com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zkteco.android.pullsdk.PullController;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioBtDaoWrapper;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioLockDaoHelp;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockService;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.VIEW.activity.MyApplication;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class LoadUserAsyncTask extends AsyncTask<Void, Void, List<BlUser>> {
    public static final String TAG = "LoadUserAsyncTask";
    private boolean isIncremental;
    private Context mContext = MyApplication.getContext();
    private long mPullHandle = PullController.getPullHandle();
    private List<BlUser> mUserList;

    public static void LoadUser() {
        new LoadUserAsyncTask(new ArrayList(), true).execute(new Void[0]);
    }

    public LoadUserAsyncTask(List<BlUser> list, boolean z) {
        this.isIncremental = z;
        this.mUserList = list;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public List<BlUser> doInBackground(Void... voidArr) {
        List<BlUser> userInfo = BioLockService.getUserInfo(this.mPullHandle, this.isIncremental);
        if (userInfo == null || userInfo.size() == 0) {
            Log.d(TAG, "Load again");
            userInfo = BioLockService.getUserInfo(this.mPullHandle, this.isIncremental);
        }
        if (userInfo != null && userInfo.size() > 0) {
            BioBtDaoWrapper bioBtDaoWrapper = new BioBtDaoWrapper(BlUser.class);
            if (!this.isIncremental) {
                bioBtDaoWrapper.deleteAll();
            }
            Iterator<BlUser> it = userInfo.iterator();
            while (it.hasNext()) {
                bioBtDaoWrapper.insertOrReplace(it.next());
            }
        }
        if (this.isIncremental) {
            if (userInfo != null) {
                this.mUserList.addAll(userInfo);
            }
        } else if (userInfo != null && userInfo.size() > 0) {
            this.mUserList = userInfo;
        }
        this.mUserList = BioLockDaoHelp.LoadUserSortByTime(this.mContext);
        return this.mUserList;
    }
}
