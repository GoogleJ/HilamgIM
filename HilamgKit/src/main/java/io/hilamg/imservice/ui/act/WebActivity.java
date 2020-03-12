package io.hilamg.imservice.ui.act;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.blankj.utilcode.util.Utils;
import io.hilamg.imservice.R;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.widget.ProgressView;

public class WebActivity extends BaseActivity {

    String currentUrl;
    private FrameLayout fl_webview;
    private ProgressView pb_webview;
    private WebSettings webSettings;
    private WebView mWebView;
    private TextView tv_title;
    private String title;
    private String type;
    private ValueAnimator pbAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web111);

        currentUrl = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        type = getIntent().getStringExtra("type");

        initView();

        initAnimtor();

        initWebView();
    }

    public void back(View view) {
        finish();
    }

    @SuppressLint("CheckResult")
    private void initView() {
        fl_webview = findViewById(R.id.fl_webview);
        pb_webview = findViewById(R.id.pb_webview);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setText(title);

        if (!TextUtils.isEmpty(type) && type.equals("mall")) {
            findViewById(R.id.rl_back).setVisibility(View.INVISIBLE);
            findViewById(R.id.rl_end).setVisibility(View.VISIBLE);
            ImageView iv_end = findViewById(R.id.iv_end);
            Drawable up = ContextCompat.getDrawable(this, R.drawable.ic_delete_dialog);
            Drawable drawableUp = DrawableCompat.wrap(up);
            DrawableCompat.setTint(drawableUp, ContextCompat.getColor(this, R.color.black));
            iv_end.setImageDrawable(drawableUp);
            findViewById(R.id.rl_end).setOnClickListener(v -> finish());
        } else {
            findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        }
    }

    private void initAnimtor() {
        pbAnim = ValueAnimator.ofFloat(0f, 70f);
        pbAnim.setDuration(3000);
        pbAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        pbAnim.addUpdateListener(animation -> pb_webview.setProgress((Float) animation.getAnimatedValue()));
        pbAnim.start();
    }

    private void initWebView() {
        mWebView = new WebView(Utils.getApp().getApplicationContext());

        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWebView.setLayoutParams(layoutParams);
        fl_webview.addView(mWebView, 0);

        webSettings = mWebView.getSettings();
        webSettings.setLoadsImagesAutomatically(true);

//        webSettings.setMixedContentMode(WebSettings.);

//设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        webSettings.setUseWideViewPort(true);
        //允许js代码
        webSettings.setJavaScriptEnabled(true);
        //允许SessionStorage/LocalStorage存储
        webSettings.setDomStorageEnabled(true);
        webSettings.setBlockNetworkImage(false);

//缩放操作
        webSettings.setSupportZoom(false); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(false); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(true); //隐藏原生的缩放控件

        webSettings.setAllowFileAccess(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pbAnim.cancel();
                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(pb_webview.getProgress(), 100f);
                    valueAnimator.setDuration((long) (1500 * (1 - pb_webview.getProgress() / 100f)));
                    valueAnimator.addUpdateListener(animation -> pb_webview.setProgress((Float) animation.getAnimatedValue()));
                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pb_webview.setVisibility(View.GONE);
                        }
                    });
                    valueAnimator.start();
                }
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (pb_webview.getVisibility() == View.GONE) {
                    pb_webview.setProgress(0);
                    pb_webview.setVisibility(View.VISIBLE);
                    pbAnim.start();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = Uri.parse(request.getUrl().toString());
                if (!TextUtils.isEmpty(uri.getScheme()) && !uri.getScheme().startsWith("http")) {
                    if (uri.getScheme().equals("hilamg")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                if (!TextUtils.isEmpty(uri.getScheme()) && !uri.getScheme().startsWith("http")) {
                    if (uri.getScheme().equals("hilamg")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                    return true;
                }
                return false;
            }
        });
        mWebView.loadUrl(currentUrl);
    }


    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            fl_webview.removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

}
