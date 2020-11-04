package com.mobxpert.supercleaner.threads;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.mobxpert.supercleaner.models.InstalledApp;

import java.util.ArrayList;
import java.util.Queue;

public class ConverterThread implements Runnable {
    private final String TAG = ConverterThread.class.getSimpleName();
    private ArrayList<InstalledApp> installedApps;
    private PackageManager packageManager;
    private Queue<ApplicationInfo> queue;
    private int thread;

    public ConverterThread(int thread, PackageManager packageManager, Queue<ApplicationInfo> queue, ArrayList<InstalledApp> installedApps) {
        this.queue = queue;
        this.thread = thread;
        this.installedApps = installedApps;
        this.packageManager = packageManager;
    }

    public void run() {
        if (this.queue != null && this.queue.size() > 0) {
            while (!this.queue.isEmpty()) {
                try {
                    ApplicationInfo applicationInfo = (ApplicationInfo) this.queue.remove();
                    if (applicationInfo != null) {
                        InstalledApp installedApp = new InstalledApp();
                        installedApp.setFlag(applicationInfo.flags);
                        installedApp.setEnabled(applicationInfo.enabled);
                        installedApp.setPackageName(applicationInfo.packageName);
                        installedApp.setName(applicationInfo.loadLabel(this.packageManager).toString());
                        installedApp.setIcon(applicationInfo.loadIcon(this.packageManager));
                        this.installedApps.add(installedApp);
                    }
                } catch (Exception e) {
                }
            }
        }
    }
}
