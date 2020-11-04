package com.mobxpert.supercleaner;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.mobxpert.supercleaner.activities.SocialAppsActivity;
import com.mobxpert.supercleaner.adapters.MainPagerAdapter;
import com.mobxpert.supercleaner.databinding.ActivityHomeBinding;
import com.mobxpert.supercleaner.storagecleaner.ui.JunkCleanActivity;

import java.util.ArrayList;

import static android.content.DialogInterface.BUTTON_POSITIVE;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    ArrayList<View> tabViews = new ArrayList();
    private MainPagerAdapter adapter;
    public boolean isAppClosed;
    AdView adView;
    InterstitialAd interstitialAd;
    TextView textView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudienceNetworkAds.initialize(this);
        AudienceNetworkAds.isInAdsProcess(this);
        interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initTabLayout();
        //AdMobAdsManager.getInstance().loadBanner(binding.adView);
        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.BANNER_HEIGHT_90);
        LinearLayout adContainer = (LinearLayout) findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();
    }

    private void initTabLayout() {
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.setTabMode(TabLayout.MODE_FIXED);
        binding.tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.mainPager.setCurrentItem(tab.getPosition());
                tabViews.get(tab.getPosition()).findViewById(R.id.tab_text).setVisibility(View.VISIBLE);
                switch (tab.getPosition()) {
                    case 0:
                        binding.parentLayout.setBackgroundResource(R.color.tab_one_color);
                        break;
                    case 1:
                        binding.parentLayout.setBackgroundResource(R.color.tab_two_color);
                        break;
                    case 2:
                        binding.parentLayout.setBackgroundResource(R.color.tab_three_color);
                        break;
                    case 3:
                        binding.parentLayout.setBackgroundResource(R.color.tab_four_color);
                        break;
                    case 4:
                        binding.parentLayout.setBackgroundResource(R.color.tab_five_color);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabViews.get(tab.getPosition()).findViewById(R.id.tab_text).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        for (int i = 0; i < 5; i++) {
            View view1 = getLayoutInflater().inflate(R.layout.tabs_list_item, null);
            tabViews.add(view1);
            imageView = view1.findViewById(R.id.tab_main_img);
            textView = view1.findViewById(R.id.tab_text);
            if (i != 0) {
                textView.setVisibility(View.INVISIBLE);
            }
            if (i == 0) {
                /*interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        imageView.setImageResource(R.drawable.storage_tab_img);
                        textView.setText("Storage Cleaner");
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        imageView.setImageResource(R.drawable.storage_tab_img);
                        textView.setText("Storage Cleaner");
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }*/
                imageView.setImageResource(R.drawable.storage_tab_img);
                textView.setText("Storage Cleaner");

            } else if (i == 1) {
               /* interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        imageView.setImageResource(R.drawable.memory_tab_img);
                        textView.setText("Memory Boost");
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        imageView.setImageResource(R.drawable.memory_tab_img);
                        textView.setText("Memory Boost");
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }*/
                imageView.setImageResource(R.drawable.memory_tab_img);
                textView.setText("Memory Boost");
            } else if (i == 2) {
                /*interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        imageView.setImageResource(R.drawable.cooler_tab_img);
                        textView.setText("CPU Cooler");
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        imageView.setImageResource(R.drawable.cooler_tab_img);
                        textView.setText("CPU Cooler");
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }*/
                imageView.setImageResource(R.drawable.cooler_tab_img);
                textView.setText("CPU Cooler");

            } else if (i == 3) {
               /* interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        imageView.setImageResource(R.drawable.appmanager_tab_img);
                        textView.setText("App Manager");
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        imageView.setImageResource(R.drawable.appmanager_tab_img);
                        textView.setText("App Manager");
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }*/
                imageView.setImageResource(R.drawable.appmanager_tab_img);
                textView.setText("App Manager");
            } else if (i == 4) {
              /*  interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        imageView.setImageResource(R.drawable.social_tab_img);
                        textView.setText("Social Cleaner");
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        imageView.setImageResource(R.drawable.social_tab_img);
                        textView.setText("Social Cleaner");
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }*/
                imageView.setImageResource(R.drawable.social_tab_img);
                textView.setText("Social Cleaner");
            }
            binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(view1));
        }
        adapter = new MainPagerAdapter(getSupportFragmentManager(), binding.tabLayout.getTabCount());
        binding.mainPager.setAdapter(adapter);
        binding.parentLayout.setBackgroundResource(R.color.tab_one_color);
        binding.mainPager.setOffscreenPageLimit(5);
        binding.mainPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));
    }

    private void showExitDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exit, null);

        adView = new AdView(this, getString(R.string.debugFbPlacementId), AdSize.RECTANGLE_HEIGHT_250);
        LinearLayout adContainer = (LinearLayout) dialogView.findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(dialogView);
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getText(R.string.rate_uss), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        openRating();
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        openRating();
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(HomeActivity.this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }

            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getText(android.R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int which) {
                interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        dialog.dismiss();
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        dialog.dismiss();
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(HomeActivity.this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }

            }
        });
        alertDialog.setButton(BUTTON_POSITIVE, getText(android.R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                interstitialAd.setAdListener(new AbstractAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        super.onError(ad, adError);
                        isAppClosed = true;
                        finish();
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        super.onAdLoaded(ad);
                        interstitialAd.show();
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        super.onInterstitialDismissed(ad);
                        isAppClosed = true;
                        finish();
                    }
                });
                if (interstitialAd.isAdLoaded()) {
                    interstitialAd = new InterstitialAd(HomeActivity.this, getString(R.string.debugFbPlacementIdInterstitial));
                } else {
                    interstitialAd.loadAd();
                }

            }
        });
        alertDialog.show();
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setTransformationMethod(null);
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryCleaner));
        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTransformationMethod(null);
        alertDialog.getButton(BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
        alertDialog.getButton(BUTTON_POSITIVE).setTransformationMethod(null);
    }

    private void openRating() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0) {
            Toast.makeText(this, "Please grant the Storage permission", Toast.LENGTH_LONG).show();
        } else if (grantResults[0] == 0 && grantResults[1] == 0) {
            if (requestCode == 4) {
                openSocialAppsActivity();
            } else if (requestCode == 0) {
                openJunkCleanActivity();
            }
        }
    }

    public void openJunkCleanActivity() {
        startActivity(new Intent(this, JunkCleanActivity.class));
    }

    public void openSocialAppsActivity() {
        startActivity(new Intent(this, SocialAppsActivity.class));
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}
