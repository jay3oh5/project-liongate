package com.zkteconology.android.zkbiobt.VIEW.activity.biolock;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.pullsdk.PullController;
import com.zkteco.android.widget.datepicker.NumericWheelAdapter;
import com.zkteco.android.widget.datepicker.WheelView;
import com.zkteconology.android.zkbiobt.CONTROLLER.adapter.biolock.TimeZoneSchemeAdapter;
import com.zkteconology.android.zkbiobt.CONTROLLER.adapter.biolock.TimeZoneSchemeContentAdapter;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock.TimeZoneSelect;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.LoadTimeZoneDataAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.UploadTimeZoneData2LockAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Constant;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlTimeZone;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.MyApplication;
import com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: classes.dex */
public class BlTimeZoneActivity extends ZKBaseActivity {
    private static final String TAG = "com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity";
    public static Map<Long, Integer> mCurrentStateChangeMap;
    private WheelView hour;
    private LayoutInflater inflater;
    private boolean isCurrentSchemeLoadSuccess;
    private MaterialDialog mConfirmDialog;
    private RelativeLayout mCover;
    private int mCurrentChooseTxt;
    private TimeZoneSelect mCurrentSelect;
    private LinearLayout mDataPickerContainer;
    private LinearLayout mDataPickerContent;
    private TextView mDataPickerFinish;
    private Button mEndTimeTxt;
    private MaterialDialog mLoadingDialog;
    private TimeZoneSchemeAdapter mSchemeAdaper;
    private TimeZoneSchemeContentAdapter mSchemeContentAdaper;
    private GridView mSchemeContentGridView;
    private List<String> mSchemeContentList;
    private GridView mSchemeGridView;
    private List<String> mSchemeList;
    private Button mStartTimeTxt;
    private RelativeLayout mTimeSelectContent;
    private CheckBox mTimeTableCheck;
    private MaterialDialog mToastDialog;
    private WheelView minute;
    private static final String[] SCHEME_CONTENT_LIST = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    public static boolean isFirst = true;
    private AdapterView.OnItemClickListener mOnItemSchemeClickListener = new AdapterView.OnItemClickListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.3
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            BlTimeZoneActivity.this.mOnItemSchemeCallBack.onItemClick(i);
        }
    };
    private AdapterView.OnItemClickListener mOnItemSchemeContentClickListener = new AdapterView.OnItemClickListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.4
        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            BlTimeZoneActivity.this.closeDataPick();
            Log.d(BlTimeZoneActivity.TAG, "OnItemSchemeContent position " + i);
            BlTimeZoneActivity.this.closeScheme();
        }
    };
    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.5
        @Override // android.widget.CompoundButton.OnCheckedChangeListener
        public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
            if (z) {
                BlTimeZoneActivity.this.closeDataPick();
                BlTimeZoneActivity.this.showScheme();
            } else {
                BlTimeZoneActivity.this.closeScheme();
            }
        }
    };
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.6
        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int id = view.getId();
            if (id == R.id.time_zone_check_scheme) {
                BlTimeZoneActivity.this.closeDataPick();
                return false;
            }
            if (id == R.id.time_zone_end_time_txt) {
                BlTimeZoneActivity.this.closeScheme();
                BlTimeZoneActivity.this.mCurrentChooseTxt = R.id.time_zone_end_time_txt;
                BlTimeZoneActivity.this.showDataPick(BlTimeZoneActivity.this.getEndTimeTxtContent());
                return false;
            }
            if (id != R.id.time_zone_start_time_txt) {
                return false;
            }
            BlTimeZoneActivity.this.closeScheme();
            BlTimeZoneActivity.this.mCurrentChooseTxt = R.id.time_zone_start_time_txt;
            BlTimeZoneActivity.this.showDataPick(BlTimeZoneActivity.this.getStartTimeTxtContent());
            return false;
        }
    };
    private MaterialDialog.ButtonCallback mButtonCallBack = new MaterialDialog.ButtonCallback() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.7
        @Override // com.afollestad.materialdialogs.MaterialDialog.ButtonCallback
        public void onPositive(MaterialDialog materialDialog) {
            super.onPositive(materialDialog);
            BlTimeZoneActivity.this.uploadTimeZoneData2Lock();
            BlTimeZoneActivity.this.mConfirmDialog.dismiss();
        }
    };
    private OnItemSchemeCallBack mOnItemSchemeCallBack = new OnItemSchemeCallBack() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.8
        @Override // com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.OnItemSchemeCallBack
        public void onItemClick(int i) {
            Log.d(BlTimeZoneActivity.TAG, " scheme - position " + i + " is selected! ");
            BlTimeZoneActivity.this.mCover.setVisibility(i < 2 ? 0 : 4);
            CheckBox checkBox = BlTimeZoneActivity.this.mTimeTableCheck;
            StringBuilder sb = new StringBuilder();
            int i2 = i + 1;
            sb.append(i2);
            sb.append("");
            checkBox.setText(sb.toString());
            BlTimeZoneActivity.this.loadTimeZoneData(i2);
            BlTimeZoneActivity.this.closeScheme();
        }
    };
    private OnItemSchemeContentCallBack mOnItemSchemeContentCallBack = new OnItemSchemeContentCallBack() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.9
        @Override // com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.OnItemSchemeContentCallBack
        public void onItemClick(int i, boolean z) {
            Log.d(BlTimeZoneActivity.TAG, "mOnItemSchemeContentCallBack - position " + i + " isChecked ");
            BlTimeZoneActivity.this.mCurrentSelect.getIsSelectArray()[i] = z;
            BlTimeZoneActivity.this.closeScheme();
        }
    };

    public interface OnItemSchemeCallBack {
        void onItemClick(int i);
    }

    public interface OnItemSchemeContentCallBack {
        void onItemClick(int i, boolean z);
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bl_time_zone);
        this.inflater = (LayoutInflater) getSystemService("layout_inflater");
        initFirstShecme();
        initView();
        loadTimeZoneData(1);
    }

    private void initFirstShecme() {
        this.mCurrentSelect = new TimeZoneSelect(null, 1);
    }

    private void initView() {
        ((TextView) findViewById(R.id.rl_toolbar).findViewById(R.id.tv_toolbar_title)).setText(R.string.time_zone_title);
        ((ImageView) findViewById(R.id.img_toolbar_right)).setImageResource(R.drawable.time_zone_upload_icon);
        findViewById(R.id.img_toolbar_right).setOnClickListener(this);
        ((TextView) findViewById(R.id.time_zone_device_name)).setText(MyApplication.getConnectedDevice().getName());
        this.mTimeTableCheck = (CheckBox) findViewById(R.id.time_zone_checkbox);
        this.mTimeTableCheck.setOnCheckedChangeListener(this.mOnCheckedChangeListener);
        this.mTimeTableCheck.setText("1");
        this.mSchemeGridView = (GridView) findViewById(R.id.time_zone_check_scheme);
        this.mSchemeList = new ArrayList();
        for (int i = 1; i < 13; i++) {
            this.mSchemeList.add(String.valueOf(i));
        }
        this.mSchemeAdaper = new TimeZoneSchemeAdapter(this, this.mSchemeList);
        this.mSchemeGridView.setAdapter((ListAdapter) this.mSchemeAdaper);
        this.mSchemeAdaper.setOnItemSchemeCallBack(this.mOnItemSchemeCallBack);
        this.mSchemeGridView.setOnItemClickListener(this.mOnItemSchemeClickListener);
        this.mSchemeGridView.setOnTouchListener(this.mOnTouchListener);
        this.mSchemeContentGridView = (GridView) findViewById(R.id.time_zone_check_scheme_content);
        this.mSchemeContentList = new ArrayList();
        for (int i2 = 0; i2 < SCHEME_CONTENT_LIST.length; i2++) {
            this.mSchemeContentList.add(SCHEME_CONTENT_LIST[i2]);
        }
        this.mSchemeContentAdaper = new TimeZoneSchemeContentAdapter(this, this.mCurrentSelect, this.mSchemeContentList);
        this.mSchemeContentGridView.setAdapter((ListAdapter) this.mSchemeContentAdaper);
        this.mSchemeContentGridView.setOnItemClickListener(this.mOnItemSchemeContentClickListener);
        this.mSchemeContentAdaper.setOnItemSchemeContentCallBack(this.mOnItemSchemeContentCallBack);
        this.mSchemeContentGridView.setOnTouchListener(this.mOnTouchListener);
        this.mDataPickerContainer = (LinearLayout) findViewById(R.id.time_zone_data_pick_container);
        this.mDataPickerContent = (LinearLayout) findViewById(R.id.time_zone_data_pick_content);
        this.mDataPickerFinish = (TextView) findViewById(R.id.time_zone_data_pick_finish);
        this.mDataPickerFinish.setOnClickListener(this);
        this.mDataPickerContent.addView(getDataPickView());
        this.mStartTimeTxt = (Button) findViewById(R.id.time_zone_start_time_txt);
        this.mEndTimeTxt = (Button) findViewById(R.id.time_zone_end_time_txt);
        this.mStartTimeTxt.setOnTouchListener(this.mOnTouchListener);
        this.mEndTimeTxt.setOnTouchListener(this.mOnTouchListener);
        findViewById(R.id.time_zone_user_select_btn).setOnClickListener(this);
        this.mCover = (RelativeLayout) findViewById(R.id.time_zone_cover);
        this.mCover.setOnClickListener(this);
        this.mTimeSelectContent = (RelativeLayout) findViewById(R.id.time_zone_time_select_content);
        this.mSchemeContentGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.1
            private boolean isFirst = true;

            @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
            public void onGlobalLayout() {
                if (this.isFirst) {
                    Log.d(BlTimeZoneActivity.TAG, "mSchemeContentGridView height " + BlTimeZoneActivity.this.mSchemeContentGridView.getMeasuredHeight());
                    Log.d(BlTimeZoneActivity.TAG, "mTimeSelectContent height " + BlTimeZoneActivity.this.mTimeSelectContent.getMeasuredHeight());
                    BlTimeZoneActivity.this.mCover.setMinimumHeight(BlTimeZoneActivity.this.mSchemeContentGridView.getMeasuredHeight() + (BlTimeZoneActivity.this.mTimeSelectContent.getMeasuredHeight() * 2));
                    this.isFirst = false;
                }
            }
        });
        mCurrentStateChangeMap = new Hashtable();
        this.mLoadingDialog = ZkDialogManager.createCircleProgressDialog(this, R.string.pleaseWait);
        this.mConfirmDialog = ZkDialogManager.createConfirmDialog(this, "", getResources().getString(R.string.confirm_upload), this.mButtonCallBack);
        this.mToastDialog = ZkDialogManager.createToastDialog(this, getString(R.string.attention), "");
    }

    public String getStartTimeTxtContent() {
        return this.mStartTimeTxt.getText().toString();
    }

    public String getEndTimeTxtContent() {
        return this.mEndTimeTxt.getText().toString();
    }

    public void setEndTimeTxt(String str) {
        this.mEndTimeTxt.setText(str);
    }

    public void setStartTimeTxt(String str) {
        this.mStartTimeTxt.setText(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showScheme() {
        if (4 == this.mSchemeGridView.getVisibility()) {
            performAnimation(this.mSchemeGridView, -this.mSchemeGridView.getHeight(), 0.0f, false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void closeScheme() {
        if (this.mSchemeGridView.getVisibility() == 0) {
            performAnimation(this.mSchemeGridView, 0.0f, -this.mSchemeGridView.getHeight(), true);
            this.mTimeTableCheck.setChecked(false);
        }
    }

    private void performAnimation(final View view, float f, float f2, final boolean z) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, "translationY", f, f2);
        objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.2
            @Override // com.nineoldandroids.animation.AnimatorListenerAdapter, com.nineoldandroids.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                view.setVisibility(0);
            }

            @Override // com.nineoldandroids.animation.AnimatorListenerAdapter, com.nineoldandroids.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(z ? 4 : 0);
            }
        });
        objectAnimatorOfFloat.setDuration(300L).start();
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.img_toolbar_right) {
            closeScheme();
            Log.d(TAG, " isCurrentSchemeLoadSuccess " + this.isCurrentSchemeLoadSuccess);
            Log.d(TAG, " scheme is " + this.mTimeTableCheck.getText().toString());
            if (!this.isCurrentSchemeLoadSuccess && Integer.valueOf(this.mTimeTableCheck.getText().toString()).intValue() < 3) {
                this.mCover.performClick();
                return;
            } else {
                this.mConfirmDialog.show();
                return;
            }
        }
        if (id == R.id.time_zone_cover) {
            closeScheme();
            this.mToastDialog.setContent(getResources().getString(R.string.warn_prefix) + ((Object) this.mTimeTableCheck.getText()) + " " + getResources().getString(R.string.warn_cannot_change));
            this.mToastDialog.show();
            return;
        }
        if (id != R.id.time_zone_data_pick_finish) {
            if (id != R.id.time_zone_user_select_btn) {
                return;
            }
            closeScheme();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.MARK_CURRENT_SELECT_SCHEME, this.mTimeTableCheck.getText().toString());
            openActivity(BlUserSelectActivity.class, bundle);
            return;
        }
        if (R.id.time_zone_start_time_txt == this.mCurrentChooseTxt) {
            if (getDataPickTime().compareTo(getEndTimeTxtContent()) > 0) {
                this.mToastDialog.setContent(getResources().getString(R.string.history_record_data_search_warn_one));
                this.mToastDialog.show();
                return;
            } else {
                setStartTimeTxt(getDataPickTime());
                closeDataPick();
                return;
            }
        }
        if (R.id.time_zone_end_time_txt == this.mCurrentChooseTxt) {
            if (getStartTimeTxtContent().compareTo(getDataPickTime()) > 0) {
                this.mToastDialog.setContent(getResources().getString(R.string.history_record_data_search_warn_two));
                this.mToastDialog.show();
            } else {
                setEndTimeTxt(getDataPickTime());
                closeDataPick();
            }
        }
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity
    public void onKeyBackBtn(View view) {
        if (closeDataPick()) {
            return;
        }
        super.onKeyBackBtn(view);
        finish();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDataPick(String str) {
        if (4 == this.mDataPickerContainer.getVisibility()) {
            performAnimation(this.mDataPickerContainer, this.mDataPickerContainer.getHeight(), 0.0f, false);
        }
        refreshDataPick(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public boolean closeDataPick() {
        if (this.mDataPickerContainer.getVisibility() != 0) {
            return false;
        }
        performAnimation(this.mDataPickerContainer, 0.0f, this.mDataPickerContainer.getHeight(), true);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity$10] */
    public void loadTimeZoneData(int i) {
        new LoadTimeZoneDataAsyncTask() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.10
            @Override // android.os.AsyncTask
            protected void onPreExecute() {
                super.onPreExecute();
                BlTimeZoneActivity.this.mLoadingDialog.show();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(BlTimeZone blTimeZone) {
                super.onPostExecute(blTimeZone);
                BlTimeZoneActivity.mCurrentStateChangeMap.clear();
                BlTimeZoneActivity.this.isCurrentSchemeLoadSuccess = blTimeZone != null;
                BlTimeZoneActivity.this.mCurrentSelect = new TimeZoneSelect(blTimeZone, Integer.valueOf(BlTimeZoneActivity.this.mTimeTableCheck.getText().toString()).intValue());
                BlTimeZoneActivity.this.mSchemeContentAdaper.setSelect(BlTimeZoneActivity.this.mCurrentSelect);
                BlTimeZoneActivity.this.setStartTimeTxt(BlTimeZoneActivity.this.mCurrentSelect.getStartTime());
                BlTimeZoneActivity.this.setEndTimeTxt(BlTimeZoneActivity.this.mCurrentSelect.getEndTime());
                BlTimeZoneActivity.this.mLoadingDialog.cancel();
                Log.d(BlTimeZoneActivity.TAG, " Load mCurrentSelect " + BlTimeZoneActivity.this.mCurrentSelect);
                if (blTimeZone == null) {
                    BlTimeZoneActivity.this.mToastDialog.setContent(BlTimeZoneActivity.this.getResources().getString(R.string.load_fail));
                    BlTimeZoneActivity.this.mToastDialog.show();
                }
            }
        }.execute(new Integer[]{Integer.valueOf(i)});
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r8v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity$11] */
    public void uploadTimeZoneData2Lock() {
        new UploadTimeZoneData2LockAsyncTask(this, PullController.getPullHandle(), this.mCurrentSelect, getStartTimeTxtContent(), getEndTimeTxtContent()) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlTimeZoneActivity.11
            @Override // android.os.AsyncTask
            protected void onPreExecute() {
                super.onPreExecute();
                BlTimeZoneActivity.this.mLoadingDialog.show();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Boolean bool) {
                super.onPostExecute(bool);
                BlTimeZoneActivity.this.mToastDialog.setContent(BlTimeZoneActivity.this.getResources().getString(bool.booleanValue() ? R.string.upload_success : R.string.upload_fail));
                BlTimeZoneActivity.this.mLoadingDialog.dismiss();
                BlTimeZoneActivity.this.mToastDialog.show();
            }
        }.execute(new Void[0]);
    }

    private String getDataPickTime() {
        return Utils.getFormatTime(this.hour.getCurrentItem(), this.minute.getCurrentItem());
    }

    private int[] getUnFormatTime(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] strArrSplit = str.trim().split(":");
        for (String str2 : strArrSplit) {
            if (str2.startsWith("0")) {
                str2.substring(1, str2.length());
            }
        }
        return new int[]{Integer.valueOf(strArrSplit[0]).intValue(), Integer.valueOf(strArrSplit[1]).intValue()};
    }

    private void refreshDataPick(String str) {
        int[] unFormatTime = getUnFormatTime(str);
        if (unFormatTime == null) {
            return;
        }
        Log.d(TAG, " refreshDataPick txt " + Arrays.toString(unFormatTime));
        if (unFormatTime[0] != this.hour.getCurrentItem()) {
            Log.d(TAG, " current DataPick year " + this.hour.getCurrentItem());
            this.hour.setCurrentItem(unFormatTime[0]);
        }
        if (unFormatTime[1] != this.minute.getCurrentItem()) {
            Log.d(TAG, " current DataPick month " + this.minute.getCurrentItem());
            this.minute.setCurrentItem(unFormatTime[1]);
        }
        Log.d(TAG, " last DataPick year " + this.hour.getCurrentItem() + " month " + this.minute.getCurrentItem());
    }

    private View getDataPickView() {
        View viewInflate = this.inflater.inflate(R.layout.time_zone_date_picker, (ViewGroup) null);
        this.hour = (WheelView) viewInflate.findViewById(R.id.date_wheelview_hour);
        this.hour.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
        this.hour.setLabel(getResources().getString(R.string.date_hour));
        this.hour.setCyclic(true);
        this.minute = (WheelView) viewInflate.findViewById(R.id.date_wheelview_minute);
        this.minute.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        this.minute.setLabel(getResources().getString(R.string.date_minute));
        this.minute.setCyclic(true);
        return viewInflate;
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        mCurrentStateChangeMap = null;
    }
}
