package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.GetUpgradeGroupsResponnse;
import io.hilamg.imservice.bean.response.GroupResponse;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.widget.NewPayBoard;
import io.hilamg.imservice.ui.widget.dialog.MuteRemoveDialog;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.MD5Utils;

@SuppressLint("CheckResult")
public class UpdateGroupLimitActivity extends BaseActivity {
    private GroupResponse group;
    private TextView tvGroupLimit;
    private RecyclerView recycler;
    private BaseQuickAdapter adapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_limit);

        group = (GroupResponse) getIntent().getSerializableExtra("group");
        TextView title = findViewById(R.id.tv_title);
        title.setText(R.string.update_group_limit);
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());

        tvGroupLimit = findViewById(R.id.tvGroupLimit);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        float itemHeight = (ScreenUtils.getScreenWidth() - CommonUtils.dip2px(this, 36)) / 2f;
        adapter = new BaseQuickAdapter<GetUpgradeGroupsResponnse.GroupLevelsInfoListBean, BaseViewHolder>(
                R.layout.item_updategrouplimit) {
            @Override
            protected void convert(BaseViewHolder helper, GetUpgradeGroupsResponnse.GroupLevelsInfoListBean item) {
                helper.setText(R.id.tvtips, item.getFee() + item.getSymbol() + "永久升级")
                        .setText(R.id.tvNum, item.getLimitNumber())
                        .setText(R.id.btnUpdate, item.getIsUpgrade().equals("1") ? "已升级" : "升级");
                helper.setBackgroundRes(R.id.btnUpdate, item.getIsUpgrade().equals("1") ? R.drawable.shape_unable
                        : R.drawable.shape_theme1);
                LinearLayout ll = helper.getView(R.id.ll);
                ViewGroup.LayoutParams layoutParams = ll.getLayoutParams();
                layoutParams.height = (int) itemHeight;
                ll.setLayoutParams(layoutParams);

                Button btnUpdate = helper.getView(R.id.btnUpdate);
                if (!item.getIsUpgrade().equals("1")) {
                    btnUpdate.setOnClickListener(v -> {
                        MuteRemoveDialog dialog = new MuteRemoveDialog(UpdateGroupLimitActivity.this, "确定", "取消", "提示", "是否确定升级该群");
                        dialog.setOnCancelListener(() -> new NewPayBoard(UpdateGroupLimitActivity.this)
                                .show(psw -> ServiceFactory.getInstance().getBaseService(Api.class)
                                        .payToUpgradeGroup(group.getGroupInfo().getId(),
                                                MD5Utils.getMD5(psw), item.getGroupTag(), item.getFee())
                                        .compose(bindToLifecycle())
                                        .compose(RxSchedulers.normalTrans())
                                        .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(UpdateGroupLimitActivity.this)))
                                        .subscribe(s -> {
                                            ToastUtils.showShort(R.string.update_success1);
                                            group.getGroupInfo().setLimitNumber(item.getLimitNumber());
                                            finish();
                                        }, UpdateGroupLimitActivity.this::handleApiError)));
                        dialog.show();
                    });
                }
            }
        };
        recycler.setAdapter(adapter);

        ServiceFactory.getInstance().getBaseService(Api.class)
                .getUpgradeGroups(group.getGroupInfo().getId())
                .compose(bindToLifecycle())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                .compose(RxSchedulers.normalTrans())
                .subscribe(r -> {
                    tvGroupLimit.setText(r.getGroupLevelInfo().getLimitNumber());
                    adapter.setNewData(r.getGroupLevelsInfoList());
                }, this::handleApiError);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("group", group);
        setResult(1, intent);
        super.finish();
    }
}
