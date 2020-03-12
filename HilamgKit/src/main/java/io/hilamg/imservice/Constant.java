package io.hilamg.imservice;

import android.text.TextUtils;

import com.blankj.utilcode.util.DeviceUtils;

import io.hilamg.imservice.bean.response.BusinessBean;
import io.hilamg.imservice.bean.response.LoginResponse;

import java.util.Locale;

public class Constant {
    //阿里OSS上传地址
//    public static final String OSS_URL = "https://zhongxingjike2.oss-cn-hongkong.aliyuncs.com/upload/";
//    public static final String BASE_URL = "https://mochart.ztoken.cn";

    public static final String OSS_URL = "https://zhongxingjike1.oss-cn-beijing.aliyuncs.com/upload/"; //debug
    public static final String BASE_URL = "http://47.111.164.191:8085/";  //26g  74w  191h

    public static final String APP_CODE = "fb0e95b069f74f29a2f972f9454d7d1a";
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_UNLOGIN = 601;

    public static String userId = "";
    public static String token = "";
    public static String nick = "";
    public static String phoneUuid =
            TextUtils.isEmpty(DeviceUtils.getMacAddress()) ? DeviceUtils.getAndroidID() : DeviceUtils.getMacAddress();
    public static String language = Locale.getDefault().toString().replace("_", "-");

    public static final String regUrl = "(([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[\\-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9\\.\\-]+|(?:www\\.|[\\-;:&=\\+\\$,\\w]+@)[A-Za-z0-9\\.\\-]+)((?:\\/[\\+~%\\/\\.\\w\\-_]*)?\\??(?:[\\-\\+=&;%@\\.\\w_]*)#?(?:[\\.\\!\\/\\\\\\w]*))?";

    public static void clear() {
        Constant.token = "";
        Constant.userId = "";
        Constant.phoneUuid = "";
    }

    public static void init(BusinessBean s) {
        userId = s.getCustomer().getId();
        token = s.getCustomer().getToken();
        nick = s.getCustomer().getNick();
    }
}
