package com.mobxpert.supercleaner.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ProcessManager {
    private static final String TAG = ProcessManager.class.getSimpleName();
    private static final String regex;

    public static class Process implements Parcelable {
        public static final Creator<Process> CREATOR = new ProcessCreator();
        private final int var1;
        private final long var10;
        private final int var11;
        private final String var12;
        private final long var13;
        private final int var14;
        private final String var15;
        private final long var16;
        private final long var17;
        private final String var18;
        private final String var2;
        private final int var3;
        private final String var4;
        public final int var5;
        private final String var6;
        private final int var7;
        private final int var8;
        private final int var9;

        static class ProcessCreator implements Creator<Process> {
            ProcessCreator() {
            }

            public Process createFromParcel(Parcel parcel) {
                return getProcessFromParcel(parcel);
            }

            public Process[] newArray(int i) {
                return getProcessAtPosition(i);
            }

            private Process getProcessFromParcel(Parcel parcel) {
                return new Process(parcel);
            }

            private Process[] getProcessAtPosition(int i) {
                return new Process[i];
            }
        }

        private Process(String str) throws Exception {
            String[] split = str.split("\\s+");
            this.var15 = split[0];
            this.var14 = android.os.Process.getUidForName(this.var15);
            this.var5 = Integer.parseInt(split[1]);
            this.var7 = Integer.parseInt(split[2]);
            this.var17 = (long) (Integer.parseInt(split[3]) * 1024);
            this.var10 = (long) (Integer.parseInt(split[4]) * 1024);
            this.var1 = Integer.parseInt(split[5]);
            this.var8 = Integer.parseInt(split[6]);
            this.var3 = Integer.parseInt(split[7]);
            this.var9 = Integer.parseInt(split[8]);
            this.var11 = Integer.parseInt(split[9]);
            if (split.length == 16) {
                this.var6 = "";
                this.var18 = split[10];
                this.var4 = split[11];
                this.var12 = split[12];
                this.var2 = split[13];
                this.var16 = (long) (Integer.parseInt(split[14].split(":")[1].replace(",", "")) * 1000);
                this.var13 = (long) (Integer.parseInt(split[15].split(":")[1].replace(")", "")) * 1000);
                return;
            }
            this.var6 = split[10];
            this.var18 = split[11];
            this.var4 = split[12];
            this.var12 = split[13];
            this.var2 = split[14];
            this.var16 = (long) (Integer.parseInt(split[15].split(":")[1].replace(",", "")) * 1000);
            this.var13 = (long) (Integer.parseInt(split[16].split(":")[1].replace(")", "")) * 1000);
        }

        private Process(Parcel parcel) {
            this.var15 = parcel.readString();
            this.var14 = parcel.readInt();
            this.var5 = parcel.readInt();
            this.var7 = parcel.readInt();
            this.var17 = parcel.readLong();
            this.var10 = parcel.readLong();
            this.var1 = parcel.readInt();
            this.var8 = parcel.readInt();
            this.var3 = parcel.readInt();
            this.var9 = parcel.readInt();
            this.var11 = parcel.readInt();
            this.var6 = parcel.readString();
            this.var18 = parcel.readString();
            this.var4 = parcel.readString();
            this.var12 = parcel.readString();
            this.var2 = parcel.readString();
            this.var16 = parcel.readLong();
            this.var13 = parcel.readLong();
        }

        public String getInitialPart() {
            if (!this.var15.matches(ProcessManager.regex)) {
                return null;
            }
            if (this.var2.contains(":")) {
                return this.var2.split(":")[0];
            }
            return this.var2;
        }

        public PackageInfo getPackageInfo(Context context, int i) throws NameNotFoundException {
            String a = getInitialPart();
            if (a != null) {
                return context.getPackageManager().getPackageInfo(a, i);
            }
            throw new NameNotFoundException(this.var2 + " is not an application process");
        }

        public ApplicationInfo getApplicationInfo(Context context, int i) throws NameNotFoundException {
            String a = getInitialPart();
            if (a != null) {
                return context.getPackageManager().getApplicationInfo(a, i);
            }
            throw new NameNotFoundException(this.var2 + " is not an application process");
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(this.var15);
            parcel.writeInt(this.var14);
            parcel.writeInt(this.var5);
            parcel.writeInt(this.var7);
            parcel.writeLong(this.var17);
            parcel.writeLong(this.var10);
            parcel.writeInt(this.var1);
            parcel.writeInt(this.var8);
            parcel.writeInt(this.var3);
            parcel.writeInt(this.var9);
            parcel.writeInt(this.var11);
            parcel.writeString(this.var6);
            parcel.writeString(this.var18);
            parcel.writeString(this.var4);
            parcel.writeString(this.var12);
            parcel.writeString(this.var2);
            parcel.writeLong(this.var16);
            parcel.writeLong(this.var13);
        }
    }

    static {
        if (VERSION.SDK_INT >= 17) {
            regex = "u\\d+_a\\d+";
        } else {
            regex = "app_\\d+";
        }
    }

    public static List<Process> getAllProcesses() {
        List<Process> processes = new ArrayList();
        for (String str : Shell.SH.run("toolbox ps -p -P -x -c")) {
            try {
                processes.add(new Process(str));
            } catch (Exception e) {
                Log.d(TAG, "Failed parsing line " + str);
            }
        }
        return processes;
    }

    public static List<Process> getProcesses() {
        List<Process> processes = new ArrayList();
        List<String> a = Shell.SH.run("toolbox ps -p -P -x -c");
        int myPid = android.os.Process.myPid();
        for (String str : a) {
            try {
                Process process = new Process(str);
                if (!(!process.var15.matches(regex) || process.var7 == myPid || process.var2.equals("toolbox"))) {
                    processes.add(process);
                }
            } catch (Exception e) {
                Log.d(TAG, "Failed parsing line " + str);
            }
        }
        return processes;
    }
}
