package com.mobxpert.supercleaner;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.facebook.ads.AudienceNetworkAds;


public class MyApplication extends MultiDexApplication {
    public static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();



      /*  MobileAds.initialize(this, context.getString(R.string.app_id));
        AdMobAdsManager.getInstance();*/
    }

    public static Context getContext() {
        return context;
    }
}
