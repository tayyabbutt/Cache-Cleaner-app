package com.mobxpert.supercleaner.fragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobxpert.supercleaner.HomeActivity;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.databinding.FragmentStorageCleanerBinding;
import com.mobxpert.supercleaner.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class StorageCleanerFragment extends Fragment {

    FragmentStorageCleanerBinding binding;
    private Context context;
    private Runnable progressRunnable = new Runnable() {
        public void run() {
            if (isAdded()) {
                setProgressStats();
                new Handler().postDelayed(this, 20000);
            }
        }
    };
    private Handler progresHandler = new Handler();

    public StorageCleanerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        progresHandler.postDelayed(progressRunnable, 20000);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_storage_cleaner, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermissions();
            }
        });
        setProgressStats();
    }

    @Override
    public void onDetach() {
        progresHandler.removeCallbacks(progressRunnable);
        super.onDetach();
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            ((HomeActivity) context).openJunkCleanActivity();
        } else if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") == 0 || ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            ((HomeActivity) context).openJunkCleanActivity();
        } else {
            ActivityCompat.requestPermissions((HomeActivity) context, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
        }
    }

    private void setProgressStats() {
        double percent;
        if (isDetached()) {
            return;
        }
        try {
            long totalInternal = Utils.getTotalInternalStorage();
            double usedInternal = (totalInternal - Utils.getAvailableInternalStorage());
            percent = (usedInternal / ((double) totalInternal)) * 100.0d;
            binding.storagePercent.setText(getString(R.string.percent, new Object[]{Integer.valueOf((int) percent), "%"}));
            binding.storageStats.setText(getString(R.string.stats, new Object[]{Utils.bytes2String((long) usedInternal), Utils.bytes2String(totalInternal)}));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
