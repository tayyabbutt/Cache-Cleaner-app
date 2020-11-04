package com.mobxpert.supercleaner.adapters;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.mobxpert.supercleaner.fragments.AppManagerFragment;
import com.mobxpert.supercleaner.fragments.BlankFragment;
import com.mobxpert.supercleaner.fragments.CPUCoolerFragment;
import com.mobxpert.supercleaner.fragments.RamBoosterFragment;
import com.mobxpert.supercleaner.fragments.SocialCleanerFragment;
import com.mobxpert.supercleaner.fragments.StorageCleanerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    private List<Fragment> fragments = new ArrayList<>();

    public MainPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        for (int i = 0 ; i < mNumOfTabs ; i++){
            if (i == 0)
                fragments.add(new StorageCleanerFragment());
            else if (i == 1)
                fragments.add(new RamBoosterFragment());
            else if (i == 2)
                fragments.add(new CPUCoolerFragment());
            else if (i == 3)
                fragments.add(new AppManagerFragment());
            else
                fragments.add(new SocialCleanerFragment());
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    public Fragment getFragmentAtPosition(int position) {
        return new BlankFragment();

    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
