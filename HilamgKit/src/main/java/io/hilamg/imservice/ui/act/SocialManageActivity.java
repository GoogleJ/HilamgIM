package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ToastUtils;
import io.hilamg.imservice.Constant;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.GroupResponse;
import io.hilamg.imservice.ui.act.base.BaseActivity;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class SocialManageActivity extends BaseActivity {

    private TextView tvSocialName;
    private TextView tvNick;
    private LinearLayout llOwner;
    private LinearLayout llNewOwner;
    private LinearLayout llReport;
    private Switch switch1;
    private Switch switch2;
    private LinearLayout ll_groupmanager;

    private GroupResponse group;

    private String identity;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_manage);

        group = (GroupResponse) getIntent().getSerializableExtra("group");

        TextView title = findViewById(R.id.tv_title);
        title.setText(R.string.ic_social_end_pop2);
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());

        tvSocialName = findViewById(R.id.tvSocialName);
        tvNick = findViewById(R.id.tvNick);
        llOwner = findViewById(R.id.llOwner);
        llNewOwner = findViewById(R.id.llNewOwner);
        llReport = findViewById(R.id.llReport);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        ll_groupmanager = findViewById(R.id.ll_groupmanager);
        tvNick.setText(Constant.nick);
        tvSocialName.setText(group.getGroupInfo().getGroupNikeName());


        if (group.getGroupInfo().getGroupOwnerId().equals(Constant.userId)) {
            identity = "2";
        } else if (group.getIsAdmin().equals("1")) {
            identity = "1";
        } else {
            identity = "0";
        }

        if (!"0".equals(identity)) {
            group = (GroupResponse) getIntent().getSerializableExtra("group");
        }
        switch (identity) {
            case "0":
                //成员
                llReport.setVisibility(View.VISIBLE);
                break;
            case "1":
                //管理
                ll_groupmanager.setVisibility(View.VISIBLE);
                break;
            case "2":
                //群主
                llOwner.setVisibility(View.VISIBLE);
                llNewOwner.setVisibility(View.VISIBLE);
                break;
        }

        RongIM.getInstance().getConversationNotificationStatus(Conversation.ConversationType.GROUP, group.getGroupInfo().getId(), new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
            @Override
            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.NOTIFY) {
                    switch2.setChecked(false);
                } else {
                    switch2.setChecked(true);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
            }
        });
        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Conversation.ConversationNotificationStatus status;
            if (isChecked) {
                status = Conversation.ConversationNotificationStatus.DO_NOT_DISTURB;
            } else {
                status = Conversation.ConversationNotificationStatus.NOTIFY;
            }
            RongIM.getInstance().setConversationNotificationStatus(Conversation.ConversationType.GROUP, group.getGroupInfo().getId(), status, null);
        });

        RongIM.getInstance().getConversation(Conversation.ConversationType.GROUP, group.getGroupInfo().getId(), new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                if (conversation != null) switch1.setChecked(conversation.isTop());
                else switch1.setEnabled(false);
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
            }
        });
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> RongIM.getInstance().setConversationToTop(Conversation.ConversationType.GROUP, group.getGroupInfo().getId(), isChecked, null));
    }

    public void socialName(View view) {
        if (!identity.equals("0")) {
            Intent intent = new Intent(this, UpdateInfoActivity.class);
            intent.putExtra("type", 4);
            intent.putExtra("groupId", group.getGroupInfo().getId());
            intent.putExtra("group", group);
            intent.putExtra("fromSocial", true);
            startActivityForResult(intent, 1);
        } else {
            ToastUtils.showShort(getString(R.string.no_update_nick));
        }
    }

    public void clearHistory(View view) {
        showNewDialog();
    }


    private void showNewDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_general_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText("提示");
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText("确定要清空当前聊天记录吗?");
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        tvCancel.setText("取消");
        TextView tvNotarize = view.findViewById(R.id.tv_notarize);
        tvNotarize.setText("确认");
        tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        tvNotarize.setOnClickListener(v -> RongIM.getInstance().clearMessages(Conversation.ConversationType.GROUP, group.getGroupInfo().getId(), new RongIMClient.ResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                dialog.dismiss();
                finish();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                dialog.dismiss();
                ToastUtils.showShort(R.string.function_fail);
            }
        }));

        dialog.show();
        dialog.getWindow().setLayout((getScreenWidth(this)/4*3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private  int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public void report(View view) {
        startActivity(new Intent(this, SkinReportActivity.class));
    }

    public void chooseNewOwner(View view) {
        Intent intent = new Intent(this, ChooseNewOwnerActivity.class);
        intent.putExtra("groupId", group.getGroupInfo().getId());
        intent.putExtra("fromSocial", true);
        startActivity(intent);
    }

    //群主权限管理
    public void ownerAuthority(View view) {
        Intent intent = new Intent(this, OwnerGroupAuthorityActivity.class);
        intent.putExtra("group", group);
        startActivityForResult(intent, 1);
    }

    //群主群管理
    public void ownerGroupManage(View view) {
        Intent intent = new Intent(this, OwnerGroupManageActivity.class);
        intent.putExtra("group", group);
        intent.putExtra("fromsocial", true);
        startActivityForResult(intent, 1);
    }

    //管理员禁言管理
    public void managerMute(View view) {
        GroupResponse.PermissionBean permission = group.getGroupPermission();
        if (permission.getOpenBanned().equals("0")) {
            ToastUtils.showShort(R.string.nopermisson);
            return;
        }
        Intent intent = new Intent(this, MuteManageActivity.class);
        intent.putExtra("groupId", group.getGroupInfo().getId());
        startActivity(intent);
    }

    //管理员视频直播
    public void managerVideo(View view) {
        GroupResponse.PermissionBean permission = group.getGroupPermission();
        if (permission.getOpenVideo().equals("0")) {
            ToastUtils.showShort(R.string.nopermisson);
            return;
        }
        ToastUtils.showShort(R.string.developing);
    }

    //管理员语音直播
    public void managerAudio(View view) {
        GroupResponse.PermissionBean permission = group.getGroupPermission();
        if (permission.getOpenAudio().equals("0")) {
            ToastUtils.showShort(R.string.nopermisson);
            return;
        }
        ToastUtils.showShort(R.string.developing);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == 2) {
            if (data.getSerializableExtra("group") != null) {
                group = (GroupResponse) data.getSerializableExtra("group");
            }
            if (!TextUtils.isEmpty(data.getStringExtra("result"))) {
                group.getGroupInfo().setGroupNikeName(data.getStringExtra("result"));
                tvSocialName.setText(data.getStringExtra("result"));
            }
        }
    }

    @Override
    public void finish() {
        if (group != null) {
            Intent intent = new Intent();
            intent.putExtra("title", group.getGroupInfo().getGroupNikeName());
            intent.putExtra("group", group);
            this.setResult(1000, intent);
        }
        super.finish();
    }
}
