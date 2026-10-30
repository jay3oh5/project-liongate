package com.zkteconology.android.zkbiobt.VIEW.activity.biolock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.swipe.util.Attributes;
import com.zkteco.android.dialog.ZkDialogManager;
import com.zkteco.android.tool.ZKLog;
import com.zkteconology.android.zkbiobt.CONTROLLER.adapter.biolock.UsersAdapter;
import com.zkteconology.android.zkbiobt.CONTROLLER.dao.BioLockDaoHelp;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.DeleteUserAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.LoadUserAsyncTask;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Constant;
import com.zkteconology.android.zkbiobt.CONTROLLER.utils.Utils;
import com.zkteconology.android.zkbiobt.MODEL.database.biolock.BlUser;
import com.zkteconology.android.zkbiobt.R;
import com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity;
import java.io.Serializable;
import java.util.List;

/* JADX INFO: loaded from: classes.dex */
public class BlUsersActivity extends ZKBaseActivity {
    private static final int REQUEST_COD_EDIT_ITEM = 8;
    private static final String TAG = "BlUsersActivity";
    private Context mContext;
    private List<BlUser> mList;
    private ListView mListView;
    private MaterialDialog mLoadingDialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTvUserCount;
    private UsersAdapter mUsersAdapter;
    private boolean isSwipeRefresh = false;
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.2
        @Override // android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener
        public void onRefresh() {
            BlUsersActivity.this.isSwipeRefresh = true;
            BlUsersActivity.this.refreshData(false);
        }
    };
    private CallBackListener mCallBackListener = new CallBackListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.3
        @Override // com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.CallBackListener
        public void editChildItem(int i) {
            BlUsersActivity.this.mUsersAdapter.closeItem(i);
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constant.BUNDLE_USER_INFO, (Serializable) BlUsersActivity.this.mList.get(i));
            BlUsersActivity.this.startActivityForResult((Class<?>) BlUserInfoActivity.class, 8, bundle);
        }

        @Override // com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.CallBackListener
        public void deleteChildItem(int i) {
            ZKLog.d(BlUsersActivity.TAG, "delete position is" + i);
            ZKLog.d(BlUsersActivity.TAG, ((BlUser) BlUsersActivity.this.mList.get(i)).toString());
            BlUsersActivity.this.mUsersAdapter.closeItem(i);
            BlUsersActivity.this.deleteUser((BlUser) BlUsersActivity.this.mList.get(i));
        }
    };

    public interface CallBackListener {
        void deleteChildItem(int i);

        void editChildItem(int i);
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity, android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.support.v4.app.SupportActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_bl_user_list);
        this.mContext = this;
        initView();
        refreshData(this.mList == null || this.mList.size() == 0);
    }

    private void initView() {
        ((TextView) findViewById(R.id.rl_toolbar).findViewById(R.id.tv_toolbar_title)).setText(R.string.menu_item_user_title);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        this.mSwipeRefreshLayout.setOnRefreshListener(this.mOnRefreshListener);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.lightGreen);
        this.mListView = (ListView) findViewById(R.id.user_listview);
        initList();
        this.mUsersAdapter = new UsersAdapter(this, this.mList);
        this.mUsersAdapter.setMode(Attributes.Mode.Single);
        this.mUsersAdapter.setCallBackListener(this.mCallBackListener);
        this.mListView.setAdapter((ListAdapter) this.mUsersAdapter);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                BlUsersActivity.this.mUsersAdapter.closeAllItems();
                BlUsersActivity.this.mCallBackListener.editChildItem(i);
            }
        });
        this.mTvUserCount = (TextView) findViewById(R.id.user_userCount);
        this.mLoadingDialog = ZkDialogManager.createCircleProgressDialog(this.mContext, R.string.loadingData);
    }

    private void initList() {
        this.mList = BioLockDaoHelp.LoadUserSortByTime(this.mContext);
    }

    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
    }

    @Override // com.zkteconology.android.zkbiobt.VIEW.activity.ZKBaseActivity
    public void onKeyBackBtn(View view) {
        super.onKeyBackBtn(view);
        finish();
    }

    @Override // android.support.v4.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 8 && i2 == -1) {
            this.mList = BioLockDaoHelp.LoadUserSortByTime(this.mContext);
            this.mUsersAdapter.reFreshData(this.mList);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void deleteUser(BlUser blUser) {
        new DeleteUserAsyncTask(this.mContext, blUser) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.4
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Can't rename method to resolve collision */
            @Override // com.zkteconology.android.zkbiobt.CONTROLLER.task.biolock.DeleteUserAsyncTask, android.os.AsyncTask
            public void onPostExecute(Boolean bool) {
                super.onPostExecute(bool);
                if (bool.booleanValue()) {
                    BlUsersActivity.this.mList = BioLockDaoHelp.LoadUserSortByTime(BlUsersActivity.this.mContext);
                    BlUsersActivity.this.mTvUserCount.setText(BlUsersActivity.this.mList.size() + "");
                    BlUsersActivity.this.mUsersAdapter.reFreshData(BlUsersActivity.this.mList);
                    Utils.toastShow(R.string.deletedSuccessfully);
                    return;
                }
                Utils.toastShow(R.string.deleteFailed);
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity$5] */
    public void refreshData(boolean z) {
        new LoadUserAsyncTask(this.mList, z) { // from class: com.zkteconology.android.zkbiobt.VIEW.activity.biolock.BlUsersActivity.5
            @Override // android.os.AsyncTask
            protected void onPreExecute() {
                super.onPreExecute();
                BlUsersActivity.this.handleUiAnimationStart();
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(List<BlUser> list) {
                super.onPostExecute(list);
                BlUsersActivity.this.handleUiAnimationEnd();
                if (list.get(0).getStartTime().longValue() != 0) {
                    BlUsersActivity.this.mList = list;
                }
                BlUsersActivity.this.mTvUserCount.setText(BlUsersActivity.this.mList.size() + "");
                BlUsersActivity.this.mUsersAdapter.reFreshData(BlUsersActivity.this.mList);
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
