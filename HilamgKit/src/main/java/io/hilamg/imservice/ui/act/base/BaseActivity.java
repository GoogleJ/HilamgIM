package io.hilamg.imservice.ui.act.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import io.hilamg.imservice.Constant;
import io.hilamg.imservice.HilamgKit;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.DaoMaster;
import io.hilamg.imservice.db.OpenHelper;
import io.hilamg.imservice.network.rx.RxException;

@SuppressLint({"CheckResult", "Registered"})
public class BaseActivity extends RxAppCompatActivity {
    private boolean enableTouchHideKeyBoard = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (HilamgKit.daoSession == null) {
            OpenHelper open = new
                    OpenHelper(Utils.getApp(), Constant.userId, null);
            HilamgKit.daoSession = new DaoMaster(open.getWritableDatabase()).newSession();
        }
        setLightStatusBar(true);
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.hide();
        }
    }

    public void setEnableTouchHideKeyBoard(boolean enableTouchHideKeyBoard) {
        this.enableTouchHideKeyBoard = enableTouchHideKeyBoard;
    }

    public void setLightStatusBar(boolean isLight) {
        if (isLight) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorTheme));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void setTrasnferStatusBar(boolean isTransfer) {
        if (isTransfer) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorTheme));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public void handleApiError(Throwable throwable) {
        ToastUtils.showShort(RxException.getMessage(throwable));
    }

    @Override
    public void finish() {
        KeyboardUtils.hideSoftInput(this);
        super.finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (enableTouchHideKeyBoard && ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS
                );
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // Return whether touch the view.
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if ((v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }
}
