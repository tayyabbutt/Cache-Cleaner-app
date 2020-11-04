package com.mobxpert.supercleaner.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.adapters.RunningAppsAdapter;
import com.mobxpert.supercleaner.databinding.ActivityMemoryBoosterBinding;
import com.mobxpert.supercleaner.listeners.OnRunningAppSelectedListener;
import com.mobxpert.supercleaner.managers.SharedPreferencesManager;
import com.mobxpert.supercleaner.models.Process;
import com.mobxpert.supercleaner.models.RunningApp;
import com.mobxpert.supercleaner.utils.ProcessManager;
import com.mobxpert.supercleaner.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MemoryBoosterActivity extends AppCompatActivity implements OnRunningAppSelectedListener {

    private static final String TAG = MemoryBoosterActivity.class.getName();
    ActivityMemoryBoosterBinding binding;
    private Animation slideUp;
    private PackageManager packageManager;
    private ActivityManager activityManager;
    private Thread workerThread;
    private ArrayList<Process> runningApps;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        public void run() {
            MemoryBoosterActivity.this.setupRecyclerView();
            if (MemoryBoosterActivity.this.handler != null) {
                MemoryBoosterActivity.this.handler.postDelayed(this, 500);
            }
        }
    };
    private RunningAppsAdapter runningAppsAdapter;
    private boolean isAnimating;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_memory_booster);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.adView);
        adContainer.addView(adView);
        adView.loadAd();

        //   AdMobAdsManager.getInstance().loadBanner(binding.adContainer);
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

    @Override
    protected void onStart() {
        boolean isRamBoosted;
        super.onStart();
        if ((Calendar.getInstance().getTimeInMillis() - SharedPreferencesManager.getInstance().getLong(getString(R.string.ram_boosted))) / 1000 <= 10) {
            isRamBoosted = true;
        } else {
            isRamBoosted = false;
        }
        if (isRamBoosted) {
            binding.total.setText(null);
            binding.unit.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.selected.setText(R.string.ram_is_boosted);
            binding.done.setVisibility(View.GONE);
            binding.congrats.setVisibility(View.GONE);
            binding.congratsView.setVisibility(View.VISIBLE);
            return;
        }
        //   AdMobAdsManager.getInstance().loadBanner(binding.adView);
        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.ad_container);
        adContainer.addView(adView);
        adView.loadAd();
        Glide.with(this).load(R.raw.rocket).into(binding.rocket);
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.congratsView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down));
        this.packageManager = getPackageManager();
        this.activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        getAppsRunningInBackground();
        binding.boost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initBooster();
            }
        });
    }

    private void initBooster() {
        if (this.runningApps != null && this.runningApps.size() > 0) {
            this.isAnimating = true;
            binding.rocketView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.rocketView.setVisibility(View.GONE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = MemoryBoosterActivity.this.runningApps.size() - 1; i >= 0; i--) {
                                        Process process = (Process) MemoryBoosterActivity.this.runningApps.get(i);
                                        if (process.isSelected()) {
                                            MemoryBoosterActivity.this.activityManager.killBackgroundProcesses(process.getPackageName());
                                            MemoryBoosterActivity.this.runningApps.remove(i);
                                            if (MemoryBoosterActivity.this.runningAppsAdapter != null) {
                                                MemoryBoosterActivity.this.runningAppsAdapter.notifyItemRemoved(i);
                                            }
                                        }
                                    }
                                    MemoryBoosterActivity.this.updateDashboard();
                                    SharedPreferencesManager.getInstance().setLong(MemoryBoosterActivity.this.getString(R.string.ram_boosted), Calendar.getInstance().getTimeInMillis());
                                    MemoryBoosterActivity.this.isAnimating = false;
                                    binding.congratsView.setVisibility(View.VISIBLE);
                                    binding.congratsView.startAnimation(slideUp);
                                    slideUpCongrats();
                                }
                            }, 100);
                        }
                    }, 500);
                }
            }, 5000);
        }
    }

    private void slideUpCongrats() {
        this.slideUp.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                binding.congrats.playAnimation();
                binding.done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
                //              AdMobAdsManager.getInstance().showInterstitialAd();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void getAppsRunningInBackground() {
        this.workerThread = new Thread(new Runnable() {
            public void run() {
                MemoryBoosterActivity.this.runningApps = MemoryBoosterActivity.this.getProcessesRunningInBackground();
                if (MemoryBoosterActivity.this.workerThread != null && !MemoryBoosterActivity.this.workerThread.isInterrupted()) {
                    MemoryBoosterActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.boost.setVisibility(View.VISIBLE);
                            MemoryBoosterActivity.this.removeHandlerCallBacks();
                            binding.animationLoading.cancelAnimation();
                            binding.animationLoading.setVisibility(View.GONE);
                            MemoryBoosterActivity.this.setupRecyclerView();
                        }
                    });
                }
            }
        });
        this.workerThread.start();
        this.handler.postDelayed(this.runnable, 0);
    }

    private void setupRecyclerView() {
        if (binding.progressBar.getVisibility() == View.VISIBLE) {
            binding.progressBar.setVisibility(View.GONE);
        }
        if (this.runningApps == null || this.runningApps.size() <= 0) {
            binding.boost.setVisibility(View.GONE);
            binding.message.setVisibility(View.VISIBLE);
            return;
        }
        if (binding.message.getVisibility() == View.VISIBLE) {
            binding.message.setVisibility(View.GONE);
        }
        if (this.runningAppsAdapter == null) {
            this.runningAppsAdapter = new RunningAppsAdapter(this.runningApps, this);
            binding.runningAppsList.setLayoutManager(new LinearLayoutManager(this));
            binding.runningAppsList.setAdapter(this.runningAppsAdapter);
        }
        this.runningAppsAdapter.notifyDataSetChanged();
        updateDashboard();
    }

    private void updateDashboard() {
        long totalSize = getTotalSize();
        if (totalSize > 0) {
            bytes2String(totalSize);
            binding.selected.setText("Selected: " + Utils.bytes2String(getTotalSizeOfSelectedApps()).split(" ")[0]);
        } else {
            binding.boost.setVisibility(View.GONE);
        }
    }


    private void bytes2String(long length) {
        NumberFormat numberFormat = new DecimalFormat();
        numberFormat.setMaximumFractionDigits(1);
        if (((double) length) < 1024.0d) {

            try {
                binding.total.setText(numberFormat.format(length));
                binding.unit.setText("Byte(s)");
            } catch (Exception e) {
                binding.total.setText(length + "");
                binding.unit.setText("Byte(s)");
            }
        } else if (((double) length) < 1048576.0d) {
            binding.total.setText(numberFormat.format(((double) length) / 1024.0d));
            binding.unit.setText("KB");
        } else {
            if (((double) length) < 1.073741824E9d) {
                binding.total.setText(numberFormat.format(((double) length) / 1048576.0d));
                binding.unit.setText("MB");
            } else if (((double) length) < 1.099511627776E12d) {
                binding.total.setText(numberFormat.format(((double) length) / 1.073741824E9d));
                binding.unit.setText("GB");
            } else {
                binding.total.setText(numberFormat.format(((double) length) / 1.099511627776E12d));
                binding.unit.setText("TB");
            }
        }
    }

    private long getTotalSize() {
        long totalSize = 0;
        if (this.runningApps != null && this.runningApps.size() > 0) {
            Iterator it = this.runningApps.iterator();
            while (it.hasNext()) {
                totalSize += ((Process) it.next()).getSize();
            }
        }
        return totalSize;
    }

    private long getTotalSizeOfSelectedApps() {
        long totalSize = 0;
        ArrayList<Process> clone = (ArrayList<Process>) runningApps.clone();
        if (clone != null && clone.size() > 0) {
            Iterator it = clone.iterator();
            while (it.hasNext()) {
                Process process = (Process) it.next();
                if (process.isSelected()) {
                    totalSize += process.getSize();
                }
            }
        }
        return totalSize;
    }

    private ArrayList<RunningApp> getRunningApps() {
        HashMap<String, RunningApp> linkedHashMap = new HashMap();
        for (ProcessManager.Process process : ProcessManager.getProcesses()) {
            if (!process.getInitialPart().contains(getPackageName())) {
                RunningApp runningApp = (RunningApp) linkedHashMap.get(process.getInitialPart());
                if (runningApp == null) {
                    runningApp = new RunningApp(process.getInitialPart());
                    linkedHashMap.put(process.getInitialPart(), runningApp);
                }
                runningApp.addPID(process.var5);
            }
        }
        return new ArrayList(linkedHashMap.values());
    }

    private ArrayList<RunningApp> getRunningServices() {
        HashMap<String, RunningApp> linkedHashMap = new HashMap();
        for (ActivityManager.RunningServiceInfo runningServiceInfo : this.activityManager.getRunningServices(500)) {
            if (!runningServiceInfo.service.getPackageName().contains(getPackageName())) {
                RunningApp runningApp = (RunningApp) linkedHashMap.get(runningServiceInfo.service.getPackageName());
                if (runningApp == null) {
                    runningApp = new RunningApp(runningServiceInfo.service.getPackageName());
                    linkedHashMap.put(runningServiceInfo.service.getPackageName(), runningApp);
                }
                runningApp.addPID(runningServiceInfo.pid);
            }
        }
        return new ArrayList(linkedHashMap.values());
    }

    private RunningApp getMatchingRunningApp(String str, List<RunningApp> list) {
        try {
            for (RunningApp runningApp : list) {
                if (runningApp.getPackageName().equalsIgnoreCase(str)) {
                    return runningApp;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Process> getProcessesRunningInBackground() {
        ArrayList<Process> processes = new ArrayList();
        try {
            ArrayList<RunningApp> runningApps = getRunningApps();
            if (runningApps.size() == 0) {
                runningApps = getRunningServices();
            }
            if (runningApps.size() > 0) {
                Intent intent = new Intent("android.intent.action.MAIN", null);
                intent.addCategory("android.intent.category.LAUNCHER");
                for (ResolveInfo resolveInfo : this.packageManager.queryIntentActivities(intent, 0)) {
                    RunningApp runningApp = getMatchingRunningApp(resolveInfo.activityInfo.packageName, runningApps);
                    if (runningApp != null) {
                        long j;
                        int[] iArr = new int[runningApp.getPid().size()];
                        for (int i = 0; i < runningApp.getPid().size(); i++) {
                            iArr[i] = ((Integer) runningApp.getPid().get(i)).intValue();
                        }
                        Debug.MemoryInfo[] processMemoryInfo = this.activityManager.getProcessMemoryInfo(iArr);
                        if (processMemoryInfo != null) {
                            j = 0;
                            for (Debug.MemoryInfo totalPrivateDirty : processMemoryInfo) {
                                j += (long) (totalPrivateDirty.getTotalPrivateDirty() * 1024);
                            }
                        } else {
                            j = 0;
                        }
                        if (j > 0) {
                            Process process = new Process();
                            process.setSize(j);
                            process.setSelected(true);
                            process.setName((String) resolveInfo.loadLabel(this.packageManager));
                            process.setPackageName(resolveInfo.activityInfo.packageName);
                            process.setIcon(resolveInfo.loadIcon(this.packageManager));
                            processes.add(process);
                        }
                    }
                    this.runningApps = processes;
                }
                Collections.sort(processes);
            }
        } catch (Exception e) {
            Log.e(this.TAG, "getProcessesRunningInBackground exception: " + e.getLocalizedMessage());
        }
        return processes;
    }


    private void removeHandlerCallBacks() {
        if (this.handler != null && this.runnable != null) {
            this.handler.removeCallbacks(this.runnable);
        }
    }

    @Override
    public void onAppSelected(boolean selected, Process process) {
        updateDashboard();
    }

    @Override
    protected void onDestroy() {
        removeHandlerCallBacks();
        if (this.workerThread != null) {
            this.workerThread.interrupt();
        }
        if (this.runningApps != null) {
            this.runningApps.clear();
        }
        super.onDestroy();
    }
}
