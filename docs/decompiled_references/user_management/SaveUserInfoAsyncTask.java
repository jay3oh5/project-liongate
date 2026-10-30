package com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock;

import android.content.Context;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.pullsdk.PullController;
import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioBtDaoWrapper;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockService;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUserAuthorize;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.MyApplication;

/* JADX INFO: loaded from: classes.dex */
public class SaveUserInfoAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final int ERROR_USER_FULL = -18;
    private static final String TAG = "SaveUserInfoAsyncTask";
    private BlUser mCacheUser;
    private BlUserAuthorize mCacheUserAuthorize;
    private Context mContext;
    private MaterialDialog mLoadingDialog;
    private BlUser mUser;
    private BlUserAuthorize mUserAuthorize;
    private long pullHandle = PullController.getPullHandle();
    private int ret = 0;

    public SaveUserInfoAsyncTask(Context context, BlUser blUser, BlUserAuthorize blUserAuthorize, BlUser blUser2, BlUserAuthorize blUserAuthorize2) {
        this.mUser = blUser;
        this.mUserAuthorize = blUserAuthorize;
        this.mCacheUser = blUser2;
        this.mCacheUserAuthorize = blUserAuthorize2;
        this.mContext = context;
        this.mLoadingDialog = ZkDialogManager.createCircleProgressDialog(this.mContext, R.string.saving);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Boolean doInBackground(Void... voidArr) {
        ZKLog.d(TAG, "mUser= " + this.mUser.toString());
        ZKLog.d(TAG, "cacheUser= " + this.mCacheUser.toString());
        if (!this.mUser.equals(this.mCacheUser)) {
            boolean zSaveUserInfo = saveUserInfo();
            if (!zSaveUserInfo && this.ret != -18) {
                zSaveUserInfo = saveUserInfo();
            }
            if (zSaveUserInfo) {
                new BioBtDaoWrapper(BlUser.class).update(this.mUser);
            } else {
                return false;
            }
        }
        if (this.mUserAuthorize.getAuthorizeTimeZoneId().intValue() != this.mCacheUserAuthorize.getAuthorizeTimeZoneId().intValue()) {
            boolean zSaveUserAuthorize2Lock = BioLockService.saveUserAuthorize2Lock(this.pullHandle, this.mUser.getUID(), this.mUserAuthorize.getAuthorizeTimeZoneId().intValue());
            if (!zSaveUserAuthorize2Lock) {
                zSaveUserAuthorize2Lock = BioLockService.saveUserAuthorize2Lock(this.pullHandle, this.mUser.getUID(), this.mUserAuthorize.getAuthorizeTimeZoneId().intValue());
            }
            if (zSaveUserAuthorize2Lock) {
                new BioBtDaoWrapper(BlUserAuthorize.class).update(this.mUserAuthorize);
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean saveUserInfo() {
        this.ret = BioLockService.saveUserData2Lock(this.pullHandle, this.mUser);
        return this.ret == 0;
    }

    @Override // android.os.AsyncTask
    protected void onPreExecute() {
        super.onPreExecute();
        this.mLoadingDialog.show();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        this.mLoadingDialog.dismiss();
        if (bool.booleanValue()) {
            checkCurrentUser();
            Utils.toastShow(R.string.saveSucceed);
        } else if (this.ret == -18) {
            Utils.toastShow(R.string.userIsFull);
        } else {
            Utils.toastShow(R.string.saveFailed);
        }
    }

    private void checkCurrentUser() {
        ZKLog.d(TAG, "checkCurrentUser");
        ZKLog.d(TAG, "myapplication pin " + MyApplication.getPin());
        ZKLog.d(TAG, "cache pin " + this.mCacheUser.getPin());
        ZKLog.d(TAG, "mUser pin " + this.mUser.getPin());
        if (MyApplication.isNullUser() || MyApplication.getPin() != this.mCacheUser.getPin().intValue()) {
            return;
        }
        ZKLog.d(TAG, "changeCurrentUser");
        MyApplication.setUser(this.mUser);
    }
}
