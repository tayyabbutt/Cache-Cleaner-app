package com.mobxpert.supercleaner.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.listeners.OnAppSelectedListener;
import com.mobxpert.supercleaner.models.InstalledApp;

import java.util.ArrayList;

import static com.mobxpert.supercleaner.utils.Utils.isSystemPackage;

public class InstalledAppsAdapter extends Adapter<ViewHolder> {
    private final String TAG = InstalledAppsAdapter.class.getSimpleName();
    private Context context;
    private ArrayList<InstalledApp> installedApps;
    private OnAppSelectedListener listener;

    class PostInstalledAppViewHolder extends ViewHolder {
        TextView appName;
        AppCompatCheckBox checked;
        ImageView icon;
        TextView packageName;

        PostInstalledAppViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            checked = itemView.findViewById(R.id.checked);
            icon = itemView.findViewById(R.id.icon);
            packageName = itemView.findViewById(R.id.package_name);
        }
    }

    class PreInstalledAppViewHolder extends ViewHolder {
        TextView appName;
        SwitchCompat enabled;
        ImageView icon;
        TextView packageName;

        PreInstalledAppViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            enabled = itemView.findViewById(R.id.enabled);
            icon = itemView.findViewById(R.id.icon);
            packageName = itemView.findViewById(R.id.package_name);
        }
    }

    public InstalledAppsAdapter(Context context, ArrayList<InstalledApp> installedApps, OnAppSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.installedApps = installedApps;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new PreInstalledAppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_system_apps, parent, false));
            case 1:
                return new PostInstalledAppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_installed_apps, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        InstalledApp installedApp = (InstalledApp) this.installedApps.get(position);
        if (installedApp != null) {
            switch (holder.getItemViewType()) {
                case 0:
                    final PreInstalledAppViewHolder preInstalledAppViewHolder = (PreInstalledAppViewHolder) holder;
                    try {
                        preInstalledAppViewHolder.icon.setImageDrawable(installedApp.getIcon());
                        preInstalledAppViewHolder.appName.setText(installedApp.getName());
                        preInstalledAppViewHolder.packageName.setText(installedApp.getPackageName());
                        preInstalledAppViewHolder.itemView.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                if (InstalledAppsAdapter.this.listener != null) {
                                    InstalledAppsAdapter.this.listener.onAppEnabled(preInstalledAppViewHolder.enabled.isEnabled(), (InstalledApp) InstalledAppsAdapter.this.installedApps.get(preInstalledAppViewHolder.getAdapterPosition()));
                                }
                            }
                        });
                        preInstalledAppViewHolder.enabled.setOnCheckedChangeListener(null);
                        if (installedApp.isEnabled()) {
                            preInstalledAppViewHolder.enabled.setChecked(true);
                        } else {
                            preInstalledAppViewHolder.enabled.setChecked(false);
                        }
                        preInstalledAppViewHolder.enabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                ((InstalledApp) InstalledAppsAdapter.this.installedApps.get(preInstalledAppViewHolder.getAdapterPosition())).setEnabled(isChecked);
                                if (InstalledAppsAdapter.this.listener != null) {
                                    InstalledAppsAdapter.this.listener.onAppEnabled(isChecked, (InstalledApp) InstalledAppsAdapter.this.installedApps.get(preInstalledAppViewHolder.getAdapterPosition()));
                                }
                            }
                        });
                        return;
                    } catch (Exception e) {
                        Log.e(this.TAG, "onBindViewHolder --> system app: " + e.getLocalizedMessage());
                        return;
                    }
                case 1:
                    final PostInstalledAppViewHolder postInstalledAppViewHolder = (PostInstalledAppViewHolder) holder;
                    try {
                        postInstalledAppViewHolder.icon.setImageDrawable(installedApp.getIcon());
                        postInstalledAppViewHolder.appName.setText(installedApp.getName());
                        postInstalledAppViewHolder.packageName.setText(installedApp.getPackageName());
                        postInstalledAppViewHolder.itemView.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                InstalledAppsAdapter.this.openApplicationInfoActivity(((InstalledApp) InstalledAppsAdapter.this.installedApps.get(postInstalledAppViewHolder.getAdapterPosition())).getPackageName());
                            }
                        });
                        postInstalledAppViewHolder.checked.setOnCheckedChangeListener(null);
                        if (installedApp.isSelected()) {
                            postInstalledAppViewHolder.checked.setChecked(true);
                        } else {
                            postInstalledAppViewHolder.checked.setChecked(false);
                        }
                        postInstalledAppViewHolder.checked.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                ((InstalledApp) InstalledAppsAdapter.this.installedApps.get(postInstalledAppViewHolder.getAdapterPosition())).setSelected(isChecked);
                                if (InstalledAppsAdapter.this.listener != null) {
                                    InstalledAppsAdapter.this.listener.onAppSelected(isChecked, (InstalledApp) InstalledAppsAdapter.this.installedApps.get(postInstalledAppViewHolder.getAdapterPosition()));
                                }
                            }
                        });
                        return;
                    } catch (Exception e2) {
                        Log.e(this.TAG, "onBindViewHolder --> installed app: " + e2.getLocalizedMessage());
                        return;
                    }
                default:
                    return;
            }
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        if (holder != null) {
            switch (holder.getItemViewType()) {
                case 0:
                    PreInstalledAppViewHolder preInstalledAppViewHolder = (PreInstalledAppViewHolder) holder;
                    preInstalledAppViewHolder.icon.setImageDrawable(null);
                    preInstalledAppViewHolder.appName.setText(null);
                    preInstalledAppViewHolder.packageName.setText(null);
                    preInstalledAppViewHolder.enabled.setOnCheckedChangeListener(null);
                    preInstalledAppViewHolder.enabled.setChecked(false);
                    break;
                case 1:
                    PostInstalledAppViewHolder postInstalledAppViewHolder = (PostInstalledAppViewHolder) holder;
                    postInstalledAppViewHolder.icon.setImageDrawable(null);
                    postInstalledAppViewHolder.appName.setText(null);
                    postInstalledAppViewHolder.packageName.setText(null);
                    postInstalledAppViewHolder.checked.setOnCheckedChangeListener(null);
                    postInstalledAppViewHolder.checked.setChecked(false);
                    break;
            }
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return this.installedApps.size();
    }

    @Override
    public int getItemViewType(int position) {
        InstalledApp installedApp = (InstalledApp) this.installedApps.get(position);
        if (installedApp == null || isSystemPackage(installedApp)) {
            return 0;
        }
        return 1;
    }

    private void openApplicationInfoActivity(String packageName) {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + packageName));
            this.context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            this.context.startActivity(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
        }
    }
}
