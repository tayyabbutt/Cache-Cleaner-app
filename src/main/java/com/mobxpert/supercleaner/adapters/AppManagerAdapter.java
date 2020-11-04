package com.mobxpert.supercleaner.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.fragments.ApkFilesFragment;
import com.mobxpert.supercleaner.fragments.UnInstallFragment;

public class AppManagerAdapter extends FragmentPagerAdapter {
    private ApkFilesFragment apkFilesFragment = new ApkFilesFragment();
    private Context context;
    private UnInstallFragment unInstallFragment = new UnInstallFragment();

    public AppManagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    public ApkFilesFragment getApkFilesFragment() {
        return this.apkFilesFragment;
    }

    public UnInstallFragment getUnInstallFragment() {
        return this.unInstallFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return this.unInstallFragment;
            case 1:
                return this.apkFilesFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return this.context.getString(R.string.uninstall);
            case 1:
                return this.context.getString(R.string.apk_files_tab);
            default:
                return null;
        }
    }
}
