package com.mobxpert.supercleaner.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobxpert.supercleaner.R;
import com.mobxpert.supercleaner.adapters.ApkFilesAdapter;
import com.mobxpert.supercleaner.databinding.FragmentApkFilesBinding;
import com.mobxpert.supercleaner.listeners.GetApkFilesListener;
import com.mobxpert.supercleaner.listeners.OnApkFileSelectedListener;

import com.mobxpert.supercleaner.models.ApkFile;

import java.io.File;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_RECEIVER_FOREGROUND;

public class ApkFilesFragment extends Fragment implements OnApkFileSelectedListener {
    private final int INTERVAL = 500;
    private final String TAG = ApkFilesFragment.class.getSimpleName();
    private ApkFilesAdapter adapter;
    private ArrayList<ApkFile> apkFiles;
    private CountDownTimer countDownTimer;
    private Handler handler;
    private ApkFile lastSelectedApkFile;
    private GetApkFilesListener listener;
    private Runnable runnable;
    private ArrayList<ApkFile> selectedApkFiles;
    private int totalSelectedApkFiles = 0;
    FragmentApkFilesBinding binding;
    
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (GetApkFilesListener) context;
        } catch (Exception e) {
            Log.e(this.TAG, context.toString() + " must implement GetApkFilesListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apk_files, container, false);
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
                    updateDataSet(listener.getApkFiles());
                    if (listener.isApkFilesCollected()) {
                        removeHandlerCallBacks();
                    } else {
                        handler.postDelayed(this, 500);
                    }
                }
            }
        };
        this.runnable.run();
        this.countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                removeHandlerCallBacks();
                if (binding.progressBar == null) {
                    return;
                }
                if ((binding.progressBar.getVisibility() == View.VISIBLE && apkFiles == null) || apkFiles.size() == 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.message.setVisibility(View.VISIBLE);
                }
            }
        }.start();
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDeleteClicked();
            }
        });
        binding.install.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onInstallClicked();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeHandlerCallBacks();
    }

    public void updateDataSet(ArrayList<ApkFile> apkFiles) {
        if (apkFiles != null && apkFiles.size() > 0) {
            if (binding.progressBar.getVisibility() == View.VISIBLE) {
                binding.progressBar.setVisibility(View.GONE);
            }
            this.apkFiles = apkFiles;
            if (this.adapter == null) {
                this.adapter = new ApkFilesAdapter(getActivity(), this.apkFiles, this);
                binding.apkFilesList.setLayoutManager(new LinearLayoutManager(getContext()));
                binding.apkFilesList.setAdapter(this.adapter);
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

    public void onApkFileSelected(boolean selected, ApkFile apkFile) {
        if (selected) {
            this.totalSelectedApkFiles++;
            if (binding.delete.getVisibility() == View.GONE) {
                binding.delete.setVisibility(View.VISIBLE);
            }
            if (binding.install.getVisibility() == View.GONE) {
                binding.install.setVisibility(View.VISIBLE);
            }
            if (this.selectedApkFiles == null) {
                this.selectedApkFiles = new ArrayList();
            }
            this.selectedApkFiles.add(apkFile);
        } else {
            if (this.totalSelectedApkFiles > 0) {
                this.totalSelectedApkFiles--;
            }
            if (this.totalSelectedApkFiles == 0) {
                if (binding.delete.getVisibility() == View.VISIBLE) {
                    binding.delete.setVisibility(View.GONE);
                }
                if (binding.install.getVisibility() == View.VISIBLE) {
                    binding.install.setVisibility(View.GONE);
                }
            }
            if (this.selectedApkFiles != null) {
                this.selectedApkFiles.remove(apkFile);
            }
        }
        binding.delete.setText(getString(R.string.delete_apps, Integer.valueOf(this.totalSelectedApkFiles)));
        binding.install.setText(getString(R.string.install_apps, Integer.valueOf(this.totalSelectedApkFiles)));
    }

    void onDeleteClicked() {
        if (this.selectedApkFiles == null || this.selectedApkFiles.size() == 0) {
            Toast.makeText(getContext(), "Please select some apk files to DELETE!", Toast.LENGTH_LONG).show();
        } else if (getContext() != null) {
            final AlertDialog alertDialog = new Builder(getContext()).create();
            alertDialog.setTitle(getString(R.string.please_confirm));
            alertDialog.setMessage(getString(R.string.are_you_sure_to_delete));
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no),  new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    for (int i = selectedApkFiles.size() - 1; i >= 0; i--) {
                        ApkFile apkFile = (ApkFile) selectedApkFiles.get(i);
                        File file = apkFile.getFile();
                        if (file != null && file.exists() && file.delete()) {
                            onApkFileSelected(false, apkFile);
                            apkFiles.remove(apkFile);
                        }
                    }
                    if (adapter != null) {
                        if (apkFiles.size() == 0) {
                            binding.message.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    alertDialog.dismiss();
 //                   AdMobAdsManager.getInstance().showInterstitialAd();
                }
            });
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTransformationMethod(null);
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTransformationMethod(null);
        }
    }

    void onInstallClicked() {
        if (this.selectedApkFiles == null || this.selectedApkFiles.size() == 0) {
            Toast.makeText(getContext(), "Please select some apk files to INSTALL!", Toast.LENGTH_LONG).show();
        } else if (getContext() != null) {
            final AlertDialog alertDialog = new Builder(getContext()).create();
            alertDialog.setTitle(getString(R.string.please_confirm));
            alertDialog.setMessage(getString(R.string.are_you_sure_to_install));
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    for (int i = selectedApkFiles.size() - 1; i >= 0; i--) {
                        lastSelectedApkFile = (ApkFile) selectedApkFiles.get(i);
                        Intent intent;
                        if (VERSION.SDK_INT >= 24) {
                            intent = new Intent("android.intent.action.VIEW");
                            intent.setDataAndType(FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider.GenericFileProvider", lastSelectedApkFile.getFile()), "application/vnd.android.package-archive");
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(intent, 2);
                        } else {
                            intent = new Intent("android.intent.action.VIEW");
                            intent.setDataAndType(Uri.fromFile(new File(lastSelectedApkFile.getFile().getPath())), "application/vnd.android.package-archive");
                            intent.setFlags(FLAG_RECEIVER_FOREGROUND);
                            startActivityForResult(intent, 2);
                        }
                    }
                    alertDialog.dismiss();
  //                  AdMobAdsManager.getInstance().showInterstitialAd();
                }
            });
            alertDialog.show();
            alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTransformationMethod(null);
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTransformationMethod(null);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 2:
                if (getActivity() != null && this.lastSelectedApkFile != null) {
                    try {
                        if (getActivity().getPackageManager().getApplicationInfo(this.lastSelectedApkFile.getPackageName(), 0) != null) {
                            if (this.listener != null) {
                                this.listener.onApkInstalled(this.lastSelectedApkFile);
                            }
                            if (this.adapter != null) {
                                onApkFileSelected(false, this.lastSelectedApkFile);
                                this.lastSelectedApkFile.setSelected(false);
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
            default:
                return;
        }
    }
}
