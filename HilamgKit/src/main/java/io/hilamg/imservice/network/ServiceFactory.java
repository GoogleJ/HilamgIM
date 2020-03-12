package io.hilamg.imservice.network;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.Utils;
import io.hilamg.imservice.Constant;
import io.hilamg.imservice.utils.OkHttpClientUtil;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceFactory {
    private Retrofit retrofit;

    private static ServiceFactory instance;

    private ServiceFactory() {
        initRetrofit();
    }

    private void initRetrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("id", Constant.userId)
                            .header("token", Constant.token)
                            .header("Accept-Language", Constant.language)
                            .header("phoneUuid", Constant.phoneUuid)
                            .header("systemType", "1");
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .hostnameVerifier((hostname, session) -> true)
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .build();

        //buildApi
        retrofit = new Retrofit.Builder().baseUrl(Constant.BASE_URL)
                .client(OkHttpClientUtil.getSSLClient(client, Utils.getApp(), "cacert.cer"))
                .addConverterFactory(BasicConvertFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonUtils.getGson(false)))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    private Retrofit initNormal(String url) {
        //构造client对象
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "APPCODE " + Constant.APP_CODE);
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .readTimeout(8, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .build();

        return new Retrofit.Builder().baseUrl(url)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public <T> T getNormalService(String url, Class<T> from) {
        return initNormal(url).create(from);
    }

    public <T> T getBaseService(Class<T> from) {
        if (TextUtils.isEmpty(Constant.userId)) {
            initRetrofit();
        }
        return retrofit.create(from);
    }

}