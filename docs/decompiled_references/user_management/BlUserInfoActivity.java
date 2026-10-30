package com.zkteconology.android.zkbiobt.VIEW.activity.biolock;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioLockDaoHelp;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock.BioLockPrivilege;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockHelper;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.LoadTimeZoneIdAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.SaveUserInfoAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.BioBtPrivilege;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Constant;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUserAuthorize;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity;

/* JADX INFO: loaded from: classes.dex */
public class BlUserInfoActivity extends ZKBaseActivity {
    private BlUser cacheUser;
    private BlUserAuthorize cacheUserAuthorize;
    private Context mContext;
    private ImageView mEditImg;
    private TextView mSaveText;
    private MaterialDialog mSingleChoiceDialog;
    private BlUser mUser;
    private BlUserAuthorize mUserAuthorize;
    private TextView mUserInfoAuthority;
    private String[] mUserInfoAuthorityStr;
    private ImageView mUserInfoIcon;
    private EditText mUserInfoMark;
    private EditText mUserInfoName;
    private EditText mUserInfoPeriod;
    private EditText mUserInfoPin;
    private TextView mUserInfoRegistrationTime;
    private final String TAG = BlUserInfoActivity.class.getName();
    private MaterialDialog.ListCallbackSingleChoice mListCallbackSingleChoice = new MaterialDialog.ListCallbackSingleChoice() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity.1
        @Override // com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice
        public boolean onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
            if (!BlUserInfoActivity.this.mUserInfoAuthorityStr[i].equals(BlUserInfoActivity.this.mUserInfoAuthority.getText())) {
                BlUserInfoActivity.this.mEditImg.setVisibility(8);
                BlUserInfoActivity.this.mSaveText.setVisibility(0);
            }
            BlUserInfoActivity.this.mUser.setPrivilege(Integer.valueOf(BioLockPrivilege.getTypeByString(charSequence.toString())));
            BlUserInfoActivity.this.mUserInfoAuthority.setText(BlUserInfoActivity.this.mUserInfoAuthorityStr[i]);
            BlUserInfoActivity.this.updateIsCanChangeTimeZoneId();
            return true;
        }
    };
    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity.2
        @Override // android.view.View.OnFocusChangeListener
        public void onFocusChange(View view, boolean z) {
            Log.d(BlUserInfoActivity.this.TAG, "onFocusChange called");
            if (z) {
                BlUserInfoActivity.this.mEditImg.setVisibility(8);
                BlUserInfoActivity.this.mSaveText.setVisibility(0);
            }
        }
    };

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bl_info_users);
        this.mContext = this;
        if (getIntent().getExtras() == null) {
            finish();
        }
        this.mUser = (BlUser) getIntent().getExtras().getSerializable(Constant.BUNDLE_USER_INFO);
        this.cacheUser = (BlUser) getIntent().getExtras().getSerializable(Constant.BUNDLE_USER_INFO);
        initView();
        initViewContent();
        getUserTimeId(this.mUser);
    }

    private void initViewContent() {
        this.mUserInfoPin.setText(this.mUser.getPin() + "");
        this.mUserInfoName.setText(this.mUser.getName());
        this.mUserInfoAuthority.setText(BioLockPrivilege.getStringByType(this.mUser.getPrivilege().intValue()));
        this.mUserInfoRegistrationTime.setText(BioLockHelper.getTimeString(this.mUser.getStartTime().longValue()));
        this.mUserInfoIcon.setImageResource(BioLockPrivilege.getEnumByType(this.mUser.getPrivilege().intValue()).getIconResId());
        this.mUserInfoMark.setText(this.mUser.getMark());
        updateIsCanChangeTimeZoneId();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIsCanChangeTimeZoneId() {
        this.mUserInfoPeriod.setText(BioLockPrivilege.getEnumByType(this.mUser.getPrivilege().intValue()).getDefaultTimeZone() + "");
        if (BioBtPrivilege.isAdmin(this.mUser.getPrivilege().intValue())) {
            this.mUserInfoPeriod.setEnabled(false);
        } else {
            this.mUserInfoPeriod.setEnabled(true);
        }
    }

    private void initView() {
        ((TextView) findViewById(R.id.tv_toolbar_title)).setText(R.string.user_info_title);
        this.mEditImg = (ImageView) findViewById(R.id.img_toolbar_right);
        this.mEditImg.setImageResource(R.drawable.users_info_edit_icon);
        this.mSaveText = (TextView) findViewById(R.id.text_toolbar_right);
        this.mSaveText.setText(R.string.user_info_save);
        this.mUserInfoAuthority = (TextView) findViewById(R.id.user_info_authority);
        this.mUserInfoPin = (EditText) findViewById(R.id.user_info_pin);
        this.mUserInfoName = (EditText) findViewById(R.id.user_info_name);
        this.mUserInfoAuthority = (TextView) findViewById(R.id.user_info_authority);
        this.mUserInfoRegistrationTime = (TextView) findViewById(R.id.user_info_registration_time);
        this.mUserInfoPeriod = (EditText) findViewById(R.id.user_info_period);
        this.mUserInfoMark = (EditText) findViewById(R.id.user_info_mark);
        this.mUserInfoIcon = (ImageView) findViewById(R.id.user_info_icon);
        this.mUserInfoPin.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mUserInfoName.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mUserInfoPeriod.setOnFocusChangeListener(this.mOnFocusChangeListener);
        this.mUserInfoMark.setOnFocusChangeListener(this.mOnFocusChangeListener);
        findViewById(R.id.user_info_authority).setOnClickListener(this);
        this.mSaveText.setOnClickListener(this);
        this.mUserInfoAuthorityStr = getResources().getStringArray(R.array.user_info_authority_single_choice_array);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.text_toolbar_right) {
            saveData2Lock();
        } else {
            if (id != R.id.user_info_authority) {
                return;
            }
            showSingleChoiceDialog(this.mUser.getPrivilege().intValue());
        }
    }

    private void showSingleChoiceDialog(int i) {
        int i2;
        for (int i3 = 0; i3 < this.mUserInfoAuthorityStr.length; i3++) {
            if (BioLockPrivilege.getStringByType(i).equals(this.mUserInfoAuthorityStr[i3])) {
                i2 = i3;
                this.mSingleChoiceDialog = ZkDialogManager.createSingleChoiceListDialog(this, R.string.user_info_authority_single_choice_title, R.array.user_info_authority_single_choice_array, i2, this.mListCallbackSingleChoice, R.string.ok);
                this.mSingleChoiceDialog.show();
            }
        }
        i2 = 0;
        this.mSingleChoiceDialog = ZkDialogManager.createSingleChoiceListDialog(this, R.string.user_info_authority_single_choice_title, R.array.user_info_authority_single_choice_array, i2, this.mListCallbackSingleChoice, R.string.ok);
        this.mSingleChoiceDialog.show();
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity
    public void onKeyBackBtn(View view) {
        super.onKeyBackBtn(view);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity$3] */
    public void getUserTimeId(final BlUser blUser) {
        new LoadTimeZoneIdAsyncTask(this.mContext, blUser.getUID().intValue()) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity.3
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.LoadTimeZoneIdAsyncTask, android.os.AsyncTask
            public void onPostExecute(BlUserAuthorize blUserAuthorize) {
                super.onPostExecute(blUserAuthorize);
                if (blUserAuthorize.getAuthorizeTimeZoneId().intValue() == 0) {
                    BlUserInfoActivity.this.showReloadTimeZoneDialog(blUser);
                }
                BlUserInfoActivity.this.mUserAuthorize = blUserAuthorize;
                BlUserInfoActivity.this.cacheUserAuthorize = blUserAuthorize.m12clone();
                BlUserInfoActivity.this.setUserTimeZoneId(BlUserInfoActivity.this.mUserAuthorize.getAuthorizeTimeZoneId().intValue());
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showReloadTimeZoneDialog(final BlUser blUser) {
        ZkDialogManager.createConfirmDialog(this.mContext, R.string.attention, R.string.loadTimeZoneIdFailed, new MaterialDialog.ButtonCallback() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity.4
            @Override // com.afollestad.materialdialogs.MaterialDialog.ButtonCallback
            public void onPositive(MaterialDialog materialDialog) {
                super.onPositive(materialDialog);
                BlUserInfoActivity.this.getUserTimeId(blUser);
            }
        }).show();
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity$5] */
    private void saveData2Lock() {
        if (updateUserInfoFromUi()) {
            new SaveUserInfoAsyncTask(this.mContext, this.mUser, this.mUserAuthorize, this.cacheUser, this.cacheUserAuthorize) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUserInfoActivity.5
                /* JADX INFO: Access modifiers changed from: protected */
                /* JADX WARN: Can't rename method to resolve collision */
                @Override // com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.SaveUserInfoAsyncTask, android.os.AsyncTask
                public void onPostExecute(Boolean bool) {
                    super.onPostExecute(bool);
                    if (bool.booleanValue()) {
                        BlUserInfoActivity.this.finishWithResult(-1);
                    }
                }
            }.execute(new Void[0]);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setUserTimeZoneId(int i) {
        this.mUserInfoPeriod.setText(i + "");
    }

    private boolean updateUserInfoFromUi() {
        if (!updatePin() || !updateName() || !updateMark() || !updateTimeTableId()) {
            return false;
        }
        ZKLog.d(this.TAG, "updateUserInfoFromUi succeed");
        return true;
    }

    private boolean updatePin() {
        int i;
        try {
            i = Integer.parseInt(this.mUserInfoPin.getText().toString().trim());
        } catch (Exception unused) {
            i = 0;
        }
        if (i <= 0 || i > 65534) {
            Utils.toastShow(R.string.pinOutOfRange);
            return false;
        }
        if (i != this.cacheUser.getPin().intValue() && BioLockDaoHelp.checkPinIsExist(this.mContext, i)) {
            Utils.toastShow(R.string.pinHasExist);
            return false;
        }
        this.mUser.setPin(Integer.valueOf(i));
        return true;
    }

    private boolean updateName() {
        String strTrim = this.mUserInfoName.getText().toString().trim();
        if (strTrim.getBytes().length > 24) {
            Utils.toastShow(R.string.nameTooLong);
            return false;
        }
        this.mUser.setName(strTrim);
        return true;
    }

    private boolean updateMark() {
        String strTrim = this.mUserInfoMark.getText().toString().trim();
        if (strTrim.getBytes().length > 63) {
            Utils.toastShow(R.string.markTooLong);
            return false;
        }
        this.mUser.setMark(strTrim);
        return true;
    }

    private boolean updateTimeTableId() {
        int i;
        try {
            i = Integer.parseInt(this.mUserInfoPeriod.getText().toString().trim());
        } catch (Exception unused) {
            i = 0;
        }
        if (i > 0 && i < 13) {
            this.mUserAuthorize.setAuthorizeTimeZoneId(Integer.valueOf(i));
            return true;
        }
        Utils.toastShow(R.string.timeZoneIdWrong);
        return false;
    }
}
