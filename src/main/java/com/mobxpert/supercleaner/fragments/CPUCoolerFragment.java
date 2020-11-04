package com.mobxpert.supercleaner.fragments;


import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.ads.InterstitialAd;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.activities.CpuCoolerActivity;
import com.mobxpert.supercleaner.databinding.FragmentCpucoolerBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class CPUCoolerFragment extends Fragment {

    FragmentCpucoolerBinding binding;
    private Context context;
    InterstitialAd interstitialAd;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    public CPUCoolerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cpucooler, container, false);
        interstitialAd = new InterstitialAd(getContext(), getString(R.string.debugFbPlacementIdInterstitial));
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        binding.scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        context.startActivity(new Intent(context, CpuCoolerActivity.class));
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        context.startActivity(new Intent(context, CpuCoolerActivity.class));
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(getContext(), getString(R.string.debugFbPlacementIdInterstitial));
                } else {

                    interstitialAd.loadAd();
                }*/
                context.startActivity(new Intent(context, CpuCoolerActivity.class));
            }
        });
    }

}
