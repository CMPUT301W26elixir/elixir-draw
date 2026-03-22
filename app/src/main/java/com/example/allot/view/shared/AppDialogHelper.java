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
     * Prevents this utility class from being instantiated.
     */
    private AppDialogHelper() {
    }

    /**
     * Creates a dialog from a layout resource.
     *
     * @param context the context used to build the dialog
     * @param layoutResId the layout resource inflated inside the dialog
     * @param cancelable true when the dialog may be dismissed by the user
     * @return the configured dialog instance
     */
    public static Dialog createDialog(Context context, int layoutResId, boolean cancelable) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(LayoutInflater.from(context).inflate(layoutResId, null));
        dialog.setCancelable(cancelable);
        return dialog;
    }

    /**
     * Shows a dialog with the requested size and transparent background.
     *
     * @param dialog the dialog to show
     * @param widthPx the target width in pixels
     * @param heightPx the target height in pixels
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
     * Shows a dialog with a fixed width and wrap-content height.
     *
     * @param dialog the dialog to show
     * @param widthPx the target width in pixels
     */
    public static void showWrapContent(Dialog dialog, int widthPx) {
        show(dialog, widthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
