package com.example.allot.view.shared;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Makes and shows simple dialogs with the app's shared sizes.
 */
public final class AppDialogHelper {

    /**
     * Creates a new AppDialogHelper instance.
     */
    private AppDialogHelper() {
    }

    /**
     * Returns the result of create dialog.
     *
     * @param context the context
     * @param layoutResId the layout res id
     * @param cancelable the cancelable
     * @return the result of this call
     */
    public static Dialog createDialog(Context context, int layoutResId, boolean cancelable) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(LayoutInflater.from(context).inflate(layoutResId, null));
        dialog.setCancelable(cancelable);
        return dialog;
    }

    /**
     * Performs show.
     *
     * @param dialog the dialog
     * @param widthPx the width px
     * @param heightPx the height px
     */
    public static void show(Dialog dialog, int widthPx, int heightPx) {
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(widthPx, heightPx);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Performs show wrap content.
     *
     * @param dialog the dialog
     * @param widthPx the width px
     */
    public static void showWrapContent(Dialog dialog, int widthPx) {
        show(dialog, widthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
