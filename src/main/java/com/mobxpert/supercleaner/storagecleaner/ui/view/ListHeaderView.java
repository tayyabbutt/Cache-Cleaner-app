package com.mobxpert.supercleaner.storagecleaner.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.mobxpert.supercleaner.R;


public class ListHeaderView extends RelativeLayout {
    private Context mContext;
    public TextView mSize;
    public TextView mProgress;
    public LottieAnimationView animationView;
    public AdView adView;

    public ListHeaderView(Context context, ViewGroup listView) {
        super(context);
        this.mContext = context;
        View view = LayoutInflater.from(this.mContext).inflate(R.layout.list_header_view, listView, false);
        addView(view);
        mSize = (TextView) findViewById(R.id.total_size);
        mProgress = (TextView) findViewById(R.id.progress_msg);
        animationView = findViewById(R.id.animation_loading);
        adView = new AdView(this.mContext, context.getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();

    }
}