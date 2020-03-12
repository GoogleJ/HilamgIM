package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.trello.rxlifecycle3.android.ActivityEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.hilamg.imservice.Constant;
import io.hilamg.imservice.HilamgKit;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.DaoMaster;
import io.hilamg.imservice.bean.SendUrlAndsendImgBean;
import io.hilamg.imservice.bean.SlowModeLocalBeanDao;
import io.hilamg.imservice.bean.response.AllGroupMembersResponse;
import io.hilamg.imservice.bean.response.GroupResponse;
import io.hilamg.imservice.db.OpenHelper;
import io.hilamg.imservice.db.SlowModeLocalBean;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.im.rongIM.message.CusEmoteTabMessage;
import io.hilamg.imservice.ui.widget.dialog.NewRedDialog;
import io.hilamg.imservice.utils.CommonUtils;
import io.reactivex.schedulers.Schedulers;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.RongMessageItemLongClickActionManager;
import io.rong.imkit.mention.RongMentionManager;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.provider.MessageItemLongClickAction;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.CommandMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

@SuppressLint("CheckResult")
public class ConversationActivity extends BaseActivity {
    /**
     * Views
     */
    private TextView tvTitle;

    /**
     * 会话信息
     */
    private String targetId;
    private GroupResponse groupInfo;
    private String conversationType;
    private UserInfo targetUserInfo;

    /**
     * 融云监听
     */
    private RongIM.OnSendMessageListener onSendMessageListener;

    /**
     * 融云底部操作栏
     */
    private RongExtension extension;

    /**
     * db
     */
    private SlowModeLocalBeanDao slowModeLocalBeanDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setEnableTouchHideKeyBoard(false);

        RongMentionManager.getInstance().setGroupMembersProvider((s, callBack) ->
                ServiceFactory.getInstance().getBaseService(Api.class)
                        .getGroupMemByGroupId(s)
                        .compose(bindUntilEvent(ActivityEvent.DESTROY))
                        .compose(RxSchedulers.ioObserver())
                        .compose(RxSchedulers.normalTrans())
                        .subscribe(list -> {
                            ArrayList<UserInfo> result = new ArrayList<>(list.size());
                            for (AllGroupMembersResponse r : list) {
                                UserInfo member = new UserInfo(r.getId(), r.getNick(), Uri.parse(r.getHeadPortrait()));
                                result.add(member);
                            }
                            callBack.onGetGroupMembersResult(result);
                        }, t -> ToastUtils.showShort("暂时无法获取用户列表"))
        );

        initDao();

        setContentView(R.layout.activity_conversation);

        List<String> pathSegments = getIntent().getData().getPathSegments();
        conversationType = pathSegments.get(pathSegments.size() - 1);

        findViewById(R.id.rl_back).setOnClickListener(v -> finish());

        extension = findViewById(io.rong.imkit.R.id.rc_extension);

        registerMsgReceiver();

        registerSendMessageListener();

        handleBean();

        sendFakeC2CMsg();
    }

    private void sendFakeC2CMsg() {
        if (!conversationType.equals("private")) {
            return;
        }
        RongIM.getInstance().getConversation(Conversation.ConversationType.PRIVATE,
                targetId, new RongIMClient.ResultCallback<Conversation>() {
                    @Override
                    public void onSuccess(Conversation conversation) {
                        if (conversation == null || conversation.getLatestMessage() == null) {
                            InformationNotificationMessage message = InformationNotificationMessage.obtain("本次会话已开启端对端加密");
                            message.setExtra("本次会话已开启端对端加密");
                            RongIM.getInstance().insertIncomingMessage(
                                    Conversation.ConversationType.PRIVATE,
                                    targetId, Constant.userId, new Message.ReceivedStatus(1), message, null
                            );
                        }
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                    }
                });
    }

    private void initDao() {
        if (HilamgKit.daoSession == null) {
            OpenHelper open = new
                    OpenHelper(Utils.getApp(), Constant.userId, null);
            HilamgKit.daoSession = new DaoMaster(open.getWritableDatabase()).newSession();
        }

        slowModeLocalBeanDao = HilamgKit.daoSession.getSlowModeLocalBeanDao();
    }

    private void registerSendMessageListener() {
        onSendMessageListener = new RongIM.OnSendMessageListener() {
            @Override
            public Message onSend(Message message) {
                if (conversationType.equals("group") && groupInfo != null
                        && !groupInfo.getGroupInfo().getGroupOwnerId().equals(Constant.userId)
                        && groupInfo.getIsAdmin().equals("0")) {
                    if (!TextUtils.isEmpty(groupInfo.getGroupInfo().getSlowMode())) {
                        if (groupInfo.getGroupInfo().getSlowMode().equals("0")) {
                            return handleMsgForbiden(message);
                        }
                        slowModeLocalBeanDao.detachAll();
                        List<SlowModeLocalBean> localSlowList =
                                slowModeLocalBeanDao.queryBuilder()
                                        .where(SlowModeLocalBeanDao.Properties.GroupId.eq(targetId)).build().list();
                        if (localSlowList != null && localSlowList.size() != 0) {
                            SlowModeLocalBean slowModeLocalBean = localSlowList.get(0);
                            long passTime = (System.currentTimeMillis() - slowModeLocalBean.getLastMsgSentTime()) / 1000;
                            if (passTime < Integer.parseInt(groupInfo.getGroupInfo().getSlowMode())) {
                                String timeLeftStr = parstTimeLeftForSlowMode(
                                        Integer.parseInt(groupInfo.getGroupInfo().getSlowMode()) - passTime);
                                String toastTips = "群主开启了慢速模式，距下次发言时间:" + timeLeftStr;
                                ToastUtils.showShort(toastTips);
                                return null;
                            } else {
                                slowModeLocalBean.setLastMsgSentTime(System.currentTimeMillis());
                                slowModeLocalBeanDao.update(slowModeLocalBean);
                            }
                        } else {
                            SlowModeLocalBean slowModeLocalBean = new SlowModeLocalBean();
                            slowModeLocalBean.setGroupId(targetId);
                            slowModeLocalBean.setLastMsgSentTime(System.currentTimeMillis());
                            slowModeLocalBeanDao.insert(slowModeLocalBean);
                        }
                    }
                }

                return handleMsgForbiden(message);
            }

            @Override
            public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
                return false;
            }
        };

        RongIM.getInstance().setSendMessageListener(onSendMessageListener);
    }

    private String parstTimeLeftForSlowMode(long timeLeft) {
        if (timeLeft < 60) {
            return timeLeft + "秒";
        }
        if (timeLeft < 3600) {
            return timeLeft / 60 + "分钟";
        }

        return timeLeft / 60 / 60 + "小时";
    }

    private Message handleMsgForbiden(Message message) {
        if (!conversationType.equals("group")) {
            return message;
        }
        if (message.getContent() instanceof TextMessage &&
                groupInfo.getGroupInfo().getBanSendLink().equals("1")) {
            TextMessage textMessage = (TextMessage) message.getContent();
            if (RegexUtils.isMatch(Constant.regUrl, textMessage.getContent())) {
                ToastUtils.showShort(R.string.url_forbidden);
                return null;
            }
        } else if (message.getContent() instanceof ImageMessage &&
                groupInfo.getGroupInfo().getBanSendPicture().equals("1")) {
            ToastUtils.showShort(R.string.picture_forbidden);
            return null;
        } else if (message.getContent() instanceof CusEmoteTabMessage && groupInfo.getGroupInfo().getBanSendPicture().equals("1")) {
            ToastUtils.showShort(R.string.picture_forbidden);
            return null;
        }
        return message;
    }

    private void registerMsgReceiver() {
        RongIM.setOnReceiveMessageListener((message, i) -> {
            if (message.getObjectName().equals("RC:CmdMsg")) {
                CommandMessage commandMessage = (CommandMessage) message.getContent();
                if (!TextUtils.isEmpty(commandMessage.getName())) {
                    if ("sendUrlAndsendImg".equals(commandMessage.getName())) {
                        SendUrlAndsendImgBean sendUrlAndsendImgBean = GsonUtils.fromJson(commandMessage.getData(), SendUrlAndsendImgBean.class);
                        groupInfo.getGroupInfo().setBanSendLink(sendUrlAndsendImgBean.getSendUrl());
                        groupInfo.getGroupInfo().setBanSendPicture(sendUrlAndsendImgBean.getSendImg());
                    }
                }
            } else if (message.getObjectName().equals("RC:InfoNtf")) {
                //小灰条
                InformationNotificationMessage notificationMessage = (InformationNotificationMessage) message.getContent();
                if (!TextUtils.isEmpty(notificationMessage.getExtra())) {
                    if (notificationMessage.getExtra().contains("慢速模式")) {
                        handleReceiveSlowMode(notificationMessage);
                    }
                }
            }
            return false;
        });

    }

    //群主开启慢速模式
    private void handleReceiveSlowMode(InformationNotificationMessage message) {
        if (message.getExtra().contains("关闭")) {
            groupInfo.getGroupInfo().setSlowMode("0");
        } else {
            String slowModeTimeStr = message.getExtra().substring(15);
            switch (slowModeTimeStr) {
                case "30秒":
                    groupInfo.getGroupInfo().setSlowMode("30");
                    break;
                case "1分钟":
                    groupInfo.getGroupInfo().setSlowMode("60");
                    break;
                case "5分钟":
                    groupInfo.getGroupInfo().setSlowMode("300");
                    break;
                case "10分钟":
                    groupInfo.getGroupInfo().setSlowMode("600");
                    break;
                case "1小时":
                    groupInfo.getGroupInfo().setSlowMode("3600");
                    break;
            }
        }
    }

    private void handleBean() {
        targetId = getIntent().getData().getQueryParameter("targetId");

        if (conversationType.equals("group")) {
            ServiceFactory.getInstance().getBaseService(Api.class)
                    .getGroupByGroupId(targetId)
                    .compose(RxSchedulers.normalTrans())
                    .doOnNext(groupResponse -> {
                        //refresh local cache when serve logic change
                        Group ronginfo = RongUserInfoManager.getInstance().getGroupInfo(groupResponse.getGroupInfo().getId());
                        if (ronginfo == null) return;

                        String tempName = groupResponse.getGroupInfo().getGroupNikeName();
                        if (groupResponse.getGroupInfo().getGroupType().equals("1")) {
                            tempName = tempName + "おれは人间をやめるぞ！ジョジョ―――ッ!";
                        }
                        String tempGroupHead = groupResponse.getGroupInfo().getHeadPortrait();
                        //refresh local cache when serve logic change
                        if (ronginfo.getPortraitUri() == null ||
                                TextUtils.isEmpty(ronginfo.getPortraitUri().toString()) ||
                                TextUtils.isEmpty(ronginfo.getName()) ||
                                !ronginfo.getName().equals(tempName) ||
                                !ronginfo.getPortraitUri().toString().equals(tempGroupHead)) {
                            RongIM.getInstance().refreshGroupInfoCache(new Group(groupResponse.getGroupInfo().getId(), tempName, Uri.parse(tempGroupHead)));
                        }
                    })
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                    .subscribe(groupInfo -> {
                        handleNewReceiveRed(groupInfo);

                        handleGroupPlugin(groupInfo);

                        this.groupInfo = groupInfo;

                        handleGroupOwnerInit();

                        initView();

                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .getGroupMemByGroupId(targetId)
                                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                                .subscribeOn(Schedulers.io())
                                .compose(RxSchedulers.normalTrans())
                                .subscribe(groupMemberList -> {
                                    for (AllGroupMembersResponse b : groupMemberList) {
                                        if (RongUserInfoManager.getInstance().getUserInfo(b.getId()) == null) {
                                            RongIM.getInstance().refreshUserInfoCache(new UserInfo(b.getId(), TextUtils.isEmpty(b.getRemark()) ? b.getNick() : b.getRemark()
                                                    , Uri.parse(b.getHeadPortrait())));
                                        }
                                    }
                                });
                    }, t -> {
                        extension.removeAllViews();
                        handleApiError(t);
                        RongIM.getInstance().removeConversation(Conversation.ConversationType.GROUP, targetId, null);
                        finish();
                    });
        }
    }

    /**
     * 处理群主初始化逻辑
     */
    private void handleGroupOwnerInit() {
        boolean isOwner = groupInfo.getGroupInfo().getGroupOwnerId().equals(Constant.userId);

        List<MessageItemLongClickAction> actionList = RongMessageItemLongClickActionManager.getInstance().getMessageItemLongClickActions();
        Iterator<MessageItemLongClickAction> iterator = actionList.iterator();
        while (iterator.hasNext()) {
            MessageItemLongClickAction next = iterator.next();
            if (next.getTitle(this).equals("强制撤回")) {
                iterator.remove();
                break;
            }
        }

        if (isOwner || groupInfo.getGroupPermission() != null && groupInfo.getGroupPermission().getForceRecall().equals("1")) {
            MessageItemLongClickAction forceRecallAction = new MessageItemLongClickAction.Builder()
                    .title("强制撤回")
                    .showFilter(uiMessage -> {

                        String senderUserId = uiMessage.getSenderUserId();

                        groupInfo.getGroupInfo().getGroupOwnerId();

                        MessageContent messageContent = uiMessage.getContent();

                        return (messageContent instanceof TextMessage || messageContent instanceof VoiceMessage || messageContent instanceof ImageMessage) &&
                                !senderUserId.equals(Constant.userId) && !senderUserId.equals(groupInfo.getGroupInfo().getGroupOwnerId());
                    })
                    .actionListener((context, uiMessage) -> {
                        ServiceFactory.getInstance().getBaseService(Api.class)
                                .recallGroupMessage(uiMessage.getMessage().getUId())
                                .compose(bindToLifecycle())
                                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(ConversationActivity.this)))
                                .compose(RxSchedulers.normalTrans())
                                .subscribe(s -> {
                                }, ConversationActivity.this::handleApiError);
                        return true;
                    }).build();
            RongMessageItemLongClickActionManager.getInstance().addMessageItemLongClickAction(forceRecallAction);
        }
    }

    /**
     * 进群领红包
     *
     * @param groupInfo 群组信息
     */
    private void handleNewReceiveRed(GroupResponse groupInfo) {
        if (!groupInfo.getRedPacketInfo().getRedNewPersonStatus().equals("1")) return;

        if ("0".equals(groupInfo.getRedPacketInfo().getIsGetNewPersonRed())) {
            NewRedDialog newRedDialog = new NewRedDialog(ConversationActivity.this, NewRedDialog.TYPE1_NORMAL);
            newRedDialog.setOpenListener(()
                    -> ServiceFactory.getInstance().getBaseService(Api.class)
                    .receiveNewPersonRedPackage(groupInfo.getGroupInfo().getId())
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.normalTrans())
                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(ConversationActivity.this)))
                    .subscribe(r -> {
                                if (r.getResult().equals("0")) {
                                    new NewRedDialog(ConversationActivity.this, NewRedDialog.TYPE3_RECEIVED).showReceived(r.getEveryoneAwardCount(), r.getSymbol());
                                } else {
                                    new NewRedDialog(ConversationActivity.this, NewRedDialog.TYPE2_EXPIRED).showExpired1();
                                }
                            },
                            ConversationActivity.this::handleApiError));
            newRedDialog.show(groupInfo.getGroupInfo().getOwnerHeadPortrait(),
                    groupInfo.getGroupInfo().getGroupOwnerNick(), getString(R.string.newredtip));
        }
    }

    private void handleGroupPlugin(GroupResponse groupInfo) {
        List<IPluginModule> pluginModules = extension.getPluginModules();

        if (groupInfo.getGroupInfo().getIsDelete().equals("1")) {
            //群不存在，删除plugin
            Iterator<IPluginModule> iterator = pluginModules.iterator();
            while (iterator.hasNext()) {
                IPluginModule next = iterator.next();
                iterator.remove();
                extension.removePlugin(next);
            }
        }
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_title);
        RelativeLayout rl_end = findViewById(R.id.rl_end);
        rl_end.setVisibility(View.VISIBLE);
        View dotSocialContentUpdate = findViewById(R.id.dotSocialContentUpdate);

        tvTitle.setText(targetUserInfo == null ?
                (groupInfo == null ? ("")
                        : (groupInfo.getGroupInfo().getGroupNikeName() + "(" + groupInfo.getGroupInfo().getCustomerNumber() + ")"))
                : targetUserInfo.getName());

        //group logic
        if (groupInfo != null && groupInfo.getGroupInfo().getGroupType().equals("1")) {
            tvTitle.setTextColor(Color.parseColor("#EC7A00"));
            ImageView iv_end = findViewById(R.id.iv_end);
            iv_end.setImageDrawable(getDrawable(R.drawable.ic_social_end));
            rl_end.setOnClickListener(v -> {
                Intent intent = new Intent(this, SocialManageActivity.class);
                intent.putExtra("group", groupInfo);
                startActivityForResult(intent, 1000);
            });
            tvTitle.setText(groupInfo.getGroupInfo().getGroupNikeName());
            tvTitle.setOnClickListener(v -> {
                dotSocialContentUpdate.setVisibility(View.GONE);
                Intent intent = new Intent(this, SocialHomeActivity.class);
                intent.putExtra("id", groupInfo.getGroupInfo().getId());
                intent.putExtra("fromConversatin", true);
                startActivity(intent);
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == 1000) {
            if (groupInfo != null) {
                groupInfo = (GroupResponse) data.getSerializableExtra("group");
                if (groupInfo.getGroupInfo().getGroupType().equals("1")) {
                    tvTitle.setText(data.getStringExtra("title"));
                } else {
                    tvTitle.setText(data.getStringExtra("title") + "(" + groupInfo.getGroupInfo().getCustomerNumber() + ")");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        onSendMessageListener = null;
        RongIMClient.setTypingStatusListener(null);
        RongIM.getInstance().setSendMessageListener(null);
        RongIM.setConversationClickListener(null);
        RongIM.getInstance().logout();
        super.onDestroy();
    }
}
