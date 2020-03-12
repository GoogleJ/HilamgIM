package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.GroupManagementInfoBean;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.ui.widget.NewsLoadMoreView;
import io.hilamg.imservice.ui.widget.dialog.MuteRemoveDialog;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.GlideUtil;

public class MuteManageActivity extends BaseActivity {

    private TextView tv_title;
    private RelativeLayout rl_back;
    private EditText etSearch;
    private ViewPager pager;
    private TabLayout indicator;

    private int[] detailTitles = {R.string.group_member, R.string.list_mute, R.string.list_kickout};

    private List<BaseQuickAdapter> adapters = new ArrayList<>(3);
    private List<GroupManagementInfoBean> data1;
    private List<GroupManagementInfoBean> data2;
    private List<GroupManagementInfoBean> data3;
    private int currentPosition;

    private Api api;

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mute_manage);

        api = ServiceFactory.getInstance().getBaseService(Api.class);
        groupId = getIntent().getStringExtra("groupId");

        initView();
        initData();
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        rl_back = findViewById(R.id.rl_back);
        etSearch = findViewById(R.id.etSearch);
        pager = findViewById(R.id.pager);
        indicator = findViewById(R.id.indicator);
    }

    private void initData() {
        tv_title.setText(R.string.mute_manage);
        rl_back.setOnClickListener(v -> finish());
        pager.setOffscreenPageLimit(5);
        pager.setAdapter(new DetailPagerAdapter());
        indicator.setupWithViewPager(pager);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString();
                if (TextUtils.isEmpty(s)) {
                    adapters.get(currentPosition).setNewData(currentPosition == 0 ? data1 : (currentPosition == 1 ? data2 : data3));
                    return;
                }
                List<GroupManagementInfoBean> temp = new ArrayList<>();
                switch (currentPosition) {
                    case 0:
                        for (int i = 0; i < data1.size(); i++) {
                            if (data1.get(i).getNick().contains(s)) temp.add(data1.get(i));
                        }
                        break;
                    case 1:
                        for (int i = 0; i < data2.size(); i++) {
                            if (data2.get(i).getNick().contains(s)) temp.add(data2.get(i));
                        }
                        break;
                    case 2:
                        for (int i = 0; i < data3.size(); i++) {
                            if (data3.get(i).getNick().contains(s)) temp.add(data3.get(i));
                        }
                        break;
                }
                adapters.get(currentPosition).setNewData(temp);
            }
        });
    }


    class DetailPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return detailTitles.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @SuppressLint("CheckResult")
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            RecyclerView recycler = new RecyclerView(MuteManageActivity.this);
            recycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
            recycler.setLayoutManager(new LinearLayoutManager(MuteManageActivity.this));

            BaseQuickAdapter<GroupManagementInfoBean, BaseViewHolder> adapter;
            adapter = new BaseQuickAdapter<GroupManagementInfoBean, BaseViewHolder>(R.layout.item_mute_manage, null) {
                @Override
                protected void convert(BaseViewHolder helper, GroupManagementInfoBean bean) {

                    helper.setText(R.id.tvName, bean.getNick());

                    GlideUtil.loadCircleImg(helper.getView(R.id.ivHead), bean.getHeadPortrait());

                    TextView tv1 = helper.getView(R.id.tv1);
                    TextView tv2 = helper.getView(R.id.tv2);

                    switch (position) {
                        case 0:
                            tv1.setText("禁言");
                            tv2.setText("踢出");
                            tv1.setOnClickListener(v -> api.muteMembers(groupId, bean.getId(), "add")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data2.add(0, data1.get(helper.getAdapterPosition()));
                                        adapters.get(1).notifyItemInserted(0);

                                        data1.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));
                            tv2.setOnClickListener(v -> api.kickOutORRemove(groupId, bean.getId(), "kickOut")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data3.add(0, data1.get(helper.getAdapterPosition()));
                                        adapters.get(2).notifyItemInserted(0);

                                        data1.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));
                            break;
                        case 1:
                            tv1.setText("取消");
                            tv2.setText("移除");
                            tv1.setSelected(true);
                            tv1.setOnClickListener(v -> api.muteMembers(groupId, bean.getId(), "remove")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data1.add(0, data2.get(helper.getAdapterPosition()));
                                        adapters.get(0).notifyItemInserted(0);

                                        data2.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));

                            MuteRemoveDialog d = new MuteRemoveDialog(MuteManageActivity.this);

                            d.setOnCancelListener(() -> api.kickOutORRemove(groupId, bean.getId(), "remove")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data2.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));

                            d.setOnCommitListener(() -> api.kickOutORRemove(groupId, bean.getId(), "kickOut")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data3.add(0, data2.get(helper.getAdapterPosition()));
                                        adapters.get(2).notifyItemInserted(0);
                                        data2.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));

                            tv2.setOnClickListener(v -> d.show());
                            break;
                        case 2:
                            tv1.setVisibility(View.GONE);
                            tv2.setText("移除");
                            tv2.setOnClickListener(v -> api.kickOutORRemove(groupId, bean.getId(), "remove")
                                    .compose(bindToLifecycle())
                                    .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(MuteManageActivity.this)))
                                    .compose(RxSchedulers.normalTrans())
                                    .subscribe(c -> {
                                        data3.remove(helper.getAdapterPosition());
                                        notifyItemRemoved(helper.getAdapterPosition());
                                    }, MuteManageActivity.this::handleApiError));
                            break;
                    }
                }
            };

            adapters.add(position, adapter);

            adapter.setEnableLoadMore(true);
            adapter.setLoadMoreView(new NewsLoadMoreView());

            recycler.setAdapter(adapter);

            api.getGroupManagementInfo(groupId, position + "")
                    .compose(bindToLifecycle())
                    .compose(RxSchedulers.ioObserver())
                    .compose(RxSchedulers.normalTrans())
                    .subscribe(list -> {
                        adapter.setNewData(list);
                        adapter.loadMoreEnd(true);
                        if (position == 0) data1 = list;
                        if (position == 1) data2 = list;
                        if (position == 2) data3 = list;
                    }, MuteManageActivity.this::handleApiError);

            container.addView(recycler);

            return recycler;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return getString(detailTitles[position]);
        }
    }
}
