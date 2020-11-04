package com.mobxpert.supercleaner.managers;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogManager {
    private static ProgressDialogManager progressDialogManager;
    private ProgressDialog progressDialog;

    private ProgressDialogManager() {
    }

    public static ProgressDialogManager getInstance() {
        if (progressDialogManager == null) {
            progressDialogManager = new ProgressDialogManager();
        }
        return progressDialogManager;
    }

    public void showProgressDialog(Context context, String message, boolean cancelable) {
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setProgressStyle(0);
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setMessage(message);
        this.progressDialog.setCancelable(cancelable);
        this.progressDialog.show();
    }

    public void hideProgressDialog() {
        if (this.progressDialog != null && this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
    }
}
