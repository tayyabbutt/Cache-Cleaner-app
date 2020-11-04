package com.mobxpert.supercleaner.managers;

import android.content.SharedPreferences;

import com.mobxpert.supercleaner.MyApplication;

public class SharedPreferencesManager {
    private static SharedPreferencesManager appPreferences;
    private SharedPreferences sharedPreferences;

    private SharedPreferencesManager() {
    }

    public static SharedPreferencesManager getInstance() {
        if (appPreferences == null) {
            appPreferences = new SharedPreferencesManager();
            if (appPreferences.sharedPreferences == null) {
                appPreferences.sharedPreferences = MyApplication.getContext().getSharedPreferences(AppConstants.PREF_NAME, 0);
            }
        }
        return appPreferences;
    }

    public void removeKey(String key) {
        if (this.sharedPreferences != null) {
            this.sharedPreferences.edit().remove(key).apply();
        }
    }

    public void clear() {
        if (this.sharedPreferences != null) {
            this.sharedPreferences.edit().clear().apply();
        }
    }

    public boolean contains(String key) {
        if (this.sharedPreferences.contains(key)) {
            return true;
        }
        return false;
    }

    public void setString(String key, String value) {
        this.sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key) {
        return this.sharedPreferences.getString(key, AppConstants.KEY_NOT_FOUND);
    }

    public void setInt(String key, int value) {
        this.sharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key) {
        return this.sharedPreferences.getInt(key, -1);
    }

    public void setLong(String key, long value) {
        this.sharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key) {
        return this.sharedPreferences.getLong(key, -1);
    }

    public void setBoolean(String key, boolean value) {
        this.sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key) {
        return this.sharedPreferences.getBoolean(key, false);
    }
}
