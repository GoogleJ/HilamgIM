package io.hilamg.imservice.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import io.hilamg.imservice.ui.widget.dialog.LoadingDialog;

import java.lang.ref.WeakReference;

@SuppressLint("CheckResult")
public class CommonUtils {
    private static WeakReference<LoadingDialog> loadingDialog = new WeakReference<>(null);

    public static LoadingDialog initDialog(Context context) {
        return initDialog(context, null);
    }

    public static LoadingDialog initDialog(Context context, String loadText) {
        LoadingDialog d = loadingDialog.get();
        if (d != null) {
            d.dismissReally();
            loadingDialog.clear();
        }
        d = new LoadingDialog(context, loadText);
        loadingDialog = new WeakReference<>(d);
        return d;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
