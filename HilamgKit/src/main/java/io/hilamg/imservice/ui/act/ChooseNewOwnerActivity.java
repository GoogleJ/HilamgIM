package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;

import io.hilamg.imservice.R;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.im.adapter.ChooseNewOwnerAdapter;
import io.hilamg.imservice.utils.CommonUtils;

import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;

@SuppressLint("CheckResult")
public class ChooseNewOwnerActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private ChooseNewOwnerAdapter mAdapter;
    private String groupId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_new_owner);
        groupId = getIntent().getStringExtra("groupId");
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.choose_a_new_owner_title);
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        mRecyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new ChooseNewOwnerAdapter();
        getGroupMemByGroupId(groupId);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemChildClickListener((adapter, view, position) ->
                showNewDialog(position));
    }

    private void showNewDialog(int position){
        View view = LayoutInflater.from(this).inflate(R.layout.layout_general_dialog4,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();

        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText("确定选择为新群主吗?");
        TextView tvCancel = view.findViewById(R.id.tv_cancel);
        tvCancel.setText("取消");
        TextView tvNotarize = view.findViewById(R.id.tv_notarize);
        tvNotarize.setText("确定");
        tvCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        tvNotarize.setOnClickListener(v -> {
            updateGroupOwner(groupId, mAdapter.getData().get(position).getId(), mAdapter.getData().get(position).getNick());
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout((getScreenWidth(this)/4*3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }


    private void getGroupMemByGroupId(String groupId) {
        ServiceFactory.getInstance().getBaseService(Api.class)
                .getGroupMemByGroupId(groupId)
                .compose(bindToLifecycle())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                .compose(RxSchedulers.normalTrans())
                .subscribe(allGroupMembersResponses -> {
                    allGroupMembersResponses.remove(0);
                    mAdapter.setNewData(allGroupMembersResponses);
                }, this::handleApiError);
    }

    private void updateGroupOwner(String groupId, String customerId, String newOwnerNick) {
        ServiceFactory.getInstance().getBaseService(Api.class)
                .updateGroupOwner(groupId, customerId)
                .compose(bindToLifecycle())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                .compose(RxSchedulers.normalTrans())
                .subscribe(s -> {
                    ToastUtils.showShort(ChooseNewOwnerActivity.this.getString(R.string.transfer_group_successful));

                    InformationNotificationMessage notificationMessage = InformationNotificationMessage.obtain(newOwnerNick + "成为了新的群主");
                    Message message = Message.obtain(groupId, Conversation.ConversationType.GROUP, notificationMessage);
                    RongIM.getInstance().sendMessage(message, "", "", (IRongCallback.ISendMessageCallback) null);

                    Intent intent = new Intent(ChooseNewOwnerActivity.this, ConversationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }, this::handleApiError);

    }

    private  int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
