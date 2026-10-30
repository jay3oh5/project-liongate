package com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock;

import android.content.Context;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.pullsdk.PullController;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioBtDaoWrapper;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockService;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.R;

/* JADX INFO: loaded from: classes.dex */
public class DeleteUserAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private MaterialDialog mLoadingDialog;
    private long mPullHandle = PullController.getPullHandle();
    private BlUser mUser;

    public DeleteUserAsyncTask(Context context, BlUser blUser) {
        this.mContext = context;
        this.mUser = blUser;
        this.mLoadingDialog = ZkDialogManager.createCircleProgressDialog(this.mContext, R.string.pleaseWait);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public Boolean doInBackground(Void... voidArr) {
        boolean zDeleteUser = BioLockService.deleteUser(this.mPullHandle, this.mUser);
        if (!zDeleteUser) {
            zDeleteUser = BioLockService.deleteUser(this.mPullHandle, this.mUser);
        }
        if (zDeleteUser) {
            new BioBtDaoWrapper(BlUser.class).delete(this.mUser);
        }
        return Boolean.valueOf(zDeleteUser);
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
    }
}
