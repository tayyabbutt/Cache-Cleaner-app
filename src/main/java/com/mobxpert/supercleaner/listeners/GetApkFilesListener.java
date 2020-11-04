package com.mobxpert.supercleaner.listeners;

import com.mobxpert.supercleaner.models.ApkFile;

import java.util.ArrayList;

public interface GetApkFilesListener {
    ArrayList<ApkFile> getApkFiles();

    boolean isApkFilesCollected();

    void onApkInstalled(ApkFile apkFile);
}
