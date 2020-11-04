package com.mobxpert.supercleaner.fragments;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.activities.MemoryBoosterActivity;
import com.mobxpert.supercleaner.databinding.FragmentRamBoosterBinding;
import com.mobxpert.supercleaner.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class RamBoosterFragment extends Fragment {

    FragmentRamBoosterBinding binding;
    private Context context;

    public RamBoosterFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        progresHandler.postDelayed(progressRunnable, 20000);
    }

    private Runnable progressRunnable = new Runnable() {
        public void run() {
            if (isAdded()) {
                setRamStats();
                new Handler().postDelayed(this, 20000);
            }
        }
    };
    private Handler progresHandler = new Handler();

    @Override
    public void onDetach() {
        progresHandler.removeCallbacks(progressRunnable);
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ram_booster, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, MemoryBoosterActivity.class));
            }
        });
        setRamStats();
    }

    private void setRamStats(){
        if (isDetached()){
            return;
        }
        try {
            double percent;
            android.app.ActivityManager activityManager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);
                long totalMemory = memoryInfo.totalMem;
                double usedMemory = ((double) (totalMemory - memoryInfo.availMem));
                percent = (usedMemory / ((double) totalMemory)) * 100.0d;
                binding.memoryPercent.setText(getString(R.string.percent, new Object[]{(int) percent, "%"}));
                binding.ramStats.setText(getString(R.string.stats, new Object[]{Utils.bytes2String((long)usedMemory), Utils.bytes2String(totalMemory)}));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
