package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import io.hilamg.imservice.Constant;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.request.EditCommunityApplicationRequest;
import io.hilamg.imservice.bean.request.EditCommunityFileRequest;
import io.hilamg.imservice.bean.request.EditCommunityVideoRequest;
import io.hilamg.imservice.bean.request.EditCommunityWebSiteRequest;
import io.hilamg.imservice.bean.response.EditListCommunityCultureResponse;
import io.hilamg.imservice.bean.response.SocialCaltureListBean;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseFragment;
import io.hilamg.imservice.ui.widget.dialog.LoadingDialog;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.GlideUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;
import okhttp3.ResponseBody;
import razerdp.basepopup.QuickPopupBuilder;
import razerdp.basepopup.QuickPopupConfig;
import razerdp.widget.QuickPopup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SocialCalturePage extends BaseFragment implements View.OnClickListener {
    private final int REQUEST_SETTINGWEB = 1;
    private final int REQUEST_SETTINGFILE = 2;
    private final int REQUEST_SETTINGVIDEO = 3;
    private final int REQUEST_SETTINGAPP = 4;
    private final int REQUEST_SETTINGACT = 5;

    private LinearLayout llBottom;
    private RecyclerView recycler;
    private BaseMultiItemQuickAdapter<SocialCaltureListBean, BaseViewHolder> adapter;
    private String groupId;

    private OnDoneAction doneAction;

    public interface OnDoneAction {
        void done(List<SocialCaltureListBean> result);
    }

    public void setDoneAction(OnDoneAction doneAction) {
        this.doneAction = doneAction;
    }

    public SocialCalturePage(String groupId) {
        this.groupId = groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    private int dp56;

    private java.util.Formatter timeFormatter;
    private StringBuilder mFormatBuilder;

    @SuppressLint("CheckResult")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFormatBuilder = new StringBuilder();
        timeFormatter = new java.util.Formatter(mFormatBuilder, Locale.ENGLISH);

        rootView = inflater.inflate(R.layout.calturepager, container, false);

        dp56 = CommonUtils.dip2px(getContext(), 56);

        llBottom = rootView.findViewById(R.id.llBottom);
        llBottom.setOnClickListener(this);

        recycler = rootView.findViewById(R.id.recycler);
        recycler.setItemAnimator(null);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BaseMultiItemQuickAdapter<SocialCaltureListBean, BaseViewHolder>(null) {

            {
                addItemType(SocialCaltureListBean.TYPE_WEB, R.layout.item_socialcalture_web);
                addItemType(SocialCaltureListBean.TYPE_FILE, R.layout.item_socialcalture_file);
                addItemType(SocialCaltureListBean.TYPE_VIDEO, R.layout.item_socialcalture_video);
                addItemType(SocialCaltureListBean.TYPE_APP, R.layout.item_socialcalture_app);
                addItemType(SocialCaltureListBean.TYPE_ACTIVITY, R.layout.item_socialcalture_activity);
            }

            @Override
            protected void convert(BaseViewHolder helper, SocialCaltureListBean item) {
                boolean isEdit = llBottom.getVisibility() == View.VISIBLE;
                Switch sw = helper.getView(R.id.sw);
                if (!isEdit) {
                    sw.setVisibility(View.INVISIBLE);
                } else {
                    sw.setVisibility(View.VISIBLE);
                }

                helper.addOnClickListener(R.id.sw);
                switch (helper.getItemViewType()) {
                    case SocialCaltureListBean.TYPE_WEB:
                        initViewForWebPage(helper, item);
                        break;
                    case SocialCaltureListBean.TYPE_FILE:
                        initViewForFilePage(helper, item);
                        break;
                    case SocialCaltureListBean.TYPE_VIDEO:
                        initViewForVideoPage(helper, item);
                        break;
                    case SocialCaltureListBean.TYPE_APP:
                        initViewForAppPage(helper, item);
                        break;
                    case SocialCaltureListBean.TYPE_ACTIVITY:
                        initViewForActivityPage(helper);
                        break;
                }
            }
        };

        adapter.setEmptyView(LayoutInflater.from(getContext()).inflate(R.layout.empty_recycler_social_calture, container, false));

        adapter.setOnItemClickListener((adapter, view, position) -> {
            SocialCaltureListBean bean = (SocialCaltureListBean) adapter.getData().get(position);
            switch (adapter.getItemViewType(position)) {
                case SocialCaltureListBean.TYPE_WEB:
                    if (llBottom.getVisibility() == View.VISIBLE) {
                        ToastUtils.showShort(R.string.gotohilamg);
                    } else {
                        TranslateAnimation showAnimation = new TranslateAnimation(0f, 0f, ScreenUtils.getScreenHeight(), 0f);
                        showAnimation.setDuration(250);
                        TranslateAnimation dismissAnimation = new TranslateAnimation(0f, 0f, 0f, ScreenUtils.getScreenHeight());
                        dismissAnimation.setDuration(500);
                        QuickPopup show = QuickPopupBuilder.with(getActivity())
                                .contentView(R.layout.popup_dangerweb)
                                .config(new QuickPopupConfig()
                                        .withShowAnimation(showAnimation)
                                        .withDismissAnimation(dismissAnimation)
                                        .withClick(R.id.llBack, null, true)
                                        .withClick(R.id.tv, v -> {
                                            Intent intent = new Intent(getContext(), WebActivity.class);
                                            intent.putExtra("url", bean.getOfficialWebsite().getOfficialWebsiteList().get(0).getWebsiteUrl());
                                            intent.putExtra("title", bean.getOfficialWebsite().getOfficialWebsiteList().get(0).getWebsiteTitle());
                                            startActivity(intent);
                                        }, true))
                                .show();
                        TextView tv = show.findViewById(R.id.tvTips);
                        tv.setText(Html.fromHtml(
                                " <p>您在第三方链接上的使用行为将适用该第三方链接 的《用户协议》和《隐私政策》，由" +
                                        "&nbsp;<font color='black'><b>" + bean.getOfficialWebsite().getOfficialWebsiteList().get(0).getWebsiteTitle() + "</b></font>" +
                                        "&nbsp;直接并单独向您承担责任。</p>"));
                    }
                    break;
                case SocialCaltureListBean.TYPE_FILE:
                    if (llBottom.getVisibility() == View.VISIBLE) {
                        ToastUtils.showShort(R.string.gotohilamg);
                    }
                    break;
                case SocialCaltureListBean.TYPE_VIDEO:
                    if (llBottom.getVisibility() == View.VISIBLE) {
                        ToastUtils.showShort(R.string.gotohilamg);
                    }
                    break;
                case SocialCaltureListBean.TYPE_APP:
                    if (llBottom.getVisibility() == View.VISIBLE) {
                        ToastUtils.showShort(R.string.gotohilamg);
                    }
                    break;
                case SocialCaltureListBean.TYPE_ACTIVITY:
                    ToastUtils.showShort(R.string.developing);
                    break;
            }
        });

        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            SocialCaltureListBean bean = (SocialCaltureListBean) adapter.getData().get(position);
            Switch sw = (Switch) view;
            switch (adapter.getItemViewType(position)) {
                case SocialCaltureListBean.TYPE_WEB:
                    if (bean.getOfficialWebsite().getOfficialWebsiteList().size() == 0) {
                        sw.setChecked(false);
                        ToastUtils.showShort(R.string.gotohilamg);
                    } else {
                        //call api 2 update status
                        EditCommunityWebSiteRequest request = new EditCommunityWebSiteRequest();
                        request.setGroupId(groupId);
                        request.setType("openOrClose");
                        request.setOfficialWebsiteOpen(sw.isChecked() ? "1" : "0");
                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .editCommunityWebSite(GsonUtils.toJson(request))
                                .compose(bindToLifecycle())
                                .compose(RxSchedulers.normalTrans())
                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(getActivity())))
                                .subscribe(s -> {
                                    bean.getOfficialWebsite().setOfficialWebsiteOpen(sw.isChecked() ? "1" : "0");
                                    adapter.notifyItemChanged(position);
                                }, t -> {
                                    sw.setChecked(!sw.isChecked());
                                    handleApiError(t);
                                });
                    }
                    break;
                case SocialCaltureListBean.TYPE_FILE:
                    if (bean.getFiles().getFilesList().size() == 0) {
                        sw.setChecked(false);
                        ToastUtils.showShort(R.string.gotohilamg);
                    } else {
                        //call api 2 update status
                        EditCommunityFileRequest request = new EditCommunityFileRequest();
                        request.setGroupId(groupId);
                        request.setType("openOrClose");
                        request.setFilesOpen(sw.isChecked() ? "1" : "0");
                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .editCommunityFile(GsonUtils.toJson(request))
                                .compose(bindToLifecycle())
                                .compose(RxSchedulers.normalTrans())
                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(getActivity())))
                                .subscribe(s -> {
                                    bean.getFiles().setFilesOpen(sw.isChecked() ? "1" : "0");
                                    adapter.notifyItemChanged(position);
                                }, t -> {
                                    sw.setChecked(!sw.isChecked());
                                    handleApiError(t);
                                });
                    }
                    break;
                case SocialCaltureListBean.TYPE_VIDEO:
                    if (bean.getVideo().getVideoList().size() == 0) {
                        sw.setChecked(false);
                        ToastUtils.showShort(R.string.gotohilamg);
                    } else {
                        //call api 2 update status
                        EditCommunityVideoRequest request = new EditCommunityVideoRequest();
                        request.setGroupId(groupId);
                        request.setType("openOrClose");
                        request.setVideoOpen(sw.isChecked() ? "1" : "0");
                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .editCommunityVideo(GsonUtils.toJson(request))
                                .compose(bindToLifecycle())
                                .compose(RxSchedulers.normalTrans())
                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(getActivity())))
                                .subscribe(s -> {
                                    bean.getVideo().setVideoOpen(sw.isChecked() ? "1" : "0");
                                    adapter.notifyItemChanged(position);
                                }, t -> {
                                    sw.setChecked(!sw.isChecked());
                                    handleApiError(t);
                                });
                    }
                    break;
                case SocialCaltureListBean.TYPE_APP:
                    if (bean.getApplication().getApplicationList().size() == 0) {
                        sw.setChecked(false);
                        ToastUtils.showShort(R.string.gotohilamg);
                    } else {
                        //call api 2 update status
                        EditCommunityApplicationRequest request = new EditCommunityApplicationRequest();
                        request.setGroupId(groupId);
                        request.setType("openOrClose");
                        request.setApplicationOpen(sw.isChecked() ? "1" : "0");
                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .editCommunityApplication(GsonUtils.toJson(request))
                                .compose(bindToLifecycle())
                                .compose(RxSchedulers.normalTrans())
                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(getActivity())))
                                .subscribe(s -> {
                                    bean.getApplication().setApplicationOpen(sw.isChecked() ? "1" : "0");
                                    adapter.notifyItemChanged(position);
                                }, t -> {
                                    sw.setChecked(!sw.isChecked());
                                    handleApiError(t);
                                });
                    }
                    break;
                case SocialCaltureListBean.TYPE_ACTIVITY:
                    ((Switch) view).setChecked(false);
                    ToastUtils.showShort(R.string.developing);
                    break;
            }
        });

        recycler.setAdapter(adapter);

        return rootView;
    }

    private void initViewForActivityPage(BaseViewHolder helper) {
        if (llBottom.getVisibility() == View.VISIBLE) {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) helper.itemView.getLayoutParams();
            layoutParams.bottomMargin = dp56;
            helper.itemView.setLayoutParams(layoutParams);
        } else {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) helper.itemView.getLayoutParams();
            layoutParams.bottomMargin = 0;
            helper.itemView.setLayoutParams(layoutParams);
        }
    }

    private void initViewForWebPage(BaseViewHolder helper, SocialCaltureListBean item) {
        EditListCommunityCultureResponse.OfficialWebsiteBean web = item.getOfficialWebsite();
        if (!TextUtils.isEmpty(web.getOfficialWebsiteOpen())
                && web.getOfficialWebsiteOpen().equals("1")) {
            helper.setChecked(R.id.sw, true);
        } else {
            helper.setChecked(R.id.sw, false);
        }
        helper.setText(R.id.tv, (web.getOfficialWebsiteList().size() == 0 || TextUtils.isEmpty(web.getOfficialWebsiteList().get(0).getWebsiteContent())) ?
                getContext().getString(R.string.empty_socialweb) : web.getOfficialWebsiteList().get(0).getWebsiteContent())
                .setText(R.id.tvTitle, item.getOfficialWebsite().getTitle());
    }

    private void initViewForFilePage(BaseViewHolder helper, SocialCaltureListBean item) {
        TextView tvNumLeft = helper.getView(R.id.tvNumLeft);
        if (llBottom.getVisibility() == View.VISIBLE) {
            tvNumLeft.setText("(还可上传" + (Integer.parseInt(item.getFiles().getFileCreate()) - item.getFiles().getFilesList().size()) + "份资料)");
        } else {
            tvNumLeft.setText("");
        }

        if (!TextUtils.isEmpty(item.getFiles().getFilesOpen())
                && item.getFiles().getFilesOpen().equals("1")) {
            helper.setChecked(R.id.sw, true);
        } else {
            helper.setChecked(R.id.sw, false);
        }

        RecyclerView fileRecycler = helper.getView(R.id.recycler);
        fileRecycler.setLayoutManager(new GridLayoutManager(getContext(), 4));

        int dp56 = CommonUtils.dip2px(getContext(), 56);
        int dp48 = CommonUtils.dip2px(getContext(), 48);
        int dp8 = CommonUtils.dip2px(getContext(), 8);
        int recyclerWidth = ScreenUtils.getScreenWidth() - dp48;
        boolean isNormalSize = true;
        if (((recyclerWidth - dp56 * 4) / 4) < dp8) {
            isNormalSize = false;
        }

        boolean finalIsNormalSize = isNormalSize;

        BaseQuickAdapter<EditListCommunityCultureResponse.FilesBean.FilesListBean, BaseViewHolder> appAdapter;
        appAdapter = new BaseQuickAdapter<EditListCommunityCultureResponse.FilesBean.FilesListBean, BaseViewHolder>(R.layout.item_social_app, item.getFiles().getFilesList()) {
            @Override
            protected void convert(BaseViewHolder helper, EditListCommunityCultureResponse.FilesBean.FilesListBean item) {
                ImageView ivAppIcon = helper.getView(R.id.ivAppIcon);
                if (!finalIsNormalSize) {
                    LinearLayout llContent = helper.getView(R.id.llContent);
                    ViewGroup.LayoutParams layoutParams = llContent.getLayoutParams();
                    if (layoutParams.width != dp48) {
                        layoutParams.width = dp48;
                        llContent.setLayoutParams(layoutParams);
                        ViewGroup.LayoutParams layoutParams1 = ivAppIcon.getLayoutParams();
                        layoutParams1.width = dp48;
                        layoutParams1.height = dp48;
                        ivAppIcon.setLayoutParams(layoutParams1);
                    }
                }

                if (item.getFileFormat().contains("doc") || item.getFileFormat().contains("docx")) {
                    Glide.with(getContext()).load(R.drawable.ic_social_file_word).into(ivAppIcon);
                } else if (item.getFileFormat().contains("xls") || item.getFileFormat().contains("xlsx")) {
                    Glide.with(getContext()).load(R.drawable.ic_social_file_excel).into(ivAppIcon);
                } else if (item.getFileFormat().contains("ppt") || item.getFileFormat().contains("pptx")) {
                    Glide.with(getContext()).load(R.drawable.ic_social_file_ppt).into(ivAppIcon);
                } else {
                    Glide.with(getContext()).load(R.drawable.ic_social_file_pdf).into(ivAppIcon);
                }

                helper.setText(R.id.tvTitle, item.getFileName());
            }
        };

        appAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (llBottom.getVisibility() == View.VISIBLE) {
                ToastUtils.showShort(R.string.gotohilamg);
                return;
            }
            downloadAndShowFile(appAdapter, position);
        });

        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.empty_recycler_social_app, (ViewGroup) rootView, false);
        appAdapter.setEmptyView(emptyView);
        emptyView.setOnClickListener(v -> ToastUtils.showShort(R.string.gotohilamg));

        fileRecycler.setAdapter(appAdapter);

        appAdapter.setNewData(item.getFiles().getFilesList());
    }

    private void downloadAndShowFile(BaseQuickAdapter<EditListCommunityCultureResponse.FilesBean.FilesListBean, BaseViewHolder> appAdapter, int position) {
        EditListCommunityCultureResponse.FilesBean.FilesListBean filesListBean = appAdapter.getData().get(position);
        String url = filesListBean.getFileAddress().replace(Constant.OSS_URL, "");
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (filesListBean.getFileFormat().contains(".")) {
            futureStudioIconFile = new File(dcim + "/Hilamg/SocialFiles/", url + filesListBean.getFileFormat());
        } else {
            futureStudioIconFile = new File(dcim + "/Hilamg/SocialFiles/", url);
        }
        if (futureStudioIconFile.exists()) {
            if (futureStudioIconFile != null && futureStudioIconFile.exists()) {
                if (!filesListBean.getFileFormat().contains("pdf")) {
                    start2ShowDocument(filesListBean.getFileFormat(), futureStudioIconFile.getPath());
                } else {
                    start2ShowPDF(futureStudioIconFile.getPath());
                }
            } else {
                ToastUtils.showShort(R.string.cantopenfile);
            }
        } else {
            LoadingDialog loadingDialog = new LoadingDialog(getActivity(), "下载中，请稍后");
            loadingDialog.show();
            ServiceFactory.getInstance().getNormalService(Constant.OSS_URL, Api.class)
                    .downloadFile(url)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            loadingDialog.dismissReally();
                            if (response != null && response.isSuccessful()) {
                                boolean toDisk;
                                if (filesListBean.getFileFormat().contains(".")) {
                                    toDisk = writeResponseBodyToDisk(response.body());
                                } else {
                                    toDisk = writeResponseBodyToDisk(response.body());
                                }
                                if (toDisk && futureStudioIconFile != null && futureStudioIconFile.exists()) {
                                    if (!filesListBean.getFileFormat().contains("pdf")) {
                                        start2ShowDocument(filesListBean.getFileFormat(), futureStudioIconFile.getPath());
                                    } else {
                                        start2ShowPDF(futureStudioIconFile.getPath());
                                    }
                                } else {
                                    ToastUtils.showShort(R.string.cantopenfile);
                                }
                            } else {
                                ToastUtils.showShort(R.string.cantopenfile);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            loadingDialog.dismissReally();
                            ToastUtils.showShort(R.string.cantopenfile);
                        }
                    });
        }
    }

    private void initViewForAppPage(BaseViewHolder helper, SocialCaltureListBean item) {
        EditListCommunityCultureResponse.ApplicationBean app = item.getApplication();
        if (!TextUtils.isEmpty(app.getApplicationOpen())
                && app.getApplicationOpen().equals("1")) {
            helper.setChecked(R.id.sw, true);
        } else {
            helper.setChecked(R.id.sw, false);
        }

        RecyclerView appRecycler = helper.getView(R.id.recycler);
        appRecycler.setLayoutManager(new GridLayoutManager(getContext(), 4));

        TextView tvTips = helper.getView(R.id.tvTips);
        if (llBottom.getVisibility() == View.VISIBLE) {
            tvTips.setVisibility(View.VISIBLE);
        } else {
            tvTips.setVisibility(View.INVISIBLE);
        }

        int dp56 = CommonUtils.dip2px(getContext(), 56);
        int dp48 = CommonUtils.dip2px(getContext(), 48);
        int dp8 = CommonUtils.dip2px(getContext(), 8);
        int recyclerWidth = ScreenUtils.getScreenWidth() - dp48;
        boolean isNormalSize = true;
        if (((recyclerWidth - dp56 * 4) / 4) < dp8) {
            isNormalSize = false;
        }

        boolean finalIsNormalSize = isNormalSize;

        BaseQuickAdapter<EditListCommunityCultureResponse.ApplicationBean.ApplicationListBean, BaseViewHolder> appAdapter;
        appAdapter = new BaseQuickAdapter<EditListCommunityCultureResponse.ApplicationBean.ApplicationListBean, BaseViewHolder>(R.layout.item_social_app, item.getApplication().getApplicationList()) {
            @Override
            protected void convert(BaseViewHolder helper, EditListCommunityCultureResponse.ApplicationBean.ApplicationListBean item) {
                ImageView ivAppIcon = helper.getView(R.id.ivAppIcon);
                if (!finalIsNormalSize) {
                    LinearLayout llContent = helper.getView(R.id.llContent);
                    ViewGroup.LayoutParams layoutParams = llContent.getLayoutParams();
                    if (layoutParams.width != dp48) {
                        layoutParams.width = dp48;
                        llContent.setLayoutParams(layoutParams);
                        ViewGroup.LayoutParams layoutParams1 = ivAppIcon.getLayoutParams();
                        layoutParams1.width = dp48;
                        layoutParams1.height = dp48;
                        ivAppIcon.setLayoutParams(layoutParams1);
                    }
                }

                GlideUtil.loadCornerImg(ivAppIcon, item.getApplicationLogo(), 10);
                helper.setText(R.id.tvTitle, item.getApplicationName());
            }
        };

        appAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (llBottom.getVisibility() == View.VISIBLE) {
                ToastUtils.showShort(R.string.gotohilamg);
                return;
            }
            if (appAdapter.getData().get(position).getIsOffical().equals("0")) {
                TranslateAnimation showAnimation = new TranslateAnimation(0f, 0f, ScreenUtils.getScreenHeight(), 0f);
                showAnimation.setDuration(250);
                TranslateAnimation dismissAnimation = new TranslateAnimation(0f, 0f, 0f, ScreenUtils.getScreenHeight());
                dismissAnimation.setDuration(500);
                QuickPopup show = QuickPopupBuilder.with(getActivity())
                        .contentView(R.layout.popup_dangerweb)
                        .config(new QuickPopupConfig()
                                .withShowAnimation(showAnimation)
                                .withDismissAnimation(dismissAnimation)
                                .withClick(R.id.llBack, null, true)
                                .withClick(R.id.tv, v -> {
                                    EditListCommunityCultureResponse.ApplicationBean.ApplicationListBean applicationListBean = appAdapter.getData().get(position);
                                    Intent intent = new Intent(getContext(), WebActivity.class);
                                    intent.putExtra("url", applicationListBean.getApplicationAddress());
                                    intent.putExtra("title", applicationListBean.getApplicationName());
                                    startActivity(intent);
                                }, true))
                        .show();
                TextView tips = show.findViewById(R.id.tvTips);
                tips.setText(Html.fromHtml(
                        " <p>您在第三方链接上的使用行为将适用该第三方链接 的《用户协议》和《隐私政策》，由" +
                                "&nbsp;<font color='black'><b>" + appAdapter.getData().get(position).getApplicationName() + "</b></font>" +
                                "&nbsp;直接并单独向您承担责任。</p>"));
            } else {
                EditListCommunityCultureResponse.ApplicationBean.ApplicationListBean applicationListBean = appAdapter.getData().get(position);
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra("url", applicationListBean.getApplicationAddress());
                intent.putExtra("title", applicationListBean.getApplicationName());
                startActivity(intent);
            }
        });

        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.empty_recycler_social_app, (ViewGroup) rootView, false);
        appAdapter.setEmptyView(emptyView);
        emptyView.setOnClickListener(v -> ToastUtils.showShort(R.string.gotohilamg));

        appRecycler.setAdapter(appAdapter);

        appAdapter.setNewData(item.getApplication().getApplicationList());
    }

    private void initViewForVideoPage(BaseViewHolder helper, SocialCaltureListBean item) {
        EditListCommunityCultureResponse.VideoBean video = item.getVideo();
        if (!TextUtils.isEmpty(video.getVideoOpen())
                && video.getVideoOpen().equals("1")) {
            helper.setChecked(R.id.sw, true);
        } else {
            helper.setChecked(R.id.sw, false);
        }

        ViewPager pagerVideo = helper.getView(R.id.pagerVideo);
        LinearLayout llVideoEmpty = helper.getView(R.id.llVideoEmpty);
        TextView tvNumLeft = helper.getView(R.id.tvNumLeft);

        ViewGroup.LayoutParams layoutParams = pagerVideo.getLayoutParams();
        int width = (int) ((ScreenUtils.getScreenWidth() - CommonUtils.dip2px(getContext(), 64)) / 1.15f);
        int height = (int) (width * 0.55f);
        layoutParams.width = width;
        layoutParams.height = height;
        pagerVideo.setLayoutParams(layoutParams);

        if (llBottom.getVisibility() == View.VISIBLE) {
            tvNumLeft.setText("(还可上传" + (Integer.parseInt(item.getVideo().getVideoCreate()) - item.getVideo().getVideoList().size()) + "条视频)");
        } else {
            tvNumLeft.setText("");
        }

        if (item.getVideo().getVideoList().size() != 0) {

            pagerVideo.setOffscreenPageLimit(3);
            pagerVideo.setPageTransformer(false, new ViewPager.PageTransformer() {
                private static final float MAX_ALPHA = 0.5f;
                private static final float MAX_SCALE = 0.9f;

                @Override
                public void transformPage(View page, float position) {
                    if (position < -1 || position > 1) {
                        //不可见区域
                        page.setAlpha(MAX_ALPHA);
                        page.setScaleX(MAX_SCALE);
                        page.setScaleY(MAX_SCALE);
                    } else {
                        //可见区域，透明度效果
                        if (position <= 0) {
                            //pos区域[-1,0)
                            page.setAlpha(MAX_ALPHA + MAX_ALPHA * (1 + position));
                        } else {
                            //pos区域[0,1]
                            page.setAlpha(MAX_ALPHA + MAX_ALPHA * (1 - position));
                        }
                        //可见区域，缩放效果
                        float scale = Math.max(MAX_SCALE, 1 - Math.abs(position));
                        page.setScaleX(scale);
                        page.setScaleY(scale);
                    }
                }
            });

            pagerVideo.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                public void onPageSelected(int position) {
                }

                public void onPageScrollStateChanged(int state) {
                }
            });

            pagerVideo.setVisibility(View.VISIBLE);
            llVideoEmpty.setVisibility(View.GONE);
            pagerVideo.setAdapter(new PagerAdapter() {
                @Override
                public int getCount() {
                    return item.getVideo().getVideoList().size();
                }

                @Override
                public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                    return view == object;
                }

                @NonNull
                @Override
                public Object instantiateItem(@NonNull ViewGroup container, int position) {
                    View inflate = LayoutInflater.from(getContext()).inflate(R.layout.item_social_video, container, false);

                    ImageView imageView = inflate.findViewById(R.id.ivVideoPic);
                    GlideUtil.loadNormalImg(imageView, item.getVideo().getVideoList().get(position).getVideoPic());
                    imageView.setOnClickListener(v -> {
                        if (llBottom.getVisibility() == View.VISIBLE) {
                            ToastUtils.showShort(R.string.gotohilamg);
                        } else {
                            String url = item.getVideo().getVideoList().get(position).getVideoAddress();
                            start2ShowVideo(url);
                        }
                    });

                    TextView tvVideoTitle = inflate.findViewById(R.id.tvVideoTitle);
                    tvVideoTitle.setText(item.getVideo().getVideoList().get(position).getVideoName());
                    TextView tvDuration = inflate.findViewById(R.id.tvDuration);
                    tvDuration.setText(stringForTime(Long.parseLong(item.getVideo().getVideoList().get(position).getVideoDuration())));

                    container.addView(inflate);

                    return inflate;
                }

                @Override
                public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                }
            });
        } else {

            pagerVideo.setVisibility(View.GONE);
            llVideoEmpty.setVisibility(View.VISIBLE);
        }
    }

    private String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;

        long minutes = (totalSeconds / 60) % 60;

        long hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return timeFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return timeFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    public void bindCaltureData(List<SocialCaltureListBean> data) {
        if (data != null) {
            adapter.setNewData(data);
        }
    }

    @SuppressLint("CheckResult")
    public void change2Edit(EditListCommunityCultureResponse r) {
        parseCaltureResult(r);
        llBottom.setVisibility(View.VISIBLE);
    }

    //解析社群文化为多类型data
    private void parseCaltureResult(EditListCommunityCultureResponse r) {

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

        bindCaltureData(caltures);
    }

    @Override
    public void onClick(View v) {
        if (doneAction != null) {
            if (adapter != null) {
                Iterator<SocialCaltureListBean> iterator = adapter.getData().iterator();
                while (iterator.hasNext()) {
                    SocialCaltureListBean bean = iterator.next();
                    switch (bean.getItemType()) {
                        case SocialCaltureListBean.TYPE_WEB:
                            if (bean.getOfficialWebsite().getOfficialWebsiteOpen().equals("0"))
                                iterator.remove();
                            break;
                        case SocialCaltureListBean.TYPE_FILE:
                            if (bean.getFiles().getFilesOpen().equals("0"))
                                iterator.remove();
                            break;
                        case SocialCaltureListBean.TYPE_VIDEO:
                            if (bean.getVideo().getVideoOpen().equals("0"))
                                iterator.remove();
                            break;
                        case SocialCaltureListBean.TYPE_APP:
                            if (bean.getApplication().getApplicationOpen().equals("0"))
                                iterator.remove();
                            break;
                        case SocialCaltureListBean.TYPE_ACTIVITY:
                            if (bean.getActivities().getActivityOpen().equals("0"))
                                iterator.remove();
                            break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
            doneAction.done(adapter.getData());
            llBottom.setVisibility(View.GONE);

            InformationNotificationMessage notificationMessage = InformationNotificationMessage.obtain("社群文化已更新");
            Message message = Message.obtain(groupId, Conversation.ConversationType.GROUP, notificationMessage);
            RongIM.getInstance().sendMessage(message, "", "", (IRongCallback.ISendMessageCallback) null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        if (requestCode == REQUEST_SETTINGWEB && resultCode == 1) {
            SocialCaltureListBean bean = data.getParcelableExtra("data");
            adapter.getData().add(0, bean);
            adapter.getData().remove(1);
            adapter.notifyItemChanged(0);
        }

        if (requestCode == REQUEST_SETTINGFILE && resultCode == 1) {
            SocialCaltureListBean bean = data.getParcelableExtra("data");
            if (bean.getFiles().getFilesList().size() == 0)
                bean.getFiles().setFilesOpen("0");
            adapter.getData().add(1, bean);
            adapter.getData().remove(2);
            adapter.notifyItemChanged(1);
        }

        if (requestCode == REQUEST_SETTINGVIDEO && resultCode == 1) {
            SocialCaltureListBean bean = data.getParcelableExtra("data");
            if (bean.getVideo().getVideoList().size() == 0)
                bean.getVideo().setVideoOpen("0");
            adapter.getData().add(2, bean);
            adapter.getData().remove(3);
            adapter.notifyItemChanged(2);
        }

        if (requestCode == REQUEST_SETTINGAPP && resultCode == 1) {
            SocialCaltureListBean bean = data.getParcelableExtra("data");
            if (bean.getApplication().getApplicationList().size() == 0)
                bean.getApplication().setApplicationOpen("0");
            adapter.getData().add(3, bean);
            adapter.getData().remove(4);
            adapter.notifyItemChanged(3);
        }

        if (requestCode == REQUEST_SETTINGACT && resultCode == 1) {
            SocialCaltureListBean bean = data.getParcelableExtra("data");
            adapter.getData().add(4, bean);
            adapter.getData().remove(5);
            adapter.notifyItemChanged(4);
        }
    }

    private File futureStudioIconFile;

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            FileUtils.createOrExistsFile(futureStudioIconFile);
            //初始化输入流
            InputStream inputStream = null;
            //初始化输出流
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void start2ShowPDF(String Path) {
        File file = new File(Path);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/pdf");
        startActivity(intent);
    }

    private void start2ShowDocument(String path, String format) {
        if (format.contains("doc")) {
            start2ShowWordFiles(path);
        } else if (format.contains("ppt")) {
            start2ShowPPT(path);
        } else if (format.contains("excel")) {
            start2ShowExcelFiles(path);
        } else {
            ToastUtils.showShort(R.string.cantparsefile);
        }
    }

    private void start2ShowPPT(String Path) {
        File file = new File(Path);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        startActivity(intent);
    }

    private void start2ShowWordFiles(String Path) {
        File file = new File(Path);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/msword");
        startActivity(intent);
    }

    private void start2ShowExcelFiles(String Path) {
        File file = new File(Path);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        startActivity(intent);
    }

    private void start2ShowVideo(String url) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.parse(url);
        intent.setDataAndType(uri, "video/*");
        startActivity(intent);
    }

}
