package com.mobxpert.supercleaner.listeners;

import com.mobxpert.supercleaner.models.InstalledApp;

import java.util.ArrayList;

public interface GetInstalledAppsListener {
    ArrayList<InstalledApp> getInstalledApps();

    boolean isExecutorFinished();
}
