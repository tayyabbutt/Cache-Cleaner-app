package com.mobxpert.supercleaner.adapters.adapters;

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
import com.mobxpert.supercleaner.models.Process;
import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.listeners.OnRunningAppSelectedListener;
import com.mobxpert.supercleaner.utils.Utils;

import java.util.ArrayList;

public class RunningAppsAdapter extends Adapter<ViewHolder> {
    private final String TAG = RunningAppsAdapter.class.getSimpleName();
    private OnRunningAppSelectedListener listener;
    private ArrayList<Process> processes;

    class RunningAppViewHolder extends ViewHolder {
        TextView appName;
        AppCompatCheckBox checked;
        ImageView icon;
        TextView size;

        RunningAppViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.app_name);
            checked = itemView.findViewById(R.id.checked);
            icon = itemView.findViewById(R.id.icon);
            size = itemView.findViewById(R.id.size);
        }
    }

    public RunningAppsAdapter(ArrayList<Process> processes, OnRunningAppSelectedListener listener) {
        this.listener = listener;
        this.processes = processes;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RunningAppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout_running_apps, parent, false));
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        Process process = (Process) this.processes.get(position);
        if (process != null) {
            final RunningAppViewHolder runningAppViewHolder = (RunningAppViewHolder) holder;
            try {
                runningAppViewHolder.icon.setImageDrawable(process.getIcon());
                runningAppViewHolder.appName.setText(process.getName());
                runningAppViewHolder.size.setText(Utils.bytes2String(process.getSize()));
                if (this.listener == null) {
                    runningAppViewHolder.checked.setVisibility(View.GONE);
                    return;
                }
                if (process.isSelected()) {
                    runningAppViewHolder.checked.setChecked(true);
                } else {
                    runningAppViewHolder.checked.setChecked(false);
                }
                runningAppViewHolder.itemView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        Process process = (Process) RunningAppsAdapter.this.processes.get(runningAppViewHolder.getAdapterPosition());
                        process.setSelected(!process.isSelected());
                        if (RunningAppsAdapter.this.listener != null) {
                            RunningAppsAdapter.this.listener.onAppSelected(process.isSelected(), process);
                        }
                        if (process.isSelected()) {
                            runningAppViewHolder.checked.setChecked(true);
                        } else {
                            runningAppViewHolder.checked.setChecked(false);
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(this.TAG, "onBindViewHolder --> running app: " + e.getLocalizedMessage());
            }
        }
    }

    public void onViewRecycled(ViewHolder holder) {
        if (holder != null) {
            RunningAppViewHolder runningAppViewHolder = (RunningAppViewHolder) holder;
            runningAppViewHolder.icon.setImageDrawable(null);
            runningAppViewHolder.appName.setText(null);
            runningAppViewHolder.size.setText(null);
        }
        super.onViewRecycled(holder);
    }

    public int getItemCount() {
        return this.processes.size();
    }
}
