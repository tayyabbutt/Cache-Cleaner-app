package com.mobxpert.supercleaner.listeners;


import com.mobxpert.supercleaner.models.InstalledApp;

public interface OnAppSelectedListener {
    void onAppEnabled(boolean z, InstalledApp installedApp);

    void onAppSelected(boolean z, InstalledApp installedApp);
}
