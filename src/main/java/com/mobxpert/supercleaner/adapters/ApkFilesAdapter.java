package com.mobxpert.supercleaner.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.listeners.OnApkFileSelectedListener;
import com.mobxpert.supercleaner.models.ApkFile;

import java.util.ArrayList;

public class ApkFilesAdapter extends Adapter<ViewHolder> {
    private final String TAG = ApkFilesAdapter.class.getSimpleName();
    private ArrayList<ApkFile> apkFiles;
    private Context context;
    private OnApkFileSelectedListener listener;

    class ApkFileViewHolder extends ViewHolder {
        TextView appName;
        AppCompatCheckBox checked;
        ImageView icon;
        TextView packageName;

        ApkFileViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            checked = itemView.findViewById(R.id.checked);
            icon = itemView.findViewById(R.id.icon);
            packageName = itemView.findViewById(R.id.package_name);
        }
    }

    public ApkFilesAdapter(Context context, ArrayList<ApkFile> apkFiles, OnApkFileSelectedListener listener) {
        this.context = context;
        this.apkFiles = apkFiles;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ApkFileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_apk_file, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ApkFile apkFile = (ApkFile) this.apkFiles.get(position);
        if (apkFile != null) {
            final ApkFileViewHolder apkFileViewHolder = (ApkFileViewHolder) holder;
            try {
                apkFileViewHolder.icon.setImageDrawable(apkFile.getIcon());
                apkFileViewHolder.appName.setText(apkFile.getName());
                apkFileViewHolder.packageName.setText(apkFile.getPackageName());
                if (((ApkFile) this.apkFiles.get(apkFileViewHolder.getAdapterPosition())).isSelected()) {
                    apkFileViewHolder.checked.setChecked(true);
                } else {
                    apkFileViewHolder.checked.setChecked(false);
                }
                apkFileViewHolder.itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (apkFileViewHolder.getAdapterPosition()>= apkFiles.size()){
                            notifyDataSetChanged();
                            return;
                        }
                        boolean selected = !((ApkFile) ApkFilesAdapter.this.apkFiles.get(apkFileViewHolder.getAdapterPosition())).isSelected();
                        ((ApkFile) ApkFilesAdapter.this.apkFiles.get(apkFileViewHolder.getAdapterPosition())).setSelected(selected);
                        if (ApkFilesAdapter.this.listener != null) {
                            ApkFilesAdapter.this.listener.onApkFileSelected(selected, (ApkFile) ApkFilesAdapter.this.apkFiles.get(apkFileViewHolder.getAdapterPosition()));
                        }
                        if (selected) {
                            apkFileViewHolder.checked.setChecked(true);
                        } else {
                            apkFileViewHolder.checked.setChecked(false);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(this.TAG, "onBindViewHolder --> apk file: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        if (holder != null) {
            ApkFileViewHolder apkFileViewHolder = (ApkFileViewHolder) holder;
            apkFileViewHolder.icon.setImageDrawable(null);
            apkFileViewHolder.appName.setText(null);
            apkFileViewHolder.packageName.setText(null);
        }
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return this.apkFiles.size();
    }
}
