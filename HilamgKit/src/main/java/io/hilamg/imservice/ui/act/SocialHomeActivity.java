package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.hilamg.imservice.Constant;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.CommunityCultureResponse;
import io.hilamg.imservice.bean.response.CommunityInfoResponse;
import io.hilamg.imservice.bean.response.SocialCaltureListBean;
import io.hilamg.imservice.bean.response.base.BaseResponse;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.widget.NewPayBoard;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.GlideUtil;
import io.hilamg.imservice.utils.MD5Utils;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;

public class SocialHomeActivity extends BaseActivity {

    private static final int REQUEST_SLOGAN = 1;
    private static final int REQUEST_NOTICE = 2;
    private static final int REQUEST_SOCIALNAME = 3;
    private static final int REQUEST_LOGO = 4;
    private static final int REQUEST_BG = 5;
    private int[] detailTitles = {R.string.social_calture, R.string.social_act};
    private int minimumHeightForVisibleOverlappingContent = 0;
    private int totalScrollRange = 0;
    private int statusbarHeight = 0;

    private AppBarLayout app_bar;
    private CollapsingToolbarLayout collapsingLayout;
    private LinearLayout llTop;
    private LinearLayout llSecond;
    private ImageView ivBg;
    private Toolbar toolbar;
    private ImageView ivToolBarStart;
    private TextView tvTitle;
    private TextView tvSocialCode;
    private ImageView ivToolBarEnd;
    private ViewPager pagerOut;
    private TabLayout indicatorTop;

    private RecyclerView recyclerGroupMember;
    private BaseQuickAdapter<CommunityInfoResponse.MembersBean, BaseViewHolder> socialMemAdapter;
    private int maxMemVisiableItem;
    private int colorOwner;
    private TextView tvSlogan;
    private TextView tvSocialName;
    private TextView tvSocialId;
    private TextView tvNotice;
    private ImageView ivHead;
    private ImageView ivOpenConversation;
    private LinearLayout llSocialNotice;
    private LinearLayout llRemoveMem;
    private LinearLayout llInviteOrRemove;
    private View bgMask;

    private ViewStub viewStubPay;
    private ViewStub viewStubFree;
    private boolean contentEnable;
    private boolean hasInitTop;
    private boolean isInEditStatus;

    private CommunityInfoResponse response;

    private SocialCalturePage socialCalturePage;
    private DynamicsPage dynamicsPage;

    private String groupId;

    private boolean fromConversatin;
    private int appbarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTrasnferStatusBar(true);
        BarUtils.setStatusBarLightMode(this, true);
        setContentView(R.layout.activity_social_home);

        initView();

        fromConversatin = getIntent().getBooleanExtra("fromConversatin", false);
        if (fromConversatin) {
            ivOpenConversation.setVisibility(View.GONE);
        }

        groupId = getIntent().getStringExtra("id");

        initFragment();

        maxMemVisiableItem = (ScreenUtils.getScreenWidth() - CommonUtils.dip2px(this, 84)) / CommonUtils.dip2px(this, 25) + 1;

        ViewGroup.LayoutParams layoutParams = ivBg.getLayoutParams();
        layoutParams.height = (int) (ScreenUtils.getScreenWidth() * 0.75);
        ivBg.setLayoutParams(layoutParams);
        ivBg.setImageResource(R.drawable.bg_default_social);

        ViewGroup.LayoutParams layoutParams1 = bgMask.getLayoutParams();
        layoutParams1.height = (int) (ScreenUtils.getScreenWidth() * 0.75 * 0.5);
        bgMask.setLayoutParams(layoutParams1);

        onAppBarScroll();

        initData();

        setToolBarMarginTop();

        setSocialBackgroundHeight();

        setSupportActionBar(toolbar);


        initPager();
    }

    private void initFragment() {
        socialCalturePage = new SocialCalturePage(groupId);
        dynamicsPage = new DynamicsPage(groupId);

        socialCalturePage.setDoneAction(l -> {
            isInEditStatus = false;
            tvSocialId.setVisibility(View.VISIBLE);
            app_bar.setExpanded(true, true);
            tvTitle.setText(response.getName());
            ivToolBarStart.setVisibility(View.VISIBLE);
            tvSocialCode.setVisibility(View.VISIBLE);
            ivOpenConversation.animate().translationXBy(-ivOpenConversation.getWidth())
                    .setInterpolator(new OvershootInterpolator()).start();
        });
    }

    @SuppressLint("CheckResult")
    private void initData() {
        Api api = ServiceFactory.getInstance().getBaseService(Api.class);

        api.communityCulture(groupId)
                .compose(bindToLifecycle())
                .compose(RxSchedulers.normalTrans())
                .flatMap((Function<CommunityCultureResponse, ObservableSource<BaseResponse<CommunityInfoResponse>>>) r -> {
                    runOnUiThread(() -> {
                        if (!r.getType().equals("culture")) {
                            banAppBarScroll(true);

                            indicatorTop.setVisibility(View.GONE);
                            contentEnable = false;
                            llSocialNotice.setVisibility(View.GONE);
                            pagerOut.setVisibility(View.GONE);
                            llInviteOrRemove.setVisibility(View.GONE);
                            ivOpenConversation.setVisibility(View.GONE);
                            if (r.getType().equals("free")) {
                                View inflate = viewStubFree.inflate();
                                inflate.findViewById(R.id.tvFunc).setOnClickListener(v ->
                                        api.enterGroup(groupId, "", Constant.userId)
                                                .compose(bindToLifecycle())
                                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                                                .compose(RxSchedulers.normalTrans())
                                                .subscribe(s -> {
                                                    inflate.setVisibility(View.GONE);

                                                    banAppBarScroll(true);

                                                    indicatorTop.setVisibility(View.VISIBLE);
                                                    llSocialNotice.setVisibility(View.VISIBLE);
                                                    pagerOut.setVisibility(View.VISIBLE);
                                                    llInviteOrRemove.setVisibility(View.VISIBLE);
                                                    ivOpenConversation.setVisibility(View.VISIBLE);

                                                    contentEnable = true;

                                                    parseCaltureResult(s);
                                                }, this::handleApiError));
                            } else if (r.getType().equals("pay")) {
                                View inflate = viewStubPay.inflate();
                                TextView tvPayMoney = inflate.findViewById(R.id.tvPayMoney);
                                TextView tvSymbol = inflate.findViewById(R.id.tvSymbol);
                                ImageView ivIcon = inflate.findViewById(R.id.ivIcon);
                                tvPayMoney.setText(r.getPay().getPayFee());
                                tvSymbol.setText(r.getPay().getPaySymbol());
                                GlideUtil.loadNormalImg(ivIcon, r.getPay().getPayLogo());
                                findViewById(R.id.tvFunc).setOnClickListener(v -> new NewPayBoard(this).show(pwd ->
                                        api.payToGroup(groupId, "", MD5Utils.getMD5(pwd), r.getPay().getPayFee(), r.getPay().getPaySymbol())
                                                .compose(bindToLifecycle())
                                                .compose(RxSchedulers.normalTrans())
                                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                                                .subscribe(s -> {
                                                    inflate.setVisibility(View.GONE);

                                                    banAppBarScroll(true);

                                                    indicatorTop.setVisibility(View.VISIBLE);
                                                    llSocialNotice.setVisibility(View.VISIBLE);
                                                    pagerOut.setVisibility(View.VISIBLE);
                                                    llInviteOrRemove.setVisibility(View.VISIBLE);
                                                    ivOpenConversation.setVisibility(View.VISIBLE);

                                                    contentEnable = true;
                                                    parseCaltureResult(s);
                                                }, this::handleApiError)));
                            }
                        } else {
                            contentEnable = true;
                            parseCaltureResult(r);
                        }
                    });
                    return api.communityInfo(groupId);
                })
                .compose(RxSchedulers.normalTrans())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                .subscribe(r -> {
                    response = r;
                    tvSlogan.setText(r.getIntroduction());
                    tvSocialId.setText("社群号:" + r.getCode());
                    tvSocialName.setText(r.getName());
                    GlideUtil.loadNormalImg(ivBg, r.getBgi());
                    GlideUtil.loadNormalImg(ivHead, r.getLogo());
                    tvTitle.setText(r.getName());
                    tvSocialCode.setText("社群号:" + r.getCode());
                    tvNotice.setText(r.getAnnouncement());

                    if (!TextUtils.isEmpty(r.getIdentity()) && !r.getIdentity().equals("0")) {
                        llRemoveMem.setVisibility(View.VISIBLE);
                    }

                    ivHead.setOnClickListener(v -> {
                        Intent intent = new Intent(this, ZoomActivity.class);
                        intent.putExtra("image", response.getLogo());
                        intent.putExtra("fromSocialHomePage", true);
                        if (!response.getIdentity().equals("0")) {
                            intent.putExtra("id", response.getGroupId());
                            intent.putExtra("type", 1);
                        }
                        startActivityForResult(intent, REQUEST_LOGO);
                    });

                    ivBg.setOnClickListener(v -> {
                        Intent intent = new Intent(this, ZoomActivity.class);
                        intent.putExtra("image", response.getBgi());
                        intent.putExtra("fromSocialHomePage", true);
                        if (!response.getIdentity().equals("0")) {
                            intent.putExtra("id", response.getGroupId());
                            intent.putExtra("type", 2);
                        }
                        startActivityForResult(intent, REQUEST_BG);
                    });

                    if (r.getMembers().size() >= maxMemVisiableItem) {
                        maxMemVisiableItem -= 1;
                        List<CommunityInfoResponse.MembersBean> result = r.getMembers().subList(0, maxMemVisiableItem - 1);
                        int numLeft = r.getMembers().size() - result.size();
                        result.add(new CommunityInfoResponse.MembersBean());
                        ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) recyclerGroupMember.getLayoutParams();
                        layoutParams2.width = CommonUtils.dip2px(this, 25) * (result.size() + 1) + CommonUtils.dip2px(this, 20);
                        layoutParams2.setMarginEnd(CommonUtils.dip2px(this, 20));
                        recyclerGroupMember.setLayoutParams(layoutParams2);
                        initAdapterForSocialMem(result, numLeft);
                    } else {
                        ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) recyclerGroupMember.getLayoutParams();
                        layoutParams2.width = CommonUtils.dip2px(this, 25) * (r.getMembers().size()) + CommonUtils.dip2px(this, 20);
                        recyclerGroupMember.setLayoutParams(layoutParams2);
                        initAdapterForSocialMem(r.getMembers(), 0);
                    }
                }, t -> {
                    handleApiError(t);
                    finish();
                });
    }

    //解析社群文化为多类型data
    private void parseCaltureResult(CommunityCultureResponse r) {

        ArrayList<SocialCaltureListBean> caltures = new ArrayList<>();

        if (r.getOfficialWebsite() != null) {
            SocialCaltureListBean webBean = new SocialCaltureListBean(SocialCaltureListBean.TYPE_WEB);
            webBean.setOfficialWebsite(r.getOfficialWebsite());
            caltures.add(webBean);
        }
        if (r.getFiles() != null) {
            SocialCaltureListBean fileBean = new SocialCaltureListBean(SocialCaltureListBean.TYPE_FILE);
            fileBean.setFiles(r.getFiles());
            caltures.add(fileBean);
        }
        if (r.getVideo() != null) {
            SocialCaltureListBean videoBean = new SocialCaltureListBean(SocialCaltureListBean.TYPE_VIDEO);
            videoBean.setVideo(r.getVideo());
            caltures.add(videoBean);
        }
        if (r.getApplication() != null) {
            SocialCaltureListBean appBean = new SocialCaltureListBean(SocialCaltureListBean.TYPE_APP);
            appBean.setApplication(r.getApplication());
            caltures.add(appBean);
        }
        if (r.getActivities() != null) {
            SocialCaltureListBean actBean = new SocialCaltureListBean(SocialCaltureListBean.TYPE_ACTIVITY);
            actBean.setActivities(r.getActivities());
            caltures.add(actBean);
        }

        socialCalturePage.bindCaltureData(caltures);
    }

    private void banAppBarScroll(boolean isScroll) {
        if (app_bar == null) return;
        View mAppBarChildAt = app_bar.getChildAt(0);
        AppBarLayout.LayoutParams mAppBarParams = (AppBarLayout.LayoutParams) mAppBarChildAt.getLayoutParams();
        if (isScroll) {
            mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            mAppBarChildAt.setLayoutParams(mAppBarParams);
        } else {
            mAppBarParams.setScrollFlags(0);
        }
    }

    public void addSocialMem(View view) {
        ToastUtils.showShort(R.string.gotohilamg);
    }

    public void removeSocialMem(View view) {
        ToastUtils.showShort(R.string.gotohilamg);
    }

    private void initAdapterForSocialMem(List<CommunityInfoResponse.MembersBean> data, int numLeft) {
        Collections.reverse(data);
        recyclerGroupMember.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true));
        colorOwner = Color.parseColor("#ffc000");

        socialMemAdapter = new BaseQuickAdapter<CommunityInfoResponse.MembersBean, BaseViewHolder>(R.layout.item_social_membs) {
            @Override
            protected void convert(BaseViewHolder helper, CommunityInfoResponse.MembersBean item) {
                AsyncImageView ivMemberHead = helper.getView(R.id.ivMemberHead);
                ImageView ivOwner = helper.getView(R.id.ivOwner);
                TextView tvNumLeft = helper.getView(R.id.tvNumLeft);

                if (helper.getAdapterPosition() == data.size() - 1) {
                    ivOwner.setVisibility(View.VISIBLE);
                } else {
                    ivOwner.setVisibility(View.GONE);
                }

                if (helper.getAdapterPosition() == 0 && data.size() == maxMemVisiableItem) {
                    tvNumLeft.setVisibility(View.VISIBLE);
                    ivMemberHead.setVisibility(View.GONE);
                    tvNumLeft.setText("+" + (numLeft > 999 ? 999 : numLeft));
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) helper.itemView.getLayoutParams();
                    params.setMarginStart(-CommonUtils.dip2px(ivMemberHead.getContext(), 5));
                    helper.itemView.setLayoutParams(params);
                } else {
                    Uri uri = android.net.Uri.parse(item.getHeadPortrait());
                    ivMemberHead.setAvatar(uri);
                }
            }
        };
        recyclerGroupMember.setAdapter(socialMemAdapter);
        socialMemAdapter.setNewData(data);
    }

    private void onAppBarScroll() {
        app_bar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            if (isInEditStatus) {
                return;
            }

            int absOffset = Math.abs(verticalOffset);

            if (absOffset <= minimumHeightForVisibleOverlappingContent) {
                if (ivToolBarEnd.getVisibility() == View.GONE && response != null && !response.getIdentity().equals("0")) {
                    ivToolBarEnd.setVisibility(View.VISIBLE);
                    ivToolBarEnd.setImageResource(R.drawable.ic_socialhome_end_white);
                }
                if (ivToolBarStart.getVisibility() == View.GONE) {
                    ivToolBarStart.setImageResource(R.drawable.ic_social_back);
                    ivToolBarStart.setVisibility(View.VISIBLE);
                }
                if (tvTitle.getVisibility() == View.VISIBLE) {
                    tvTitle.setVisibility(View.INVISIBLE);
                    tvSocialCode.setVisibility(View.INVISIBLE);
                }
            } else if (absOffset < totalScrollRange) {
                if (ivToolBarEnd.getVisibility() == View.VISIBLE && response != null && !response.getIdentity().equals("0")) {
                    ivToolBarEnd.setVisibility(View.GONE);
                }
                if (ivToolBarStart.getVisibility() == View.VISIBLE) {
                    ivToolBarStart.setVisibility(View.GONE);
                }
                if (tvTitle.getVisibility() == View.VISIBLE) {
                    tvTitle.setVisibility(View.INVISIBLE);
                    tvSocialCode.setVisibility(View.INVISIBLE);
                }
            } else if (absOffset == totalScrollRange) {
                if (tvTitle.getVisibility() != View.VISIBLE) {
                    tvTitle.setVisibility(View.VISIBLE);
                    tvSocialCode.setVisibility(View.VISIBLE);
                }
                if (ivToolBarEnd.getVisibility() == View.GONE && response != null && !response.getIdentity().equals("0")) {
                    ivToolBarEnd.setVisibility(View.VISIBLE);
                    ivToolBarEnd.setImageResource(R.drawable.ic_socialhome_end_black);
                }
                if (ivToolBarStart.getVisibility() == View.GONE) {
                    ivToolBarStart.setVisibility(View.VISIBLE);
                    ivToolBarStart.setImageResource(R.drawable.ico_back);
                }
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
        app_bar.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!hasInitTop) {
                hasInitTop = true;
                llTop.setPadding(CommonUtils.dip2px(SocialHomeActivity.this, 16), (int) (ScreenUtils.getScreenWidth() * 0.75) - llSecond.getHeight(), CommonUtils.dip2px(SocialHomeActivity.this, 16), 0);
            }
            if (app_bar.getHeight() > appbarHeight) {
                appbarHeight = app_bar.getHeight();
                totalScrollRange = app_bar.getHeight() - BarUtils.getActionBarHeight() - statusbarHeight - CommonUtils.dip2px(SocialHomeActivity.this, 48);
                minimumHeightForVisibleOverlappingContent = app_bar.getHeight() - CommonUtils.dip2px(SocialHomeActivity.this, 48) - CommonUtils.dip2px(SocialHomeActivity.this, 160);
            }
        });
    }

    private void initView() {
        app_bar = findViewById(R.id.app_bar);
        collapsingLayout = findViewById(R.id.collapsingLayout);
        ivBg = findViewById(R.id.ivBg);
        toolbar = findViewById(R.id.toolbar);
        pagerOut = findViewById(R.id.pagerOut);
        indicatorTop = findViewById(R.id.indicatorTop);
        ivToolBarStart = findViewById(R.id.ivToolBarStart);
        tvTitle = findViewById(R.id.tvTitle);
        tvSocialCode = findViewById(R.id.tvSocialCode);
        tvNotice = findViewById(R.id.tvNotice);
        ivToolBarEnd = findViewById(R.id.ivToolBarEnd);
        recyclerGroupMember = findViewById(R.id.recyclerGroupMember);
        tvSlogan = findViewById(R.id.tvSlogan);
        tvSocialName = findViewById(R.id.tvSocialName);
        tvSocialId = findViewById(R.id.tvSocialId);
        ivHead = findViewById(R.id.ivHead);
        ivOpenConversation = findViewById(R.id.ivOpenConversation);
        llSocialNotice = findViewById(R.id.llSocialNotice);
        viewStubPay = findViewById(R.id.viewStubPay);
        viewStubFree = findViewById(R.id.viewStubFree);
        llTop = findViewById(R.id.llTop);
        llSecond = findViewById(R.id.llSecond);
        bgMask = findViewById(R.id.bgMask);
        llRemoveMem = findViewById(R.id.llRemoveMem);
        llInviteOrRemove = findViewById(R.id.llInviteOrRemove);
    }

    private void initPager() {
        pagerOut.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), R.string.appbar_scrolling_view_behavior) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) return socialCalturePage;
                else return dynamicsPage;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return getString(detailTitles[position]);
            }
        });

        indicatorTop.setupWithViewPager(pagerOut);
    }


    public void socialSlogan(View view) {
        if (!contentEnable || isInEditStatus) {
            ToastUtils.showShort(R.string.cantdone);
            return;
        }
        Intent intent = new Intent(this, EditSocialBasicActivity.class);
        intent.putExtra("type", "1");
        intent.putExtra("data", response);
        startActivityForResult(intent, REQUEST_SLOGAN);
    }

    public void socialNotice(View view) {
        if (!contentEnable || isInEditStatus) {
            ToastUtils.showShort(R.string.cantdone);
            return;
        }
        Intent intent = new Intent(this, EditSocialBasicActivity.class);
        intent.putExtra("type", "2");
        intent.putExtra("data", response);
        startActivityForResult(intent, REQUEST_NOTICE);
    }

    public void copySocialId(View view) {
        ToastUtils.showShort(R.string.duplicated_to_clipboard);
        ClipboardManager cm = (ClipboardManager) Utils.getApp().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("text", response.getCode()));
        }
    }

    public void openConversation(View view) {
        RongIM.getInstance().startGroupChat(this, response.getGroupId(), response.getName());
    }

    public void back(View view) {
        if (ivToolBarStart.getVisibility() != View.VISIBLE) {
            return;
        }
        finish();
    }

    @SuppressLint("CheckResult")
    public void menu(View view) {
        if (ivToolBarEnd.getVisibility() != View.VISIBLE) {
            return;
        }

        if (!contentEnable) {
            ToastUtils.showShort(R.string.cantdone);
            return;
        }

        ServiceFactory.getInstance().getBaseService(Api.class)
                .editListCommunityCulture(groupId)
                .compose(bindToLifecycle())
                .compose(RxSchedulers.normalTrans())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(SocialHomeActivity.this)))
                .subscribe(r -> {
                    isInEditStatus = true;
                    ivToolBarEnd.setVisibility(View.GONE);
                    ivToolBarStart.setVisibility(View.GONE);
                    tvSocialCode.setVisibility(View.GONE);
                    tvTitle.setText(R.string.edit_social_calture);
                    tvTitle.setVisibility(View.VISIBLE);
                    socialCalturePage.change2Edit(r);
                    app_bar.setExpanded(false, true);
                    ivOpenConversation.animate().translationXBy(ivOpenConversation.getWidth()).start();
                }, this::handleApiError);
    }

    public void fakeClick(View view) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == 1) {
            switch (requestCode) {
                case REQUEST_NOTICE:
                    response.setAnnouncementEditDate(data.getStringExtra("result"));
                    response.setAnnouncement(data.getStringExtra("notice"));
                    tvNotice.setText(response.getAnnouncement());
                    break;
                case REQUEST_SLOGAN:
                    response.setIntroductionEditDate(data.getStringExtra("result"));
                    response.setName(data.getStringExtra("name"));
                    response.setIntroduction(data.getStringExtra("slogan"));
                    tvSocialName.setText(response.getName());
                    tvSlogan.setText(response.getIntroduction());
                    break;
                case REQUEST_SOCIALNAME:
                    response.setName(data.getStringExtra("name"));
                    tvSocialName.setText(response.getName());
                case REQUEST_LOGO:
                    String logo = data.getStringExtra("url");
                    response.setLogo(logo);
                    GlideUtil.loadNormalImg(ivHead, logo);
                    break;
                case REQUEST_BG:
                    String bg = data.getStringExtra("url");
                    response.setBgi(bg);
                    GlideUtil.loadNormalImg(ivBg, bg);
                    break;
            }
        }
    }
}
