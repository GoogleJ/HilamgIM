package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.ToastUtils;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import io.hilamg.imservice.Constant;
import io.hilamg.imservice.HilamgKit;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.utils.CommonUtils;
import io.rong.imkit.RongIM;

@SuppressLint("CheckResult")
public class BrowsableActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getData() != null) {
            String action = getIntent().getData().getQueryParameter("action");
            if (!TextUtils.isEmpty(action)) {
                ToastUtils.showShort("success!");
            }

            HilamgKit.getInstance().init(getApplication());
            ServiceFactory.getInstance().getBaseService(Api.class)
                    .getBusiness("CE+rRBzL99", "15249047865", "E8EDF29302A189479CE9B9FEC7269385")
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                    .compose(RxSchedulers.normalTrans())
                    .subscribe(s -> {
                        Constant.init(s);
                        RongIM.getInstance().startGroupChat(this, s.getGroupId(), "");
                        finish();
                    }, t -> {
                        ToastUtils.showShort("error");
                        finish();
                    });

//

        }
    }
}
