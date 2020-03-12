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
import io.hilamg.imservice.utils.MD5Utils;
import io.rong.imkit.RongIM;

@SuppressLint("CheckResult")
public class BrowsableActivity extends RxAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getData() != null) {
            String appId = getIntent().getData().getQueryParameter("appId");
            String mobileOrEmail = getIntent().getData().getQueryParameter("mobileOrEmail");
            String appSecret = getIntent().getData().getQueryParameter("appSecret");

            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(mobileOrEmail)
                    || TextUtils.isEmpty(appSecret)) {
                ToastUtils.showShort("missing params");
                return;
            }

            String sign = "appId=" + appId + "&mobileOrEmail" + mobileOrEmail + "&appSecret" + appSecret;
            String md5Sign = MD5Utils.getMD5(sign);
            if (TextUtils.isEmpty(md5Sign)) {
                ToastUtils.showShort("missing params");
                return;
            }

            ServiceFactory.getInstance().getBaseService(Api.class)
                    .getBusiness(appId, mobileOrEmail, md5Sign.toLowerCase())
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                    .compose(RxSchedulers.normalTrans())
                    .subscribe(s -> {
                        HilamgKit.getInstance().init(getApplication());
                        Constant.init(s);
                        RongIM.getInstance().startGroupChat(this, s.getGroupId(), "");
                        finish();
                    }, t -> {
                        ToastUtils.showShort("error");
                        finish();
                    });
        }
    }
}
