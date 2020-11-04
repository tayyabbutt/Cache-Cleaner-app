package com.mobxpert.supercleaner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import com.mobxpert.supercleaner.models.InstalledApp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static int lighten(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(Color.alpha(color), lightenColor(red, fraction), lightenColor(green, fraction), lightenColor(blue, fraction));
    }

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(Color.alpha(color), darkenColor(red, fraction), darkenColor(green, fraction), darkenColor(blue, fraction));
    }
    public static void hideSystemUI(View mDecorView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    private static int darkenColor(int color, double fraction) {
        return (int) Math.max(((double) color) - (((double) color) * fraction), 0.0d);
    }

    private static int lightenColor(int color, double fraction) {
        return (int) Math.min(((double) color) + (((double) color) * fraction), 255.0d);
    }

    public static boolean isWifiEnabled(Context context) {
        if (context != null) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Calendar.getInstance().getTime());
    }

    public static String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(Calendar.getInstance().getTime());
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static void deleteFolder(File folder) {
        for (File subFile : folder.listFiles()) {
            subFile.delete();
        }
        folder.delete();
    }

    public static long getTotalInternalStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) stat.getBlockCount()) * ((long) stat.getBlockSize());
    }

    public static long getAvailableInternalStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
    }

    private static boolean externalMemoryAvailable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static long getAvailableExternalMemorySize() {
        if (!externalMemoryAvailable()) {
            return 0;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
    }

    public static long getTotalExternalMemorySize() {
        if (!externalMemoryAvailable()) {
            return 0;
        }
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((long) stat.getBlockCount()) * ((long) stat.getBlockSize());
    }

    public static String totalFreeSpace(Context context) {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            String space = Formatter.formatFileSize(context, ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()));
            if (space.endsWith("KB")) {
                return space.replace("KB", " KB");
            }
            if (space.endsWith("MB")) {
                return space.replace("MB", " MB");
            }
            if (space.endsWith("GB")) {
                return space.replace("GB", " GB");
            }
            return space;
        } catch (Exception e) {
            Log.e(TAG, "totalFreeSpace: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static String totalUsedSpace(Context context) {
        try {
            String space = Formatter.formatFileSize(context, getTotalInternalStorage() - getAvailableInternalStorage());
            if (space.endsWith("KB")) {
                return space.replace("KB", " KB");
            }
            if (space.endsWith("MB")) {
                return space.replace("MB", " MB");
            }
            if (space.endsWith("GB")) {
                return space.replace("GB", " GB");
            }
            return space;
        } catch (Exception e) {
            Log.e(TAG, "totalUsedSpace: " + e.getLocalizedMessage());
            return null;
        }
    }

    public static float getAvailableSpaceInBytes() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (float) (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()));
    }

    public static float getAvailableSpaceInKB() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((float) (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()))) / 1024.0f;
    }

    public static float getAvailableSpaceInMB() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((float) (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()))) / 1048576.0f;
    }

    public static float getAvailableSpaceInGB() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return ((float) (((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize()))) / 1.07374182E9f;
    }

    public static boolean isSystemPackage(InstalledApp installedApp) {
        return (installedApp.getFlag() & 1) != 0;
    }

    public static String bytes2String(long length) {
        NumberFormat numberFormat = new DecimalFormat();
        numberFormat.setMaximumFractionDigits(1);
        if (((double) length) < 1024.0d) {
            try {
                return numberFormat.format(length) + " Byte(s)";
            } catch (Exception e) {
                return length + " Byte(s)";
            }
        } else if (((double) length) < 1048576.0d) {
            return numberFormat.format(((double) length) / 1024.0d) + "KB";
        } else {
            if (((double) length) < 1.073741824E9d) {
                return numberFormat.format(((double) length) / 1048576.0d) + "MB";
            }
            if (((double) length) < 1.099511627776E12d) {
                return numberFormat.format(((double) length) / 1.073741824E9d) + "GB";
            }
            return numberFormat.format(((double) length) / 1.099511627776E12d) + "TB";
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            return false;
        }
        return true;
    }
}
