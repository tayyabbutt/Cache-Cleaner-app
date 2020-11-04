package com.mobxpert.supercleaner.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.AppManagerAdapter;
import com.mobxpert.supercleaner.databinding.ActivityAppManagerBinding;
import com.mobxpert.supercleaner.fragments.UnInstallFragment;
import com.mobxpert.supercleaner.listeners.GetApkFilesListener;
import com.mobxpert.supercleaner.listeners.GetInstalledAppsListener;
import com.mobxpert.supercleaner.models.ApkFile;
import com.mobxpert.supercleaner.models.InstalledApp;
import com.mobxpert.supercleaner.threads.ConverterThread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static android.content.pm.PackageManager.GET_META_DATA;

public class AppManagerActivity extends AppCompatActivity implements GetInstalledAppsListener, GetApkFilesListener {

    private final String TAG = AppManagerActivity.class.getSimpleName();
    private ActivityAppManagerBinding binding;

    private ArrayList<ApkFile> apkFiles;
    private Thread apkFilesThread;
    private AppManagerAdapter appManagerAdapter;
    private ArrayList<ApplicationInfo> applicationInfos;
    private ArrayList<InstalledApp> installedApps;
    private Thread installedAppsThread;
    private boolean isApkFilesCollected = false;
    private boolean isExecutorFinished = false;
    private PackageManager packageManager;
    private Queue<ApplicationInfo> queue;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_manager);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Application Manager");
        }
        this.packageManager = getPackageManager();
        this.appManagerAdapter = new AppManagerAdapter(this, getSupportFragmentManager());
        binding.pages.setAdapter(this.appManagerAdapter);
        binding.tabs.setupWithViewPager(binding.pages, true);
        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.adView);
        adContainer.addView(adView);
        adView.loadAd();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!this.isExecutorFinished) {
            this.installedAppsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (packageManager != null) {
                        installedApps = new ArrayList();
                        applicationInfos = new ArrayList(packageManager.getInstalledApplications(GET_META_DATA));
                        Log.d(TAG, "got all application info");
                        queue = new LinkedBlockingQueue();
                        queue.addAll(applicationInfos);
                        Log.d(TAG, "all application info inserted in queue");
                        ExecutorService executor = Executors.newFixedThreadPool(10);
                        if (executor != null) {
                            int i;
                            for (i = 0; i < 10; i++) {
                                executor.execute(new ConverterThread(i + 1, getPackageManager(), queue, installedApps));
                            }
                            executor.shutdown();
                            do {
                            } while (!executor.isTerminated());
                            queue.clear();
                            applicationInfos.clear();
                            Log.d(TAG, "finished all threads of executor service");
                            for (i = installedApps.size() - 1; i >= 0; i--) {
                                InstalledApp installedApp = (InstalledApp) installedApps.get(i);
                                if (installedApp == null || installedApp.getPackageName() == null) {
                                    installedApps.remove(i);
                                }
                            }
                            isExecutorFinished = true;
                        }
                    }
                }
            });
            this.installedAppsThread.start();
        }
        if (!this.isApkFilesCollected) {
            getAllApkFilesInPhone();
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

    private void getAllApkFilesInPhone() {
        this.apkFilesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "apk files collection started");
                File rootDirectory = Environment.getExternalStorageDirectory();
                Log.d(TAG, "external storage directory: " + rootDirectory);
                getAllRequiredContent(rootDirectory);
                String[] externalStoragePaths = getExternalStorageDirectories();
                if (externalStoragePaths != null && externalStoragePaths.length > 0) {
                    Log.d(TAG, "total Paths are: " + externalStoragePaths.length);
                    for (String path : externalStoragePaths) {
                        File file = new File(path);
                        if (file.exists()) {
                            File[] subFiles = file.listFiles();
                            if (subFiles != null && subFiles.length > 0) {
                                for (File subFile : subFiles) {
                                    if (subFile != null) {
                                        getAllRequiredContent(subFile);
                                    }
                                }
                            } else if (file.isFile()) {
                                getAllRequiredContent(file);
                            }
                        }
                    }
                }
                if (apkFiles != null) {
                    Log.d(TAG, "total apk files: " + apkFiles.size());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isApkFilesCollected = true;
                        Log.d(TAG, "apk files collection finished");
                    }
                });
            }
        });
        this.apkFilesThread.start();
    }

    public String[] getExternalStorageDirectories() {
        int i;
        List<String> results = new ArrayList();
        if (Build.VERSION.SDK_INT >= 19) {
            File[] externalDirs = getExternalFilesDirs(null);
            if (externalDirs != null && externalDirs.length > 0) {
                for (File file : externalDirs) {
                    if (file != null) {
                        String[] paths = file.getPath().split("/Android");
                        if (paths != null && paths.length > 0) {
                            boolean addPath;
                            String path = paths[0];
                            if (Build.VERSION.SDK_INT >= 21) {
                                addPath = Environment.isExternalStorageRemovable(file);
                            } else {
                                addPath = "mounted".equals(EnvironmentCompat.getStorageState(file));
                            }
                            if (addPath) {
                                results.add(path);
                            }
                        }
                    }
                }
            }
        }
        if (results.isEmpty()) {
            String output = "";
            InputStream is = null;
            try {
                Process process = new ProcessBuilder(new String[0]).command(new String[]{"mount | grep /dev/block/vold"}).redirectErrorStream(true).start();
                process.waitFor();
                is = process.getInputStream();
                byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (Exception e) {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                        Log.e(this.TAG, "inner catch (closing input stream) exception: " + e.getLocalizedMessage());
                    }
                }
                Log.e(this.TAG, "outer catch exception: " + e.getLocalizedMessage());
            }
            if (!output.trim().isEmpty()) {
                String[] devicePoints = output.split("\n");
                if (devicePoints.length > 0) {
                    for (String voldPoint : devicePoints) {
                        results.add(voldPoint.split(" ")[2]);
                    }
                }
            }
        }
        int i2;
        if (Build.VERSION.SDK_INT >= 23) {
            i = 0;
            while (i < results.size()) {
                if (!((String) results.get(i)).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d(this.TAG, ((String) results.get(i)) + " might not be extSDcard");
                    i2 = i - 1;
                    results.remove(i);
                    i = i2;
                }
                i++;
            }
        } else {
            i = 0;
            while (i < results.size()) {
                if (!(((String) results.get(i)).toLowerCase().contains("ext") || ((String) results.get(i)).toLowerCase().contains("sdcard"))) {
                    Log.d(this.TAG, ((String) results.get(i)) + " might not be extSDcard");
                    i2 = i - 1;
                    results.remove(i);
                    i = i2;
                }
                i++;
            }
        }
        String[] storageDirectories = new String[results.size()];
        for (i = 0; i < results.size(); i++) {
            storageDirectories[i] = (String) results.get(i);
        }
        return storageDirectories;
    }

    private void getAllRequiredContent(File directory) {
        if (directory == null) {
            return;
        }
        if (directory.isDirectory()) {
            File[] subFiles = directory.listFiles();
            if (subFiles != null && subFiles.length > 0) {
                for (File file : subFiles) {
                    if (file != null) {
                        if (file.isDirectory()) {
                            getAllRequiredContent(file);
                        } else {
                            detectFileTypeAndAddInCategory(file);
                        }
                    }
                }
            }
        } else if (directory.isFile()) {
            detectFileTypeAndAddInCategory(directory);
        }
    }

    private void detectFileTypeAndAddInCategory(File file) {
        if (file.exists() && file.getName().endsWith(".apk")) {
            if (this.apkFiles == null) {
                this.apkFiles = new ArrayList();
            }
            if (this.packageManager == null) {
                this.packageManager = getPackageManager();
            }
            PackageInfo packageInfo = this.packageManager.getPackageArchiveInfo(file.getAbsolutePath(), GET_META_DATA);
            if (packageInfo != null) {
                try {
                    ApkFile apkFile = new ApkFile();
                    apkFile.setFile(file);
                    apkFile.setPackageName(packageInfo.packageName);
                    packageInfo.applicationInfo.sourceDir = file.getAbsolutePath();
                    packageInfo.applicationInfo.publicSourceDir = file.getAbsolutePath();
                    if (packageInfo.applicationInfo != null) {
                        apkFile.setFlag(packageInfo.applicationInfo.flags);
                        apkFile.setEnabled(packageInfo.applicationInfo.enabled);
                        apkFile.setIcon(packageInfo.applicationInfo.loadIcon(this.packageManager));
                        apkFile.setName(packageInfo.applicationInfo.loadLabel(this.packageManager).toString());
                    }
                    this.apkFiles.add(apkFile);
                } catch (Exception e) {
                    Log.e(this.TAG, "detectFileTypeAndAddInCategory get application info from archive/apk file: " + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public ArrayList<ApkFile> getApkFiles() {
        return this.apkFiles;
    }

    @Override
    public boolean isApkFilesCollected() {
        return this.isApkFilesCollected;
    }

    @Override
    public void onApkInstalled(ApkFile apkFile) {
        if (apkFile != null) {
            InstalledApp installedApp = new InstalledApp();
            installedApp.setFlag(apkFile.getFlag());
            installedApp.setName(apkFile.getName());
            installedApp.setIcon(apkFile.getIcon());
            installedApp.setEnabled(apkFile.isEnabled());
            installedApp.setPackageName(apkFile.getPackageName());
            if (this.appManagerAdapter != null) {
                UnInstallFragment unInstallFragment = this.appManagerAdapter.getUnInstallFragment();
                if (unInstallFragment != null) {
                    unInstallFragment.addInstalledAppInList(installedApp);
                }
            }
        }
    }

    @Override
    public ArrayList<InstalledApp> getInstalledApps() {
        return this.installedApps;
    }

    @Override
    public boolean isExecutorFinished() {
        if (this.queue != null && this.queue.isEmpty() && this.isExecutorFinished) {
            return true;
        }
        return false;
    }
}
