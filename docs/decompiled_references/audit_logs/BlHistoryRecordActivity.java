package com.zkteconology.android.zkbiobt.VIEW.activity.biolock;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.tool.ZKLog;
import com.zkteco.android.widget.datepicker.NumericWheelAdapter;
import com.zkteco.android.widget.datepicker.OnWheelScrollListener;
import com.zkteco.android.widget.datepicker.WheelView;
import com.zkteconology.android.zkbiobt.CONTROLLER.adapter.biolock.HistoryRecordAdapter;
import com.zkteconology.android.zkbiobt.CONTROLLER.entity.biolock.UnlockRecord;
import com.zkteconology.android.zkbiobt.CONTROLLER.service.biolock.BioLockHelper;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.LoadHistoryRecordAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.SearchRecordAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.MA300Constant;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BlHistoryRecordActivity extends ZKBaseActivity implements View.OnFocusChangeListener {
    private WheelView day;
    private InputMethodManager imm;
    private LayoutInflater inflater;
    private MaterialDialog mAlertDialog;
    private Context mContext;
    private int mCurrentChooseTxt;
    private LinearLayout mDataPickerContainer;
    private LinearLayout mDataPickerContent;
    private TextView mDataPickerFinish;
    private Button mEndTimeTxt;
    private HistoryRecordAdapter mHistoryRecordAdapter;
    private List<UnlockRecord> mList;
    private ListView mListView;
    private MaterialDialog mLoadingDialog;
    private EditText mSearchEdit;
    private Button mStartTimeTxt;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WheelView month;
    private WheelView year;
    private final String TAG = BlHistoryRecordActivity.class.getName();
    private boolean isSwipeRefresh = false;
    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.1
        @Override // android.widget.TextView.OnEditorActionListener
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if (i != 3) {
                return false;
            }
            Log.d(BlHistoryRecordActivity.this.TAG, " searching ........... ");
            String searchEditContent = BlHistoryRecordActivity.this.getSearchEditContent();
            if (searchEditContent.length() != 0) {
                BlHistoryRecordActivity.this.startSearch(searchEditContent);
                return true;
            }
            Utils.toastShow(R.string.cannotSearchNull);
            return true;
        }
    };
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.2
        @Override // android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
        public void onRefresh() {
            BlHistoryRecordActivity.this.isSwipeRefresh = true;
            BlHistoryRecordActivity.this.startLoadRecord(BioLockHelper.getTimeMillisFrom2K(BlHistoryRecordActivity.this.getUnFormatTime(BlHistoryRecordActivity.this.getStartTimeTxtContent()), false), BioLockHelper.getTimeMillisFrom2K(BlHistoryRecordActivity.this.getUnFormatTime(BlHistoryRecordActivity.this.getEndTimeTxtContent()), true));
        }
    };
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.3
        @Override // android.view.View.OnTouchListener
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.getId() == R.id.history_record_search_edittext) {
                int rawX = (int) motionEvent.getRawX();
                int rawY = (int) motionEvent.getRawY();
                ZKLog.d(BlHistoryRecordActivity.this.TAG, "eventX = " + rawX + "; eventY = " + rawY);
                Rect rect = new Rect();
                view.getGlobalVisibleRect(rect);
                ZKLog.d(BlHistoryRecordActivity.this.TAG, "left=" + rect.left + "right=" + rect.right + "top=" + rect.top + "bottom=" + rect.bottom);
                rect.left = rect.right - Utils.getDrawableWidth(R.drawable.icon_circle_closed);
                if (!rect.contains(rawX, rawY)) {
                    return false;
                }
                BlHistoryRecordActivity.this.mSearchEdit.setText("");
                BlHistoryRecordActivity.this.mHistoryRecordAdapter.reFreshData(BlHistoryRecordActivity.this.mList);
                return true;
            }
            if (view.getId() == R.id.history_record_starttime_text) {
                Log.d(BlHistoryRecordActivity.this.TAG, "starttime----------");
                BlHistoryRecordActivity.this.mCurrentChooseTxt = R.id.history_record_starttime_text;
                BlHistoryRecordActivity.this.showDataPick(BlHistoryRecordActivity.this.getStartTimeTxtContent());
                return false;
            }
            if (view.getId() == R.id.history_record_endtime_text) {
                Log.d(BlHistoryRecordActivity.this.TAG, "endtime-----------");
                BlHistoryRecordActivity.this.mCurrentChooseTxt = R.id.history_record_endtime_text;
                BlHistoryRecordActivity.this.showDataPick(BlHistoryRecordActivity.this.getEndTimeTxtContent());
                return false;
            }
            BlHistoryRecordActivity.this.closeDataPick();
            return false;
        }
    };
    OnWheelScrollListener scrollListener = new OnWheelScrollListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.6
        @Override // com.zkteco.android.widget.datepicker.OnWheelScrollListener
        public void onScrollingStarted(WheelView wheelView) {
        }

        @Override // com.zkteco.android.widget.datepicker.OnWheelScrollListener
        public void onScrollingFinished(WheelView wheelView) {
            int currentItem = BlHistoryRecordActivity.this.year.getCurrentItem() + 1;
            int currentItem2 = BlHistoryRecordActivity.this.month.getCurrentItem() + 1;
            int currentItem3 = BlHistoryRecordActivity.this.day.getCurrentItem() + 1;
            Log.d(BlHistoryRecordActivity.this.TAG, " onScrollingFinished currentYear " + currentItem + " currentMonth " + currentItem2 + " currentDay " + currentItem3);
            BlHistoryRecordActivity.this.initDay(currentItem, currentItem2, currentItem3);
        }
    };

    private void initListData() {
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bl_history_record);
        this.mContext = this;
        this.inflater = (LayoutInflater) getSystemService("layout_inflater");
        this.imm = (InputMethodManager) getSystemService("input_method");
        initView();
        long timeMillisFrom2K = BioLockHelper.getTimeMillisFrom2K();
        startLoadRecord(timeMillisFrom2K - 604800, timeMillisFrom2K);
    }

    private void initView() {
        ((TextView) findViewById(R.id.rl_toolbar).findViewById(R.id.tv_toolbar_title)).setText(R.string.history_record_title);
        this.mStartTimeTxt = (Button) findViewById(R.id.history_record_starttime_text);
        this.mEndTimeTxt = (Button) findViewById(R.id.history_record_endtime_text);
        this.mSearchEdit = (EditText) findViewById(R.id.history_record_search_edittext);
        this.mSearchEdit.setOnFocusChangeListener(this);
        this.mSearchEdit.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mSearchEdit.setOnTouchListener(this.mOnTouchListener);
        this.mStartTimeTxt.setOnTouchListener(this.mOnTouchListener);
        this.mEndTimeTxt.setOnTouchListener(this.mOnTouchListener);
        this.mDataPickerContainer = (LinearLayout) findViewById(R.id.history_record_data_pick_container);
        this.mDataPickerContent = (LinearLayout) findViewById(R.id.history_record_data_pick_content);
        this.mDataPickerFinish = (TextView) findViewById(R.id.history_record_data_pick_finish);
        this.mDataPickerFinish.setOnClickListener(this);
        this.mDataPickerContent.addView(getDataPickView());
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        this.mSwipeRefreshLayout.setOnRefreshListener(this.mOnRefreshListener);
        this.mSwipeRefreshLayout.setOnTouchListener(this.mOnTouchListener);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.lightGreen);
        this.mListView = (ListView) findViewById(R.id.user_listview);
        initListData();
        this.mHistoryRecordAdapter = new HistoryRecordAdapter(this, this.mList);
        this.mListView.setAdapter((ListAdapter) this.mHistoryRecordAdapter);
        this.mListView.setOnTouchListener(this.mOnTouchListener);
        this.mAlertDialog = ZkDialogManager.createToastDialog(this, R.string.attention, R.string.nullString);
        this.mLoadingDialog = ZkDialogManager.createCircleProgressDialog(this.mContext, R.string.loadingData);
        initTime();
    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(1);
        int i2 = calendar.get(2) + 1;
        int i3 = calendar.get(5);
        setStartTimeTxt(i, i2, i3);
        setEndTimeTxt(i, i2, i3);
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

    @Override // android.view.View.OnFocusChangeListener
    public void onFocusChange(View view, boolean z) {
        Log.d(this.TAG, " onFocusChange ");
        if (z) {
            if (view.getId() != R.id.history_record_search_edittext) {
                return;
            }
            this.mSearchEdit.setGravity(3);
            this.mDataPickerContainer.setVisibility(4);
            return;
        }
        if (view.getId() == R.id.history_record_search_edittext) {
            this.imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            this.mSearchEdit.setGravity(1);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        Log.d(this.TAG, " onClick ");
        if (view.getId() != R.id.history_record_data_pick_finish) {
            return;
        }
        if (R.id.history_record_starttime_text == this.mCurrentChooseTxt) {
            Log.d(this.TAG, " data pick finish starttime ");
            if (getDataPickTime().compareTo(getEndTimeTxtContent()) > 0) {
                this.mAlertDialog.setContent(getResources().getString(R.string.history_record_data_search_warn_one));
                this.mAlertDialog.show();
                return;
            } else {
                setStartTimeTxt(getDataPickTime());
                closeDataPick();
                confirmReLoadData();
                return;
            }
        }
        if (R.id.history_record_endtime_text == this.mCurrentChooseTxt) {
            Log.d(this.TAG, " data pick finish endtime ");
            if (getStartTimeTxtContent().compareTo(getDataPickTime()) > 0) {
                this.mAlertDialog.setContent(getResources().getString(R.string.history_record_data_search_warn_two));
                this.mAlertDialog.show();
            } else {
                setEndTimeTxt(getDataPickTime());
                confirmReLoadData();
                closeDataPick();
            }
        }
    }

    private void confirmReLoadData() {
        ZkDialogManager.createConfirmDialog(this, R.string.attention, R.string.isNeedRefreshData, new MaterialDialog.ButtonCallback() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.4
            @Override // com.afollestad.materialdialogs.MaterialDialog.ButtonCallback
            public void onPositive(MaterialDialog materialDialog) {
                super.onPositive(materialDialog);
                BlHistoryRecordActivity.this.mOnRefreshListener.onRefresh();
            }

            @Override // com.afollestad.materialdialogs.MaterialDialog.ButtonCallback
            public void onNegative(MaterialDialog materialDialog) {
                super.onNegative(materialDialog);
                Utils.toastShow(R.string.ifSetDoneSwipe);
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getSearchEditContent() {
        return this.mSearchEdit.getText().toString().trim();
    }

    public void setStartTimeTxt(String str) {
        Log.d(this.TAG, " setStartTimeTxt " + str);
        this.mStartTimeTxt.setText(str);
    }

    public void setEndTimeTxt(String str) {
        Log.d(this.TAG, " setEndTimeTxt " + str);
        this.mEndTimeTxt.setText(str);
    }

    public void setStartTimeTxt(int i, int i2, int i3) {
        setStartTimeTxt(getPastWeekTime(i, i2, i3));
    }

    public void setEndTimeTxt(int i, int i2, int i3) {
        setEndTimeTxt(getFormatTime(i, i2, i3));
    }

    public String getStartTimeTxtContent() {
        return this.mStartTimeTxt.getText().toString();
    }

    public String getEndTimeTxtContent() {
        return this.mEndTimeTxt.getText().toString();
    }

    private String getPastWeekTime(int i, int i2, int i3) {
        int i4 = i3 - 7;
        if (i4 < 1) {
            do {
                if (1 == i2) {
                    i--;
                    i2 = 12;
                } else {
                    i2--;
                }
            } while (getDay(i, i2) + i4 < 1);
            return getFormatTime(i, i2, getDay(i, i2) + i4);
        }
        return getFormatTime(i, i2, i4);
    }

    private void refreshDataPick(String str) {
        int[] unFormatTime = getUnFormatTime(str);
        if (unFormatTime == null) {
            return;
        }
        Log.d(this.TAG, " refreshDataPick txt " + Arrays.toString(unFormatTime));
        if (unFormatTime[0] != this.year.getCurrentItem() + 1) {
            Log.d(this.TAG, " current DataPick year " + (this.year.getCurrentItem() + 1));
            this.year.setCurrentItem(unFormatTime[0] - 1);
        }
        if (unFormatTime[1] != this.month.getCurrentItem() + 1) {
            Log.d(this.TAG, " current DataPick month " + (this.month.getCurrentItem() + 1));
            this.month.setCurrentItem(unFormatTime[1] - 1);
        }
        if (unFormatTime[2] != this.day.getCurrentItem() + 1) {
            Log.d(this.TAG, " current DataPick day " + (this.day.getCurrentItem() + 1));
            this.day.setCurrentItem(unFormatTime[2] - 1);
        }
        Log.d(this.TAG, " last DataPick year " + (this.year.getCurrentItem() + 1) + " month " + (this.month.getCurrentItem() + 1) + " day " + (this.day.getCurrentItem() + 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDataPick(String str) {
        if (this.mDataPickerContainer.getVisibility() != 0) {
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

    private void performAnimation(final View view, float f, float f2, final boolean z) {
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat(view, "translationY", f, f2);
        objectAnimatorOfFloat.addListener(new AnimatorListenerAdapter() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.5
            @Override // com.nineoldandroids.animation.AnimatorListenerAdapter, com.nineoldandroids.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animator) {
                view.setVisibility(0);
            }

            @Override // com.nineoldandroids.animation.AnimatorListenerAdapter, com.nineoldandroids.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                view.setVisibility(z ? 4 : 0);
            }
        });
        objectAnimatorOfFloat.setDuration(800L).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int[] getUnFormatTime(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] strArrSplit = str.trim().split("/");
        for (String str2 : strArrSplit) {
            if (str2.startsWith("0")) {
                str2.substring(1, str2.length());
            }
        }
        return new int[]{Integer.valueOf(strArrSplit[0]).intValue(), Integer.valueOf(strArrSplit[1]).intValue(), Integer.valueOf(strArrSplit[2]).intValue()};
    }

    private String getFormatTime(int i, int i2, int i3) {
        Object objValueOf;
        Object objValueOf2;
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        sb.append("/");
        if (i2 < 10) {
            objValueOf = "0" + i2;
        } else {
            objValueOf = Integer.valueOf(i2);
        }
        sb.append(objValueOf);
        sb.append("/");
        if (i3 < 10) {
            objValueOf2 = "0" + i3;
        } else {
            objValueOf2 = Integer.valueOf(i3);
        }
        sb.append(objValueOf2);
        return sb.toString();
    }

    private String getDataPickTime() {
        return getFormatTime(this.year.getCurrentItem() + 1, this.month.getCurrentItem() + 1, this.day.getCurrentItem() + 1);
    }

    private View getDataPickView() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(1);
        int i2 = calendar.get(2) + 1;
        int i3 = calendar.get(5);
        Log.d(this.TAG, " getDataPick mYear " + i + " mMonth " + i2 + " mDay " + i3);
        View viewInflate = this.inflater.inflate(R.layout.history_record_date_picker, (ViewGroup) null);
        this.year = (WheelView) viewInflate.findViewById(R.id.date_wheelview_year);
        this.year.setAdapter(new NumericWheelAdapter(1, MA300Constant.MAX_USER_COUNT));
        this.year.setCyclic(true);
        this.year.setCurrentItem(i + (-1));
        this.year.setLabel(getResources().getString(R.string.date_year));
        this.year.addScrollingListener(this.scrollListener);
        this.month = (WheelView) viewInflate.findViewById(R.id.date_wheelview_month);
        this.month.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
        this.month.setCyclic(true);
        int i4 = i2 - 1;
        this.month.setCurrentItem(i4);
        this.month.setLabel(getResources().getString(R.string.date_month));
        this.month.addScrollingListener(this.scrollListener);
        this.day = (WheelView) viewInflate.findViewById(R.id.date_wheelview_day);
        this.day.setAdapter(new NumericWheelAdapter(1, getDay(i, i4), "%02d"));
        this.day.setCyclic(true);
        this.day.setCurrentItem(i3 - 1);
        this.day.setLabel(getResources().getString(R.string.date_day));
        return viewInflate;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initDay(int i, int i2, int i3) {
        int day = getDay(i, i2);
        this.day.setAdapter(new NumericWheelAdapter(1, day, "%02d"));
        this.day.setCurrentItem(i3 <= day ? i3 - 1 : day - 1);
        Log.d(this.TAG, " initDay currentYear " + i + " currentMonth " + i2 + " maxDay " + day);
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append(" result day ");
        if (i3 > day) {
            i3 = day;
        }
        sb.append(i3);
        Log.d(str, sb.toString());
    }

    private int getDay(int i, int i2) {
        boolean z = i % 4 == 0;
        switch (i2) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                return 31;
            case 2:
                return z ? 29 : 28;
            case 4:
            case 6:
            case 9:
            case 11:
            default:
                return 30;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r7v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity$7] */
    public void startLoadRecord(long j, long j2) {
        new LoadHistoryRecordAsyncTask(this.mContext, j, j2) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.7
            @Override // android.os.AsyncTask
            protected void onPreExecute() {
                super.onPreExecute();
                BlHistoryRecordActivity.this.handleUiAnimationStart();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(ArrayList<UnlockRecord> arrayList) {
                super.onPostExecute(arrayList);
                ZKLog.d(BlHistoryRecordActivity.this.TAG, "unlockRecords size is  " + arrayList.size());
                BlHistoryRecordActivity.this.mList = arrayList;
                BlHistoryRecordActivity.this.mHistoryRecordAdapter.reFreshData(BlHistoryRecordActivity.this.mList);
                BlHistoryRecordActivity.this.handleUiAnimationEnd();
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity$8] */
    public void startSearch(String str) {
        new SearchRecordAsyncTask(this.mContext, this.mList, str) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlHistoryRecordActivity.8
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.SearchRecordAsyncTask, android.os.AsyncTask
            public void onPostExecute(List<UnlockRecord> list) {
                super.onPostExecute(list);
                BlHistoryRecordActivity.this.mHistoryRecordAdapter.reFreshData(list);
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUiAnimationStart() {
        if (this.isSwipeRefresh) {
            this.mSwipeRefreshLayout.setRefreshing(true);
        } else {
            this.mLoadingDialog.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUiAnimationEnd() {
        if (this.isSwipeRefresh) {
            this.mSwipeRefreshLayout.setRefreshing(false);
            this.isSwipeRefresh = false;
        } else {
            this.mLoadingDialog.dismiss();
        }
    }
}
