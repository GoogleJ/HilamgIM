package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.request.EditCommunityRequest;
import io.hilamg.imservice.bean.response.CommunityInfoResponse;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.GlideUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;

public class EditSocialBasicActivity extends BaseActivity {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd  HH:mm");

    private int minimumHeightForVisibleOverlappingContent = 0;
    private int totalScrollRange = 0;
    private int statusbarHeight = 0;

    private CommunityInfoResponse data;

    private AppBarLayout app_bar;
    private CollapsingToolbarLayout collapsingLayout;
    private ImageView ivBg;
    private Toolbar toolbar;
    private ImageView ivToolBarStart;
    private ImageView ivHead;
    private TextView tvTitle;
    private TextView tvTime;
    private TextView tvContent;
    private TextView tvSocialName;
    private LinearLayout llTips;
    private LinearLayout llBottom;
    private TextView tvBottom;
    private LinearLayout llEt;
    private LinearLayout llContent;
    private TextView tvEtTitle1;
    private TextView tvEtTitle2;
    private EditText etSocialName;
    private EditText etSocialSlogan;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnableTouchHideKeyBoard(false);
        setTrasnferStatusBar(true);
        BarUtils.setStatusBarLightMode(this, true);
        setContentView(R.layout.activity_edit_social_basic);

        initView();

        setSocialBackgroundHeight();

        setToolBarMarginTop();

        setSupportActionBar(toolbar);

        onAppBarScroll();

        data = getIntent().getParcelableExtra("data");
        String type = getIntent().getStringExtra("type");
        if (type.equals("1")) {
            //slogan
            tvTitle.setText(R.string.socialslogan);
            tvTime.setText(sdf.format(new Date(Long.parseLong(data.getIntroductionEditDate()))));
            tvContent.setText(data.getIntroduction());
        } else {
            //notice
            tvTitle.setText(R.string.social_notice);
            tvTime.setText(sdf.format(new Date(Long.parseLong(data.getAnnouncementEditDate()))));
            tvContent.setText(data.getAnnouncement());
        }
        tvSocialName.setText(data.getName());
        GlideUtil.loadNormalImg(ivBg, data.getBgi());
        GlideUtil.loadNormalImg(ivHead, data.getLogo());

        String identity = data.getIdentity();
        if (!identity.equals("0")) {
            llBottom.setVisibility(View.VISIBLE);
            llTips.setVisibility(View.GONE);
        }

        tvBottom.setOnClickListener(v -> {
            if (tvBottom.getText().equals(getString(R.string.modify))) {
                //修改
                llContent.setVisibility(View.GONE);
                tvBottom.setText(R.string.done1);
                llEt.setVisibility(View.VISIBLE);
                etSocialName.setText(data.getName());
                etSocialName.setSelection(data.getName().length());

                if (type.equals("2")) {
                    //公告
                    tvEtTitle1.setVisibility(View.GONE);
                    etSocialName.setVisibility(View.GONE);
                    tvEtTitle2.setText(R.string.social_notice);
                    etSocialSlogan.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1000)});
                    etSocialSlogan.setHint(R.string.input_socialnotice);
                    etSocialSlogan.setText(data.getAnnouncement());

                    etSocialSlogan.setSelection(data.getAnnouncement().length());
                    etSocialSlogan.setFocusable(true);
                    etSocialSlogan.setFocusableInTouchMode(true);
                    etSocialSlogan.requestFocus();
                } else {
                    etSocialSlogan.setText(data.getIntroduction());
                }
                KeyboardUtils.showSoftInput(this);
            } else {
                //完成
                EditCommunityRequest request = new EditCommunityRequest();
                request.setGroupId(data.getGroupId());

                if (type.equals("1")) {
                    //简介，名称
                    String socialName = etSocialName.getText().toString().trim();
                    if (TextUtils.isEmpty(socialName)) {
                        ToastUtils.showShort(R.string.input_socialname1);
                        return;
                    }
                    String socialIntroduction = etSocialSlogan.getText().toString().trim();
                    if (TextUtils.isEmpty(socialIntroduction)) {
                        ToastUtils.showShort(R.string.input_socialnintroduction);
                        return;
                    }

                    request.setName(socialName);
                    request.setIntroduction(socialIntroduction);
                } else {
                    //公告
                    String notice = etSocialSlogan.getText().toString().trim();
                    if (TextUtils.isEmpty(notice)) {
                        ToastUtils.showShort(R.string.input_socialnotice);
                        return;
                    }
                    request.setAnnouncement(notice);
                }

                ServiceFactory.getInstance().getBaseService(Api.class)
                        .editCommunity(GsonUtils.toJson(request))
                        .compose(bindToLifecycle())
                        .compose(RxSchedulers.normalTrans())
                        .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                        .subscribe(r -> {
                            Intent intent = new Intent();
                            intent.putExtra("result", r.getUpdateTime());
                            if (type.equals("1")) {
                                intent.putExtra("name", etSocialName.getText().toString().trim());
                                intent.putExtra("slogan", etSocialSlogan.getText().toString().trim());

                                if (!etSocialName.getText().toString().trim().equals(data.getName())) {
                                    InformationNotificationMessage notificationMessage = InformationNotificationMessage.obtain("社群改名为" + etSocialName.getText().toString().trim());
                                    Message message = Message.obtain(data.getGroupId(), Conversation.ConversationType.GROUP, notificationMessage);
                                    RongIM.getInstance().sendMessage(message, "", "", (IRongCallback.ISendMessageCallback) null);
                                }

                                if (etSocialSlogan.getText().toString().trim().equals(data.getIntroduction())) {
                                    InformationNotificationMessage notificationMessage1 = InformationNotificationMessage.obtain("社群简介已更新");
                                    Message message1 = Message.obtain(data.getGroupId(), Conversation.ConversationType.GROUP, notificationMessage1);
                                    RongIM.getInstance().sendMessage(message1, "", "", (IRongCallback.ISendMessageCallback) null);
                                }

                            } else {
                                if (!etSocialSlogan.getText().toString().trim().equals(data.getAnnouncement())) {
                                    InformationNotificationMessage notificationMessage1 = InformationNotificationMessage.obtain("社群公告已更新");
                                    Message message1 = Message.obtain(data.getGroupId(), Conversation.ConversationType.GROUP, notificationMessage1);
                                    RongIM.getInstance().sendMessage(message1, "", "", (IRongCallback.ISendMessageCallback) null);
                                }
                                intent.putExtra("notice", etSocialSlogan.getText().toString().trim());
                            }
                            setResult(1, intent);
                            finish();
                        }, this::handleApiError);
            }
        });
    }

    private void onAppBarScroll() {
        app_bar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int absOffset = Math.abs(verticalOffset);
            if (minimumHeightForVisibleOverlappingContent <= 0) {
                minimumHeightForVisibleOverlappingContent = app_bar.getMinimumHeightForVisibleOverlappingContent();
            }
            if (totalScrollRange <= 0) {
                totalScrollRange = app_bar.getTotalScrollRange();
                collapsingLayout.setScrimVisibleHeightTrigger((int) (totalScrollRange * 0.5));
            }

            if (absOffset <= minimumHeightForVisibleOverlappingContent) {
                if (ivToolBarStart.getVisibility() == View.GONE) {
                    ivToolBarStart.setVisibility(View.VISIBLE);
                    ivToolBarStart.setImageResource(R.drawable.ic_social_back);
                }
            } else if (absOffset < totalScrollRange) {
                if (ivToolBarStart.getVisibility() == View.VISIBLE) {
                    ivToolBarStart.setVisibility(View.GONE);
                }
                if (tvTitle.getVisibility() == View.VISIBLE) {
                    tvTitle.setVisibility(View.INVISIBLE);
                }
            } else if (absOffset == totalScrollRange) {
                if (tvTitle.getVisibility() == View.INVISIBLE) {
                    tvTitle.setVisibility(View.VISIBLE);
                }
                ivToolBarStart.setVisibility(View.VISIBLE);
                ivToolBarStart.setImageResource(R.drawable.ico_back);
            }
        });
    }

    private void setToolBarMarginTop() {
        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
        statusbarHeight = BarUtils.getStatusBarHeight();
        layoutParams1.topMargin = statusbarHeight;
        toolbar.setLayoutParams(layoutParams1);
    }

    private void setSocialBackgroundHeight() {
        ViewGroup.LayoutParams layoutParams = app_bar.getLayoutParams();
        layoutParams.height = (int) (ScreenUtils.getScreenWidth() * 0.75);
        app_bar.setLayoutParams(layoutParams);
        ivBg.setImageResource(R.drawable.bg_default_social);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        app_bar = findViewById(R.id.app_bar);
        collapsingLayout = findViewById(R.id.collapsingLayout);
        ivBg = findViewById(R.id.ivBg);
        toolbar = findViewById(R.id.toolbar);
        ivToolBarStart = findViewById(R.id.ivToolBarStart);
        tvTitle = findViewById(R.id.tvTitle);
        tvTime = findViewById(R.id.tvTime);
        tvContent = findViewById(R.id.tvContent);
        tvSocialName = findViewById(R.id.tvSocialName);
        ivHead = findViewById(R.id.ivHead);
        llTips = findViewById(R.id.llTips);
        llBottom = findViewById(R.id.llBottom);
        tvBottom = findViewById(R.id.tvBottom);
        llEt = findViewById(R.id.llEt);
        llContent = findViewById(R.id.llContent);
        tvEtTitle1 = findViewById(R.id.tvEtTitle1);
        tvEtTitle2 = findViewById(R.id.tvEtTitle2);
        etSocialSlogan = findViewById(R.id.etSocialSlogan);
        etSocialName = findViewById(R.id.etSocialName);

        etSocialSlogan.setOnTouchListener((v, event) -> {
            if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }

    public void back(View view) {
        finish();
    }

}
