package io.hilamg.imservice.ui.act;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.BarUtils;
import io.hilamg.imservice.R;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.widget.PinchImageView;
import io.hilamg.imservice.utils.GlideUtil;

public class ZoomActivity extends BaseActivity {
    private PinchImageView pic;
    private LinearLayout llTitle;

    //1:logo 2:bg
    private int type;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.setStatusBarVisibility(this, false);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setNavigationBarColor(0x00000000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }

        setContentView(R.layout.activity_zoom);

        pic = findViewById(R.id.pic);
        llTitle = findViewById(R.id.llTitle);

        initView();
    }

    private void initView() {
        type = getIntent().getIntExtra("type", 1);
        String imageUrl = getIntent().getStringExtra("image");
        GlideUtil.loadNormalImg(pic, imageUrl);

        if (getIntent().getBooleanExtra("fromSocialHomePage", false)) {
            llTitle.setVisibility(View.VISIBLE);
        } else {
            pic.setOnClickListener(v -> finishAfterTransition());
        }
    }

    public void back(View view) {
        finishAfterTransition();
    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent();
            intent.putExtra("url", url);
            setResult(1, intent);
        }
        super.finish();
    }

}
