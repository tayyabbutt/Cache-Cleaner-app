package com.mobxpert.supercleaner.models;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class Process implements Comparable {
    private Drawable icon;
    private String name;
    private String packageName;
    private boolean selected;
    private long size;

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return this.icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int compareTo(@NonNull Object obj) {
        long d = ((Process) obj).getSize();
        if (this.size > d) {
            return -1;
        }
        if (this.size < d) {
            return 1;
        }
        return 0;
    }
}
