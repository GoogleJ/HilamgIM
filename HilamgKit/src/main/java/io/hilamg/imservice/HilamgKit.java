package io.hilamg.imservice;

import android.annotation.SuppressLint;
import android.app.Application;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;

import java.util.List;

import io.hilamg.imservice.bean.DaoSession;
import io.hilamg.imservice.ui.im.rongIM.BasePluginExtensionModule;
import io.hilamg.imservice.ui.im.rongIM.message.CusEmoteTabMessage;
import io.hilamg.imservice.ui.im.rongIM.message.GroupCardMessage;
import io.hilamg.imservice.ui.im.rongIM.provider.CusEmoteTabMessageProvider;
import io.hilamg.imservice.ui.im.rongIM.provider.MInfoNotificationMsgItemProvider;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.message.SightMessage;

import static io.rong.imlib.RongIMClient.ConnectionStatusListener.ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT;

public class HilamgKit {
    public static DaoSession daoSession;

    private static HilamgKit instance;

    private HilamgKit() {
    }

    public static HilamgKit getInstance() {
        if (instance == null) {
            instance = new HilamgKit();
        }
        return instance;
    }

    public static void setMyExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();

        if (moduleList != null) {
            IExtensionModule defaultModule = null;
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
            RongExtensionManager.getInstance().registerExtensionModule(new BasePluginExtensionModule());
        }
    }

    public void init(Application application) {
        //工具类初始化
        Utils.init(application);

        //融云初始化
        initRongSDK(application);

        //监听融云连接状态变化
        registerConnectionStatusListener();
    }

    private void registerConnectionStatusListener() {
        RongIMClient.setConnectionStatusListener(connectionStatus -> {
            if (connectionStatus == KICKED_OFFLINE_BY_OTHER_CLIENT) {
                ToastUtils.showShort(R.string.duplicated_login);
                RongIM.getInstance().logout();
                Constant.clear();
            }
        });
    }

    @SuppressLint("CheckResult")
    private void initRongSDK(Application application) {
        RongIM.init(application, "mgb7ka1nmdwcg");
        RongIM.registerMessageType(GroupCardMessage.class);
        RongIM.registerMessageType(SightMessage.class);
        RongIM.registerMessageType(CusEmoteTabMessage.class);
        RongIM.registerMessageTemplate(new MInfoNotificationMsgItemProvider());
        RongIM.registerMessageTemplate(new CusEmoteTabMessageProvider());
        RongIM.getInstance().enableNewComingMessageIcon(true);
        RongIM.getInstance().enableUnreadMessageIcon(true);
        setMyExtensionModule();
    }
}
