package com.example.allot.view.shared;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
public final class AppDialogHelper {

    private AppDialogHelper() {
    }

    public static Dialog createDialog(Context context, int layoutResId, boolean cancelable) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(LayoutInflater.from(context).inflate(layoutResId, null));
        dialog.setCancelable(cancelable);
        return dialog;
    }

    public static void show(Dialog dialog, int widthPx, int heightPx) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(widthPx, heightPx);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public static void showWrapContent(Dialog dialog, int widthPx) {
        show(dialog, widthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
