package com.mobxpert.supercleaner.fragments;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.mobxpert.supercleaner.HomeActivity;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.databinding.FragmentSocialCleanerBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class SocialCleanerFragment extends Fragment {

    FragmentSocialCleanerBinding binding;
    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    public SocialCleanerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_social_cleaner, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Glide.with(this).load(R.raw.social_cleaner).into(binding.roundBg);
        binding.scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermissions();
            }
        });
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT < 23) {
            ((HomeActivity)context).openSocialAppsActivity();
        } else if (ActivityCompat.checkSelfPermission(context,"android.permission.READ_EXTERNAL_STORAGE") == 0 || ActivityCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
            ((HomeActivity)context).openSocialAppsActivity();
        } else {
            ActivityCompat.requestPermissions((HomeActivity)context, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 4);
        }
    }
}
