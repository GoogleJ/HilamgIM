package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;

import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.GroupResponse;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.utils.CommonUtils;
import io.rong.imkit.RongIM;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;

@SuppressLint("CheckResult")
public class UpdateInfoActivity extends BaseActivity {


    private static final int TYPE_GROUP_TITLE = 4;
    private EditText etChangeSign;
    private TextView tvChangeSign;
    private int type;

    private TextView tv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        initView();

        etChangeSign = findViewById(R.id.etChangeSign);
        tvChangeSign = findViewById(R.id.tvChangeSign);

        etChangeSign.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.toString().length();
                tvChangeSign.setText("" + (20 - length));
            }
        });

        Intent intent = getIntent();
        type = intent.getIntExtra("type", 0);
        if (type == TYPE_GROUP_TITLE) {
            tv_title.setText(R.string.changegroupname);
            etChangeSign.setHint(R.string.hint_groupname);
            GroupResponse group = (GroupResponse) getIntent().getSerializableExtra("group");
            if (group != null) {
                etChangeSign.setText(group.getGroupInfo().getGroupNikeName());
            }
        }
        etChangeSign.setSelection(etChangeSign.getText().toString().length());
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        TextView tv_commit = findViewById(R.id.tv_commit);
        tv_commit.setVisibility(View.VISIBLE);
        tv_commit.setText(getString(R.string.save));
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        tv_commit.setOnClickListener(v -> submit());
    }

    //保存
    public void submit() {
        String sign = etChangeSign.getText().toString().trim();
        if (sign.length() == 0) {
            ToastUtils.showShort(R.string.input_empty);
            return;
        }

        if (type == TYPE_GROUP_TITLE) {
            GroupResponse.GroupInfoBean request = new GroupResponse.GroupInfoBean();
            request.setId(getIntent().getStringExtra("groupId"));
            request.setGroupNikeName(sign);
            ServiceFactory.getInstance().getBaseService(Api.class)
                    .updateGroupInfo(GsonUtils.toJson(request))
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(UpdateInfoActivity.this)))
                    .compose(RxSchedulers.normalTrans())
                    .subscribe(s -> {
                        if (getIntent().getBooleanExtra("fromSocial", false)) {
                            InformationNotificationMessage notificationMessage = InformationNotificationMessage.obtain("社群改名为" + sign);
                            Message message = Message.obtain(getIntent().getStringExtra("groupId"), Conversation.ConversationType.GROUP, notificationMessage);
                            RongIM.getInstance().sendMessage(message, "", "", (IRongCallback.ISendMessageCallback) null);
                        }
                        Intent intent = new Intent();
                        intent.putExtra("result", sign);
                        setResult(2, intent);
                        finish();
                    }, this::handleApiError);
        }
    }
}
