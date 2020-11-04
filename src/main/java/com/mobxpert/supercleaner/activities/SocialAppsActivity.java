package com.mobxpert.supercleaner.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.databinding.ActivitySocialAppsBinding;
import com.mobxpert.supercleaner.managers.ActivityManager;

import static android.content.pm.PackageManager.GET_META_DATA;

public class SocialAppsActivity extends AppCompatActivity {
    private final String TAG = SocialAppsActivity.class.getSimpleName();
    private PackageManager packageManager;
    private String[] packages = new String[]{"com.whatsapp", "com.facebook.katana", "com.facebook.orca", "com.instagram.android", "com.snapchat.android", "com.twitter.android"};
    private ActivitySocialAppsBinding binding;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
        binding = DataBindingUtil.setContentView(this, R.layout.activity_social_apps);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Soial Media Cleaner");
        }
        this.packageManager = getPackageManager();

        AdView adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.RECTANGLE_HEIGHT_250);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();

        // AdMobAdsManager.getInstance().loadBanner(binding.adContainer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.container.getChildCount() > 0) {
            binding.container.removeAllViews();
        }
        if (binding.container.getChildCount() == 0) {
            for (String packageName : this.packages) {
                if (this.packageManager != null) {
                    try {
                        final View view = LayoutInflater.from(this).inflate(R.layout.layout_social_app, null, false);
                        ApplicationInfo applicationInfo = this.packageManager.getApplicationInfo(packageName, GET_META_DATA);
                        if (applicationInfo != null) {
                            ImageView icon = (ImageView) view.findViewById(R.id.icon);
                            TextView appName = (TextView) view.findViewById(R.id.app_name);
                            appName.setText(applicationInfo.loadLabel(this.packageManager));
                            icon.setImageDrawable(applicationInfo.loadIcon(this.packageManager));
                            if (binding.message.getVisibility() == View.VISIBLE) {
                                binding.message.setVisibility(View.GONE);
                            }
                            view.setTag(appName.getText().toString());
                            view.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(final View view) {
                                    interstitialAd.setAdListener(new AbstractAdListener() {
                                        @Override
                                        public void onError(Ad ad, AdError adError) {
                                            super.onError(ad, adError);
                                            Bundle bundle = new Bundle();
                                            bundle.putString(SocialAppsActivity.this.getString(R.string.social_app), view.getTag().toString());
                                            ActivityManager.getInstance().openNewActivity(SocialAppsActivity.this, SocialAppCleanerActivity.class, bundle, true);
                                        }

                                        @Override
                                        public void onAdLoaded(Ad ad) {
                                            super.onAdLoaded(ad);
                                            interstitialAd.show();
                                        }

                                        @Override
                                        public void onInterstitialDismissed(Ad ad) {
                                            super.onInterstitialDismissed(ad);
                                            Bundle bundle = new Bundle();
                                            bundle.putString(SocialAppsActivity.this.getString(R.string.social_app), view.getTag().toString());
                                            ActivityManager.getInstance().openNewActivity(SocialAppsActivity.this, SocialAppCleanerActivity.class, bundle, true);
                                        }
                                    });
                                    if (interstitialAd.isAdLoaded()) {
                                        interstitialAd = new InterstitialAd(SocialAppsActivity.this, getString(R.string.debugFbPlacementIdInterstitial));
                                    } else {
                                        try {
                                            interstitialAd.loadAd();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                            });
                            binding.container.addView(view);
                        }
                    } catch (NameNotFoundException e) {
                        Log.e(this.TAG, "app not found: " + e.getLocalizedMessage());
                    }
                }
            }
        }
        if (binding.container.getChildCount() == 0) {
            binding.message.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }
}
