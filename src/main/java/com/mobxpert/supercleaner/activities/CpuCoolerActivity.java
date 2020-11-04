package com.mobxpert.supercleaner.activities;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Debug.MemoryInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.adapters.RunningAppsAdapter;
import com.mobxpert.supercleaner.databinding.ActivityCpuCoolerBinding;
import com.mobxpert.supercleaner.managers.SharedPreferencesManager;
import com.mobxpert.supercleaner.models.Process;
import com.mobxpert.supercleaner.models.RunningApp;
import com.mobxpert.supercleaner.utils.ProcessManager;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.view.View.VISIBLE;

public class CpuCoolerActivity extends AppCompatActivity {
    static final /* synthetic */ boolean $assertionsDisabled = (!CpuCoolerActivity.class.desiredAssertionStatus());
    private final String TAG = CpuCoolerActivity.class.getSimpleName();
    private ActivityManager activityManager;
    private Handler handler = new Handler();
    private boolean isAnimating = false;
    private PackageManager packageManager;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setupRecyclerView();
            if (handler != null) {
                handler.postDelayed(this, 500);
            }
        }
    };
    private ArrayList<Process> runningApps;
    private RunningAppsAdapter runningAppsAdapter;
    private Thread workerThread;
    private Animation slideUp;
    private ActivityCpuCoolerBinding binding;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_cpu_cooler);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //      AdMobAdsManager.getInstance().loadBanner(binding.adContainer);
        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.RECTANGLE_HEIGHT_250);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.adView);
        adContainer.addView(adView);
        adView.loadAd();
        binding.cool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCoolClicked();
            }
        });
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDoneClicked();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!adView.isShown()) {
            adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
            LinearLayout adContainer = (LinearLayout) findViewById(R.id.adView);
            adContainer.addView(adView);
            adView.loadAd();
        }

        AdView adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.ad_container);
        adContainer.addView(adView);
        adView.loadAd();


//        AdMobAdsManager.getInstance().loadBanner(binding.adView);

        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        this.slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.congratsView.startAnimation(slideDown);
        int temp = getCpuTemperature();
        if (temp <= 10 || temp >= 80) {
            temp = (int) (40.0d + (Math.random() * 20.0d));
        }
        if (temp > 0) {
            binding.temperature.setText(getString(R.string.temperature, new Object[]{String.valueOf(temp)}));
            if (temp <= 50) {
                binding.tempLabel.setText(getString(R.string.temperature_label, new Object[]{getString(R.string.cool)}));
            } else {
                binding.tempLabel.setText(getString(R.string.temperature_label, new Object[]{getString(R.string.high)}));
            }
        }
        this.packageManager = getPackageManager();
        this.activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        getAppsRunningInBackground();
        binding.appbar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeHandlerCallBacks();
        if (this.workerThread != null) {
            this.workerThread.interrupt();
        }
        if (this.runningApps != null) {
            this.runningApps.clear();
        }
    }

    @Override
    public void onBackPressed() {
        if (!this.isAnimating) {
            super.onBackPressed();
        }
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

    public int getCpuTemperature() {
        try {
            java.lang.Process p = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            p.waitFor();
            return (int) (Float.parseFloat(new BufferedReader(new InputStreamReader(p.getInputStream())).readLine()) / 1000.0f);
        } catch (Exception e) {
            Log.e(this.TAG, "getCpuTemperature: " + e.getLocalizedMessage());
            return 0;
        }
    }

    private void removeHandlerCallBacks() {
        if (this.handler != null && this.runnable != null) {
            this.handler.removeCallbacks(this.runnable);
        }
    }

    private void getAppsRunningInBackground() {
        this.workerThread = new Thread(new Runnable() {
            @Override
            public void run() {

                runningApps = getProcessesRunningInBackground();
                if (workerThread != null && !workerThread.isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.cool.setVisibility(VISIBLE);
                            removeHandlerCallBacks();
                            setupRecyclerView();
                        }
                    });
                }
            }
        });
        this.workerThread.start();
        this.handler.postDelayed(this.runnable, 0);
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
                        MemoryInfo[] processMemoryInfo = this.activityManager.getProcessMemoryInfo(iArr);
                        if (processMemoryInfo != null) {
                            j = 0;
                            for (MemoryInfo totalPrivateDirty : processMemoryInfo) {
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
        for (RunningServiceInfo runningServiceInfo : this.activityManager.getRunningServices(500)) {
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

    private void setupRecyclerView() {
        if (binding.progressBar.getVisibility() == View.VISIBLE) {
            binding.progressBar.setVisibility(View.GONE);
        }
        if (this.runningApps == null || this.runningApps.size() <= 0) {
            binding.cool.setVisibility(View.GONE);
            binding.message.setVisibility(View.VISIBLE);
            return;
        }
        if (binding.message.getVisibility() == View.VISIBLE) {
            binding.message.setVisibility(View.GONE);
        }
        if (this.runningAppsAdapter == null) {
            this.runningAppsAdapter = new RunningAppsAdapter(this.runningApps, null);
            binding.runningAppsList.setLayoutManager(new LinearLayoutManager(this));
            binding.runningAppsList.setAdapter(this.runningAppsAdapter);
        }
        this.runningAppsAdapter.notifyDataSetChanged();
    }

    void onCoolClicked() {
        initCooler();
    }

    private void initCooler() {
        if (this.runningApps != null && this.runningApps.size() > 0) {
            this.isAnimating = true;
            binding.fanView.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator rotation = ObjectAnimator.ofFloat(binding.fan, "rotation", new float[]{0.0f, 360.0f});
                    rotation.setDuration(300);
                    rotation.setRepeatCount(10);
                    rotation.setInterpolator(new LinearInterpolator());
                    rotation.start();
                    rotation.addListener(new AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i = runningApps.size() - 1; i >= 0; i--) {
                                        Process process = (Process) runningApps.get(i);
                                        if (process.isSelected()) {
                                            activityManager.killBackgroundProcesses(process.getPackageName());
                                            runningApps.remove(i);
                                            if (runningAppsAdapter != null) {
                                                runningAppsAdapter.notifyItemRemoved(i);
                                            }
                                        }
                                    }
                                    SharedPreferencesManager.getInstance().setLong(getString(R.string.cpu_cooled), Calendar.getInstance().getTimeInMillis());
                                    if (runningApps.size() == 0 && binding.appbar.getVisibility() == View.VISIBLE) {
                                        binding.cool.setVisibility(View.GONE);
                                        binding.appbar.setVisibility(View.GONE);
                                    }
                                    isAnimating = false;
                                    binding.congratsView.setVisibility(View.VISIBLE);
                                    binding.congratsView.startAnimation(slideUp);
                                    slideUpCongrats();
                                }
                            }, 500);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                }
            }, 500);
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
//                AdMobAdsManager.getInstance().showInterstitialAd();
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    void onDoneClicked() {
        onBackPressed();
    }

    public float getTemp() {
        float b = (float) readProcStatsA();
        float c = (float) readProcStatsB();
        try {
            Thread.sleep(360);
        } catch (Exception e) {
            e.printStackTrace();
        }
        float b2 = (float) readProcStatsB();
        float c2 = (float) readProcStatsB();
        if (b2 - b != 0.0f) {
            return (100.0f * ((b2 - c2) - (b - c))) / (b2 - b);
        }
        return 0.0f;
    }

    public long readProcStatsA() {
        String[] strArr = null;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String readLine = bufferedReader.readLine();
            bufferedReader.close();
            strArr = readLine.split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ($assertionsDisabled || strArr != null) {
            return Long.parseLong(strArr[8]) + (((((Long.parseLong(strArr[2]) + Long.parseLong(strArr[3])) + Long.parseLong(strArr[4])) + Long.parseLong(strArr[6])) + Long.parseLong(strArr[5])) + Long.parseLong(strArr[7]));
        }
        throw new AssertionError();
    }

    public long readProcStatsB() {
        String[] split;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), 1000);
            String readLine = bufferedReader.readLine();
            bufferedReader.close();
            split = readLine.split(" ");
        } catch (IOException e) {
            e.printStackTrace();
            split = null;
        }
        if (split == null || split.length <= 5 || split[5] == null) {
            return 0;
        }
        return Long.parseLong(split[5]);
    }
}
