package com.mobxpert.supercleaner.managers;

import android.content.Context;
import android.support.v7.app.AlertDialog.Builder;

public class AlertDialogManager {
    private static AlertDialogManager manager;

    private AlertDialogManager() {
    }

    public static AlertDialogManager getInstance() {
        if (manager == null) {
            manager = new AlertDialogManager();
        }
        return manager;
    }

    public void showAlertDialog(Context context, String title, String message, boolean hasPositiveAction, String positiveActionText) {
        Builder dialogBuilder = new Builder(context);
        dialogBuilder.setCancelable(true);
        dialogBuilder.setTitle((CharSequence) title);
        dialogBuilder.setMessage((CharSequence) message);
        if (hasPositiveAction) {
            dialogBuilder.setPositiveButton((CharSequence) positiveActionText, null);
        }
    }
}
