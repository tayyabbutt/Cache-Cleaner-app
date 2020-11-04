package com.mobxpert.supercleaner.models;

import java.util.ArrayList;
import java.util.List;

public class RunningApp {
    private String packageName;
    private List<Integer> pid = new ArrayList();

    public RunningApp(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Integer> getPid() {
        return this.pid;
    }

    public void setPid(List<Integer> pid) {
        this.pid = pid;
    }

    public void addPID(int i) {
        if (!this.pid.contains(Integer.valueOf(i))) {
            this.pid.add(Integer.valueOf(i));
        }
    }
}
