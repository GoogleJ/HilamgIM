package io.hilamg.imservice.ui.act.base;

import android.annotation.SuppressLint;
import android.view.View;
import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle3.components.support.RxFragment;
import io.hilamg.imservice.Constant;
import io.hilamg.imservice.network.rx.RxException;

import io.rong.imkit.RongIM;

@SuppressLint("CheckResult")
public class BaseFragment extends RxFragment {
    public View rootView;

    public void handleApiError(Throwable throwable) {
        if (throwable.getCause() instanceof RxException.DuplicateLoginExcepiton ||
                throwable instanceof RxException.DuplicateLoginExcepiton) {
            // 重复登录，挤掉线
            RongIM.getInstance().logout();
            Constant.clear();
        }

        ToastUtils.showShort(RxException.getMessage(throwable));
    }

}
