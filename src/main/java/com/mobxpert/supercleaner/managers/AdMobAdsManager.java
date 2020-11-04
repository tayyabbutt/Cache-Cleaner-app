package com.mobxpert.supercleaner.managers;/*
import android.content.Context;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest.Builder;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoController.VideoLifecycleCallbacks;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd.Image;
import com.google.android.gms.ads.formats.NativeAppInstallAd;
import com.google.android.gms.ads.formats.NativeAppInstallAd.OnAppInstallAdLoadedListener;
import com.google.android.gms.ads.formats.NativeAppInstallAdView;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.utils.Utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.mobxpert.supercleaner.MyApplication.getContext;

public class AdMobAdsManager {
    private static AdMobAdsManager manager;
    private String TAG = AdMobAdsManager.class.getName();
    private InterstitialAd interstitialAd;

    private AdMobAdsManager() {
        Context context = getContext();
        this.interstitialAd = new InterstitialAd(context);
        this.interstitialAd.setAdUnitId(context.getString(R.string.interstitial_ad_unit));
        loadInterstitialAd();
    }

    public static AdMobAdsManager getInstance() {
        if (manager == null) {
            manager = new AdMobAdsManager();
        }
        return manager;
    }

    public void loadBanner(final AdView adView) {
        if (adView != null) {
            adView.loadAd(new Builder().build());
            adView.setAdListener(new AdListener() {
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.d("AdMobBanner", "onAdFailedToLoad");
                }

                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.d("AdMobBanner", "onAdLoaded");
                    if (adView.getVisibility() == View.GONE) {
                        adView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public void loadInterstitialAd() {
        if (Utils.isNetworkAvailable(getContext())) {
            this.interstitialAd.loadAd(new Builder().build());
            this.interstitialAd.setAdListener(new AdListener() {
                public void onAdClosed() {
                    super.onAdClosed();
                    Log.e(AdMobAdsManager.this.TAG, "onAdClosed");
                    AdMobAdsManager.this.loadInterstitialAd();
                }

                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(AdMobAdsManager.this.TAG, "onAdLoaded");
                }

                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.e(AdMobAdsManager.this.TAG, "onAdFailedToLoad");
                    AdMobAdsManager.this.loadInterstitialAd();
                }
            });
        }
    }

    public void showInterstitialAd() {
        if (this.interstitialAd.isLoaded()) {
            this.interstitialAd.show();
        } else {
            loadInterstitialAd();
        }
    }

    public void loadNativeAppInstall(final Context context, final FrameLayout nativeAppInstall, final int actionButtonColor) {
        if (Utils.isNetworkAvailable(getContext())) {
            new AdLoader.Builder(context, context.getString(R.string.native_ad_unit)).forAppInstallAd(new OnAppInstallAdLoadedListener() {
                public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
                    Log.e(AdMobAdsManager.this.TAG, "onNativeAppInstallAdLoaded");
                    NativeAppInstallAdView adView = (NativeAppInstallAdView) LayoutInflater.from(context).inflate(R.layout.ad_app_install, null);
                    AdMobAdsManager.this.populateAppInstallAdView(nativeAppInstallAd, adView, actionButtonColor);
                    nativeAppInstall.removeAllViews();
                    nativeAppInstall.addView(adView);
                }
            }).withAdListener(new AdListener() {
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.e(AdMobAdsManager.this.TAG, "onNativeAppInstallAdFailedToLoad");
                }

                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(AdMobAdsManager.this.TAG, "onNativeAppInstallAdLoaded");
                }

                public void onAdClosed() {
                    super.onAdClosed();
                    Log.e(AdMobAdsManager.this.TAG, "onNativeAppInstallAdClosed");
                }
            }).build().loadAd(new Builder().build());
        }
    }

    private void populateAppInstallAdView(NativeAppInstallAd nativeAppInstallAd, NativeAppInstallAdView adView, int actionButtonColor) {
        VideoController vc = nativeAppInstallAd.getVideoController();
        vc.setVideoLifecycleCallbacks(new VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });
        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
        adView.getCallToActionView().setBackgroundColor(ContextCompat.getColor(adView.getContext(), actionButtonColor));
        ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());
        MediaView mediaView = (MediaView) adView.findViewById(R.id.appinstall_media);
        ImageView mainImageView = (ImageView) adView.findViewById(R.id.appinstall_image);
        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);
        } else {
            adView.setImageView(mainImageView);
            mediaView.setVisibility(View.GONE);
            mainImageView.setImageDrawable(((Image) nativeAppInstallAd.getImages().get(0)).getDrawable());
        }
        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAppInstallAd);
    }

    public void loadNativeBannerAppInstall(final Context context, final FrameLayout nativeAppInstall) {
        if (Utils.isNetworkAvailable(context)) {
            new AdLoader.Builder(context, context.getString(R.string.native_ad_unit)).forAppInstallAd(new OnAppInstallAdLoadedListener() {
                public void onAppInstallAdLoaded(NativeAppInstallAd nativeAppInstallAd) {
                    Log.e(AdMobAdsManager.this.TAG, "onNativeBannerAppInstallAdLoaded");
                    NativeAppInstallAdView adView = (NativeAppInstallAdView) LayoutInflater.from(context).inflate(R.layout.banner_ad_app_install, null);
                    AdMobAdsManager.this.populateNativeBannerAppInstallAdView(nativeAppInstallAd, adView);
                    nativeAppInstall.removeAllViews();
                    nativeAppInstall.addView(adView);
                    nativeAppInstall.setVisibility(View.VISIBLE);
                }
            }).withAdListener(new AdListener() {
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.e(AdMobAdsManager.this.TAG, "onNativeBannerAppInstallAdFailedToLoad");
                }

                public void onAdLoaded() {
                    super.onAdLoaded();
                    Log.e(AdMobAdsManager.this.TAG, "onNativeBannerAppInstallAdLoaded");
                }

                public void onAdClosed() {
                    super.onAdClosed();
                    Log.e(AdMobAdsManager.this.TAG, "onNativeBannerAppInstallAdClosed");
                }
            }).build().loadAd(new Builder().build());
        }
    }
//    EEB982A566FD4FC5382B338057B0D3F2
    private void populateNativeBannerAppInstallAdView(NativeAppInstallAd nativeAppInstallAd, NativeAppInstallAdView adView) {
        nativeAppInstallAd.getVideoController().setVideoLifecycleCallbacks(new VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });
        adView.setHeadlineView(adView.findViewById(R.id.appinstall_headline));
        adView.setBodyView(adView.findViewById(R.id.appinstall_body));
        adView.setCallToActionView(adView.findViewById(R.id.appinstall_call_to_action));
        adView.setIconView(adView.findViewById(R.id.appinstall_app_icon));
        adView.setStarRatingView(adView.findViewById(R.id.appinstall_stars));
        ((TextView) adView.getHeadlineView()).setText(nativeAppInstallAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAppInstallAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAppInstallAd.getCallToAction());
        ((ImageView) adView.getIconView()).setImageDrawable(nativeAppInstallAd.getIcon().getDrawable());
        if (nativeAppInstallAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAppInstallAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAppInstallAd);
    }

    private String getDeviceId(){
        String aid = Settings.Secure.getString(getContext().getContentResolver(), "android_id");

        Object obj = null;
        try {
            ((MessageDigest) (obj = MessageDigest.getInstance("MD5"))).update(
                    aid.getBytes(), 0, aid.length());

            obj = String.format("%032X", new Object[] { new BigInteger(1,
                    ((MessageDigest) obj).digest()) });
        } catch (NoSuchAlgorithmException localNoSuchAlgorithmException) {
            obj = aid.substring(0, 32);
        }
        return obj.toString();
    }
}
*/