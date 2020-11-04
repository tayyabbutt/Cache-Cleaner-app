package com.mobxpert.supercleaner.managers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ActivityManager {
    private static ActivityManager activityManager;

    private ActivityManager() {
    }

    public static ActivityManager getInstance() {
        if (activityManager == null) {
            activityManager = new ActivityManager();
        }
        return activityManager;
    }

    public void openNewActivity(Activity activity, Class activityToOpen, boolean keepCurrentActivityOpen) {
        if (activity != null) {
            activity.startActivity(new Intent(activity, activityToOpen));
            if (!keepCurrentActivityOpen) {
                activity.finish();
            }
        }
    }

    public void openNewActivity(Activity activity, Class activityToOpen, Bundle bundle, boolean keepCurrentActivityOpen) {
        if (activity != null) {
            Intent intent = new Intent(activity, activityToOpen);
            intent.putExtras(bundle);
            activity.startActivity(intent);
            if (!keepCurrentActivityOpen) {
                activity.finish();
            }
        }
    }
}
