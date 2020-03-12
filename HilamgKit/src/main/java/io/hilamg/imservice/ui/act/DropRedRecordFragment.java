package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.ReleaseRecordDetails;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseFragment;
import io.hilamg.imservice.ui.widget.NewsLoadMoreView;
import io.hilamg.imservice.utils.GlideUtil;

import java.text.SimpleDateFormat;

public class DropRedRecordFragment extends BaseFragment {

    public String groupId;
    public String symbol;
    private RecyclerView recyclerView;
    private BaseQuickAdapter adapter;
    private boolean hasInitData;
    private int page = 1;
    private int offset = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                             @NonNull Bundle savedInstanceState) {

        rootView = new SwipeRefreshLayout(getContext());
        ((SwipeRefreshLayout) rootView).setColorSchemeColors(Color.parseColor("#4585F5"));
        recyclerView = new RecyclerView(getContext());
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        ((SwipeRefreshLayout) rootView).addView(recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rootView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        ((SwipeRefreshLayout) rootView).setOnRefreshListener(() -> {
            page = 1;
            offset = 15;
            initData();
        });

        adapter = new BaseQuickAdapter<ReleaseRecordDetails.ListBean, BaseViewHolder>(R.layout.item_release_record_details) {
            @Override
            protected void convert(BaseViewHolder helper, ReleaseRecordDetails.ListBean bean) {
                helper.setText(R.id.tv_item_release_nick, bean.getNick())
                        .setText(R.id.tv_item_release_create_time, new SimpleDateFormat("yyyy.MM.dd HH:mm").format(Long.parseLong(bean.getCreateTime())))
                        .setText(R.id.tv_item_release_amount, bean.getAmount())
                        .setText(R.id.tv_item_release_currency, symbol);
                GlideUtil.loadCircleImg(helper.getView(R.id.img_item_release_head), bean.getHeadPortrait());
            }
        };
        adapter.setLoadMoreView(new NewsLoadMoreView());
        adapter.setEnableLoadMore(true);
        adapter.setOnLoadMoreListener(this::initData, recyclerView);

        recyclerView.setAdapter(adapter);

        ((SwipeRefreshLayout) rootView).setRefreshing(true);
        initData();
        return rootView;
    }

    @SuppressLint("CheckResult")
    private void initData() {
        ServiceFactory.getInstance().getBaseService(Api.class)
                .releaseRecordDetails(groupId, symbol, String.valueOf(page), String.valueOf(offset))
                .compose(RxSchedulers.ioObserver())
                .compose(RxSchedulers.normalTrans())
                .doOnTerminate(() -> ((SwipeRefreshLayout) rootView).setRefreshing(false))
                .subscribe(s -> {
                    page += 1;
                    if (!hasInitData) {
                        hasInitData = true;
                        adapter.setNewData(s.getList());
                        adapter.disableLoadMoreIfNotFullPage();
                    } else {
                        if (s.isHasNextPage()) {
                            adapter.loadMoreComplete();
                        } else {
                            adapter.loadMoreEnd(false);
                        }

                        if (page == 2) {
                            adapter.setNewData(s.getList());
                            adapter.disableLoadMoreIfNotFullPage();
                        } else {
                            adapter.addData(s.getList());
                        }
                    }
                }, t -> {
                    if (page != 1) {
                        adapter.loadMoreFail();
                    }
                    handleApiError(t);
                });
    }

}
