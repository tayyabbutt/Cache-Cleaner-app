package com.mobxpert.supercleaner.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.InstalledAppsAdapter;
import com.mobxpert.supercleaner.databinding.FragmentUninstallBinding;
import com.mobxpert.supercleaner.listeners.GetInstalledAppsListener;
import com.mobxpert.supercleaner.listeners.OnAppSelectedListener;
import com.mobxpert.supercleaner.models.InstalledApp;

import java.util.ArrayList;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

public class UnInstallFragment extends Fragment implements OnAppSelectedListener {
    private final int INTERVAL = 500;
    private final String TAG = UnInstallFragment.class.getSimpleName();
    private InstalledAppsAdapter adapter;
    private ArrayList<InstalledApp> appsToUnInstall;
    private Handler handler;
    private ArrayList<InstalledApp> installedApps;
    private InstalledApp lastSelectedApp;
    private GetInstalledAppsListener listener;
    private Runnable runnable;
    private int totalSelectedApps = 0;
    
    FragmentUninstallBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (GetInstalledAppsListener) context;
        } catch (Exception e) {
            Log.e(this.TAG, context.toString() + " must implement GetInstalledAppsListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_uninstall, container, false);
        binding.parentLayout.setOnClickListener(null);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    updateDataSet(listener.getInstalledApps());
                    if (listener.isExecutorFinished()) {
                        removeHandlerCallBacks();
                    } else {
                        handler.postDelayed(this, 500);
                    }
                }
            }
        };
        this.runnable.run();
        binding.uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onUnInstallClicked();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeHandlerCallBacks();
    }

    public void updateDataSet(ArrayList<InstalledApp> installedApps) {
        if (installedApps != null && installedApps.size() > 0) {
            if (binding.progressBar.getVisibility() == View.VISIBLE) {
                binding.progressBar.setVisibility(View.GONE);
            }
            this.installedApps = installedApps;
            if (this.adapter == null) {
                this.adapter = new InstalledAppsAdapter((Context) listener, this.installedApps, this);
                binding.installedApps.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.installedApps.setAdapter(this.adapter);
            }
            this.adapter.notifyDataSetChanged();
        } else if (binding.progressBar.getVisibility() == View.GONE) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void removeHandlerCallBacks() {
        if (this.handler != null && this.runnable != null) {
            this.handler.removeCallbacks(this.runnable);
        }
    }

    public void onAppEnabled(boolean enabled, InstalledApp installedApp) {
        this.lastSelectedApp = installedApp;
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.parse("package:" + installedApp.getPackageName()));
            startActivityForResult(intent, 1);
        } catch (ActivityNotFoundException e) {
            startActivityForResult(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS"), 1);
        }
    }

    public void onAppSelected(boolean selected, InstalledApp installedApp) {
        if (selected) {
            this.totalSelectedApps++;
            if (binding.uninstall.getVisibility() == View.GONE) {
                binding.uninstall.setVisibility(View.VISIBLE);
            }
            if (this.appsToUnInstall == null) {
                this.appsToUnInstall = new ArrayList();
            }
            this.appsToUnInstall.add(installedApp);
        } else {
            if (this.totalSelectedApps > 0) {
                this.totalSelectedApps--;
            }
            if (this.totalSelectedApps == 0 && binding.uninstall.getVisibility() == View.VISIBLE) {
                binding.uninstall.setVisibility(View.GONE);
            }
            if (this.appsToUnInstall != null) {
                this.appsToUnInstall.remove(installedApp);
            }
        }
        binding.uninstall.setText(getString(R.string.uninstall_apps, Integer.valueOf(this.totalSelectedApps)));
    }

    void onUnInstallClicked() {
        if (this.appsToUnInstall == null || this.appsToUnInstall.size() == 0) {
            Toast.makeText(getContext(), "Please select some app(s) to UNINSTALL!", Toast.LENGTH_LONG).show();
        } else if (getContext() != null) {
            final AlertDialog alertDialog = new Builder(getContext()).create();
            alertDialog.setTitle(getString(R.string.please_confirm));
            alertDialog.setMessage(getString(R.string.are_you_sure_to_uninstall));
            alertDialog.setButton(BUTTON_NEGATIVE, getString(android.R.string.no), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton(BUTTON_POSITIVE, getString(android.R.string.yes), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int number) {
                    for (int i = appsToUnInstall.size() - 1; i >= 0; i--) {
                        lastSelectedApp = (InstalledApp) appsToUnInstall.get(i);
                        Intent intent = new Intent("android.intent.action.DELETE");
                        intent.setData(Uri.parse(getString(R.string.package_colon) + ((InstalledApp) appsToUnInstall.get(i)).getPackageName()));
                        startActivityForResult(intent, 3);
                    }
                    alertDialog.dismiss();
     //               AdMobAdsManager.getInstance().showInterstitialAd();
                }
            });
            alertDialog.show();
            alertDialog.getButton(BUTTON_NEGATIVE).setTransformationMethod(null);
            alertDialog.getButton(BUTTON_POSITIVE).setTransformationMethod(null);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (getActivity() != null && this.lastSelectedApp != null) {
                    try {
                        ApplicationInfo applicationInfo = getActivity().getPackageManager().getApplicationInfo(this.lastSelectedApp.getPackageName(), 0);
                        if (applicationInfo != null) {
                            this.lastSelectedApp.setEnabled(applicationInfo.enabled);
                            if (this.adapter != null) {
                                this.adapter.notifyDataSetChanged();
                                return;
                            }
                            return;
                        }
                        return;
                    } catch (NameNotFoundException e) {
                        Log.e(this.TAG, "package not found: " + e.getLocalizedMessage());
                        return;
                    }
                }
                return;
            case 3:
                if (getActivity() != null && this.lastSelectedApp != null) {
                    try {
                        if (getActivity().getPackageManager().getApplicationInfo(this.lastSelectedApp.getPackageName(), 0) == null) {
                            removeLastApp();
                            return;
                        }
                        return;
                    } catch (NameNotFoundException e2) {
                        removeLastApp();
                        Log.e(this.TAG, "package not found: " + e2.getLocalizedMessage());
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    public void addInstalledAppInList(InstalledApp installedApp) {
        if (this.installedApps != null && installedApp != null) {
            this.installedApps.add(installedApp);
            if (this.adapter != null) {
                this.adapter.notifyDataSetChanged();
            }
        }
    }

    private void removeLastApp() {
        if (this.lastSelectedApp != null) {
            onAppSelected(false, this.lastSelectedApp);
            this.installedApps.remove(this.lastSelectedApp);
            if (this.adapter != null) {
                this.adapter.notifyDataSetChanged();
            }
            this.lastSelectedApp = null;
        }
    }
}
