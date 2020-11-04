package com.mobxpert.supercleaner.storagecleaner.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;

import com.mobxpert.supercleaner.storagecleaner.callback.IScanCallback;
import com.mobxpert.supercleaner.storagecleaner.model.JunkGroup;
import com.mobxpert.supercleaner.storagecleaner.model.JunkInfo;
import com.mobxpert.supercleaner.storagecleaner.task.OverallScanTask;
import com.mobxpert.supercleaner.storagecleaner.task.ProcessScanTask;
import com.mobxpert.supercleaner.storagecleaner.task.SysCacheScanTask;
import com.mobxpert.supercleaner.storagecleaner.ui.view.ListHeaderView;
import com.mobxpert.supercleaner.storagecleaner.util.CleanUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class JunkCleanActivity extends AppCompatActivity {

    public static final int MSG_SYS_CACHE_BEGIN = 0x1001;
    public static final int MSG_SYS_CACHE_POS = 0x1002;
    public static final int MSG_SYS_CACHE_FINISH = 0x1003;

    public static final int MSG_PROCESS_BEGIN = 0x1011;
    public static final int MSG_PROCESS_POS = 0x1012;
    public static final int MSG_PROCESS_FINISH = 0x1013;

    public static final int MSG_OVERALL_BEGIN = 0x1021;
    public static final int MSG_OVERALL_POS = 0x1022;
    public static final int MSG_OVERALL_FINISH = 0x1023;

    public static final int MSG_SYS_CACHE_CLEAN_FINISH = 0x1100;
    public static final int MSG_PROCESS_CLEAN_FINISH = 0x1101;
    public static final int MSG_OVERALL_CLEAN_FINISH = 0x1102;

    public static final String HANG_FLAG = "hanged";

    private Handler handler;

    private boolean mIsSysCacheScanFinish = false;
    private boolean mIsSysCacheCleanFinish = false;

    private boolean mIsProcessScanFinish = false;
    private boolean mIsProcessCleanFinish = false;

    private boolean mIsOverallScanFinish = false;
    private boolean mIsOverallCleanFinish = false;

    private boolean mIsScanning = false;

    private BaseExpandableListAdapter mAdapter;
    private HashMap<Integer, JunkGroup> mJunkGroups = null;

    private Button mCleanButton;

    private ListHeaderView mHeaderView;
    private NestedScrollView congratsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_junk_clean);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        congratsView = findViewById(R.id.congrats_view);
        congratsView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down));
      AdView  adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MSG_SYS_CACHE_BEGIN:
                        break;

                    case MSG_SYS_CACHE_POS:
                        mHeaderView.mProgress.setText("Scanning:" + ((JunkInfo) msg.obj).mPackageName);
                        mHeaderView.mSize.setText(CleanUtil.formatShortFileSize(JunkCleanActivity.this, getTotalSize()));
                        break;

                    case MSG_SYS_CACHE_FINISH:
                        mIsSysCacheScanFinish = true;
                        checkScanFinish();
                        break;

                    case MSG_SYS_CACHE_CLEAN_FINISH:
                        mIsSysCacheCleanFinish = true;
                        checkCleanFinish();
                        Bundle bundle = msg.getData();
                        if (bundle != null) {
                            boolean hanged = bundle.getBoolean(HANG_FLAG, false);
                            if (hanged) {
                                Toast.makeText(JunkCleanActivity.this, "Cleanup system cache exception！", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                        break;

                    case MSG_PROCESS_BEGIN:
                        break;

                    case MSG_PROCESS_POS:
                        mHeaderView.mProgress.setText("Scanning:" + ((JunkInfo) msg.obj).mPackageName);
                        mHeaderView.mSize.setText(CleanUtil.formatShortFileSize(JunkCleanActivity.this, getTotalSize()));
                        break;

                    case MSG_PROCESS_FINISH:
                        mIsProcessScanFinish = true;
                        checkScanFinish();
                        break;

                    case MSG_PROCESS_CLEAN_FINISH:
                        mIsProcessCleanFinish = true;
                        checkCleanFinish();
                        break;

                    case MSG_OVERALL_BEGIN:
                        break;

                    case MSG_OVERALL_POS:
                        mHeaderView.mProgress.setText("Scanning:" + ((JunkInfo) msg.obj).mPath);
                        mHeaderView.mSize.setText(CleanUtil.formatShortFileSize(JunkCleanActivity.this, getTotalSize()));
                        break;

                    case MSG_OVERALL_FINISH:
                        mIsOverallScanFinish = true;
                        checkScanFinish();
                        break;

                    case MSG_OVERALL_CLEAN_FINISH:
                        mIsOverallCleanFinish = true;
                        checkCleanFinish();
                        break;
                }
            }
        };

        mCleanButton = (Button) findViewById(R.id.do_junk_clean);
        mCleanButton.setEnabled(false);
        mCleanButton.setVisibility(View.INVISIBLE);
        mCleanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCleanButton.setVisibility(View.INVISIBLE);
                mCleanButton.setEnabled(false);
                clearAll();
            }
        });

        resetState();

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.junk_list);
        mHeaderView = new ListHeaderView(this, listView);
        mHeaderView.mProgress.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        listView.addHeaderView(mHeaderView);
        listView.setGroupIndicator(null);
        listView.setChildIndicator(null);
        listView.setDividerHeight(0);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                JunkInfo info = (JunkInfo) mAdapter.getChild(groupPosition, childPosition);
                if (groupPosition == JunkGroup.GROUP_APK ||
                        info.mIsChild ||
                        (groupPosition == JunkGroup.GROUP_ADV && !info.mIsChild && info.mPath != null)) {
                    if (info.mPath != null) {
                        Toast.makeText(JunkCleanActivity.this, info.mPath, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int childrenInThisGroup = mAdapter.getChildrenCount(groupPosition);
                    for (int i = childPosition + 1; i < childrenInThisGroup; i++) {
                        JunkInfo child = (JunkInfo) mAdapter.getChild(groupPosition, i);
                        if (!child.mIsChild) {
                            break;
                        }

                        child.mIsVisible = !child.mIsVisible;
                    }
                    mAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        mAdapter = new BaseExpandableListAdapter() {
            @Override
            public int getGroupCount() {
                return mJunkGroups.size();
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                if (mJunkGroups.get(groupPosition).mChildren != null) {
                    return mJunkGroups.get(groupPosition).mChildren.size();
                } else {
                    return 0;
                }
            }

            @Override
            public Object getGroup(int groupPosition) {
                return mJunkGroups.get(groupPosition);
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return mJunkGroups.get(groupPosition).mChildren.get(childPosition);
            }

            @Override
            public long getGroupId(int groupPosition) {
                return 0;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                GroupViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(JunkCleanActivity.this)
                            .inflate(R.layout.group_list, null);
                    holder = new GroupViewHolder();
                    holder.mPackageNameTv = (TextView) convertView.findViewById(R.id.package_name);
                    holder.mPackageSizeTv = (TextView) convertView.findViewById(R.id.package_size);
                    holder.groupCheck = convertView.findViewById(R.id.select_group);
                    convertView.setTag(holder);
                } else {
                    holder = (GroupViewHolder) convertView.getTag();
                }

                final JunkGroup group = mJunkGroups.get(groupPosition);
                holder.mPackageNameTv.setText(group.mName);
                holder.mPackageSizeTv.setText(CleanUtil.formatShortFileSize(JunkCleanActivity.this, group.mSize));
                holder.groupCheck.setChecked(group.isChecked);
                holder.groupCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        group.isChecked = b;
                    }
                });
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                final JunkInfo info = mJunkGroups.get(groupPosition).mChildren.get(childPosition);

                if (info.mIsVisible) {
                    ChildViewHolder holder;
                    if (info.mIsChild) {
                        convertView = LayoutInflater.from(JunkCleanActivity.this)
                                .inflate(R.layout.level2_item_list, null);
                    } else {
                        convertView = LayoutInflater.from(JunkCleanActivity.this)
                                .inflate(R.layout.level1_item_list, null);
                    }
                    holder = new ChildViewHolder();
                    holder.mJunkTypeTv = (TextView) convertView.findViewById(R.id.junk_type);
                    holder.mJunkSizeTv = (TextView) convertView.findViewById(R.id.junk_size);
                    holder.checkBox = convertView.findViewById(R.id.checkbox1);

                    holder.mJunkTypeTv.setText(info.name);
                    holder.mJunkSizeTv.setText(CleanUtil.formatShortFileSize(JunkCleanActivity.this, info.mSize));
                    holder.checkBox.setChecked(info.isChecked);
                    holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            info.isChecked = b;
                        }
                    });
                } else {
                    convertView = LayoutInflater.from(JunkCleanActivity.this)
                            .inflate(R.layout.item_null, null);
                }

                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        };

        listView.setAdapter(mAdapter);

        if (!mIsScanning) {
            mIsScanning = true;
            startScan();
        }
    }


    private Animation slideUp;

    private void slideUpCongrats() {
        this.slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LottieAnimationView viewById = findViewById(R.id.animation_success);
                viewById.playAnimation();
                findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
//                AdMobAdsManager.getInstance().showInterstitialAd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    private void clearAll() {
        Thread clearThread = new Thread(new Runnable() {
            @Override
            public void run() {
                JunkGroup processGroup = mJunkGroups.get(JunkGroup.GROUP_PROCESS);
                if (processGroup.isChecked) {
                    for (JunkInfo info : processGroup.mChildren) {
                        if (info.isChecked) {
                            CleanUtil.killAppProcesses(info.mPackageName);
                        }
                    }
                }
                Message msg = handler.obtainMessage(JunkCleanActivity.MSG_PROCESS_CLEAN_FINISH);
                msg.sendToTarget();
                msg = handler.obtainMessage(JunkCleanActivity.MSG_SYS_CACHE_BEGIN);
                msg.sendToTarget();
                if (mJunkGroups.get(JunkGroup.GROUP_CACHE) != null && mJunkGroups.get(JunkGroup.GROUP_CACHE).isChecked) {
                    for (JunkInfo info : mJunkGroups.get(JunkGroup.GROUP_CACHE).mChildren) {
                        if (info.isChecked) {
                            for (JunkInfo junkInfo : info.mChildren) {
                                if (junkInfo.isChecked) {
                                    CleanUtil.freeAppCache(junkInfo.mPackageName);
                                }
                            }
                        }
                    }
                }
                msg = handler.obtainMessage(JunkCleanActivity.MSG_SYS_CACHE_CLEAN_FINISH);
                msg.sendToTarget();
                ArrayList<JunkInfo> junks = new ArrayList<>();
                JunkGroup group = mJunkGroups.get(JunkGroup.GROUP_APK);
                if (group.isChecked) {
                    for (JunkInfo info : group.mChildren) {
                        if (info.isChecked) {
                            junks.add(info);
                        }
                    }
                }
                group = mJunkGroups.get(JunkGroup.GROUP_LOG);
                if (group.isChecked) {
                    for (JunkInfo info : group.mChildren) {
                        if (info.isChecked) {
                            junks.add(info);
                        }
                    }
                }

                group = mJunkGroups.get(JunkGroup.GROUP_TMP);
                if (group.isChecked) {
                    for (JunkInfo info : group.mChildren) {
                        if (info.isChecked) {
                            junks.add(info);
                        }
                    }
                }
                CleanUtil.freeJunkInfos(junks, handler);
            }
        });
        clearThread.start();
    }

    private void resetState() {
        mIsScanning = false;

        mIsSysCacheScanFinish = false;
        mIsSysCacheCleanFinish = false;

        mIsProcessScanFinish = false;
        mIsProcessCleanFinish = false;

        mJunkGroups = new HashMap<>();

        mCleanButton.setEnabled(false);
        mCleanButton.setVisibility(View.INVISIBLE);

        JunkGroup cacheGroup = new JunkGroup();
        cacheGroup.mName = getString(R.string.cache_clean);
        cacheGroup.mChildren = new ArrayList<>();
        mJunkGroups.put(JunkGroup.GROUP_CACHE, cacheGroup);

        JunkGroup processGroup = new JunkGroup();
        processGroup.mName = getString(R.string.process_clean);
        processGroup.mChildren = new ArrayList<>();
        mJunkGroups.put(JunkGroup.GROUP_PROCESS, processGroup);

        JunkGroup apkGroup = new JunkGroup();
        apkGroup.mName = getString(R.string.apk_clean);
        apkGroup.mChildren = new ArrayList<>();
        mJunkGroups.put(JunkGroup.GROUP_APK, apkGroup);

        JunkGroup tmpGroup = new JunkGroup();
        tmpGroup.mName = getString(R.string.tmp_clean);
        tmpGroup.mChildren = new ArrayList<>();
        mJunkGroups.put(JunkGroup.GROUP_TMP, tmpGroup);

        JunkGroup logGroup = new JunkGroup();
        logGroup.mName = getString(R.string.log_clean);
        logGroup.mChildren = new ArrayList<>();
        mJunkGroups.put(JunkGroup.GROUP_LOG, logGroup);
    }

    private void checkScanFinish() {

        mAdapter.notifyDataSetChanged();

        if (mIsProcessScanFinish && mIsSysCacheScanFinish && mIsOverallScanFinish) {
            mIsScanning = false;

            JunkGroup cacheGroup = mJunkGroups.get(JunkGroup.GROUP_CACHE);
            ArrayList<JunkInfo> children = cacheGroup.mChildren;
            cacheGroup.mChildren = new ArrayList<>();
            for (JunkInfo info : children) {
                cacheGroup.mChildren.add(info);
                if (info.mChildren != null) {
                    cacheGroup.mChildren.addAll(info.mChildren);
                }
            }
            children = null;

            long size = getTotalSize();
            String totalSize = CleanUtil.formatShortFileSize(this, size);
            mHeaderView.mSize.setText(totalSize);
            mHeaderView.mProgress.setText("Total Found: " + totalSize);
            mCleanButton.setVisibility(View.VISIBLE);
            mHeaderView.mProgress.setGravity(Gravity.CENTER);
            mHeaderView.animationView.setVisibility(View.GONE);
            mHeaderView.animationView.cancelAnimation();
            mCleanButton.setEnabled(true);
        }
    }

    private void checkCleanFinish() {
        if (mIsProcessCleanFinish && mIsSysCacheCleanFinish && mIsOverallCleanFinish) {
//            mHeaderView.mProgress.setText("Clean up");
//            mHeaderView.mSize.setText(CleanUtil.formatShortFileSize(this, 0L));

            for (JunkGroup group : mJunkGroups.values()) {
                group.mSize = 0L;
                group.mChildren = null;
            }

            mAdapter.notifyDataSetChanged();
            congratsView.setVisibility(View.VISIBLE);
            congratsView.startAnimation(slideUp);
            slideUpCongrats();
        }
    }

    private void startScan() {

        ProcessScanTask processScanTask = new ProcessScanTask(new IScanCallback() {
            @Override
            public void onBegin() {
                Message msg = handler.obtainMessage(MSG_PROCESS_BEGIN);
                msg.sendToTarget();
            }

            @Override
            public void onProgress(JunkInfo info) {
                Message msg = handler.obtainMessage(MSG_PROCESS_POS);
                msg.obj = info;
                msg.sendToTarget();
            }

            @Override
            public void onFinish(ArrayList<JunkInfo> children) {
                JunkGroup cacheGroup = mJunkGroups.get(JunkGroup.GROUP_PROCESS);
                cacheGroup.mChildren.addAll(children);
                for (JunkInfo info : children) {
                    cacheGroup.mSize += info.mSize;
                }
                Message msg = handler.obtainMessage(MSG_PROCESS_FINISH);
                msg.sendToTarget();
            }
        });
        processScanTask.execute();

        SysCacheScanTask sysCacheScanTask = new SysCacheScanTask(new IScanCallback() {
            @Override
            public void onBegin() {
                Message msg = handler.obtainMessage(MSG_SYS_CACHE_BEGIN);
                msg.sendToTarget();
            }

            @Override
            public void onProgress(JunkInfo info) {
                Message msg = handler.obtainMessage(MSG_SYS_CACHE_POS);
                msg.obj = info;
                msg.sendToTarget();
            }

            @Override
            public void onFinish(ArrayList<JunkInfo> children) {
                JunkGroup cacheGroup = mJunkGroups.get(JunkGroup.GROUP_CACHE);
                cacheGroup.mChildren.addAll(children);
                Collections.sort(cacheGroup.mChildren);
                Collections.reverse(cacheGroup.mChildren);
                for (JunkInfo info : children) {
                    cacheGroup.mSize += info.mSize;
                }
                Message msg = handler.obtainMessage(MSG_SYS_CACHE_FINISH);
                msg.sendToTarget();
            }
        });
        sysCacheScanTask.execute();

        OverallScanTask overallScanTask = new OverallScanTask(new IScanCallback() {
            @Override
            public void onBegin() {
                Message msg = handler.obtainMessage(MSG_OVERALL_BEGIN);
                msg.sendToTarget();
            }

            @Override
            public void onProgress(JunkInfo info) {
                Message msg = handler.obtainMessage(MSG_OVERALL_POS);
                msg.obj = info;
                msg.sendToTarget();
            }

            @Override
            public void onFinish(ArrayList<JunkInfo> children) {
                for (JunkInfo info : children) {
                    String path = info.mChildren.get(0).mPath;
                    int groupFlag = 0;
                    if (path.endsWith(".apk")) {
                        groupFlag = JunkGroup.GROUP_APK;
                    } else if (path.endsWith(".log")) {
                        groupFlag = JunkGroup.GROUP_LOG;
                    } else if (path.endsWith(".tmp") || path.endsWith(".temp")) {
                        groupFlag = JunkGroup.GROUP_TMP;
                    }

                    JunkGroup cacheGroup = mJunkGroups.get(groupFlag);
                    cacheGroup.mChildren.addAll(info.mChildren);
                    cacheGroup.mSize = info.mSize;
                }

                Message msg = handler.obtainMessage(MSG_OVERALL_FINISH);
                msg.sendToTarget();
            }
        });
        overallScanTask.execute();
    }

    private long getTotalSize() {
        long size = 0L;
        for (JunkGroup group : mJunkGroups.values()) {
            size += group.mSize;
        }
        return size;
    }

    public static class GroupViewHolder {
        public TextView mPackageNameTv;
        public TextView mPackageSizeTv;
        public CheckBox groupCheck;
    }

    public static class ChildViewHolder {
        public TextView mJunkTypeTv;
        public TextView mJunkSizeTv;
        public CheckBox checkBox;
    }
}
