package io.hilamg.imservice.ui.act;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.ReleaseRecord;
import io.hilamg.imservice.network.Api;
import io.hilamg.imservice.network.ServiceFactory;
import io.hilamg.imservice.network.rx.RxSchedulers;
import io.hilamg.imservice.ui.act.base.BaseActivity;
import io.hilamg.imservice.utils.CommonUtils;
import io.hilamg.imservice.utils.GlideUtil;



import java.util.List;

@SuppressLint("CheckResult")
public class DropRedRecordActivity extends BaseActivity {

    private TabLayout mDropRedMagicIndicator;
    private ViewPager mDropRedViewPager;
    private TextView mTvDropRecordLaveCount;
    private TextView mTvDropRecordCurrency;
    private TextView mTvDropRecordAirdrops;
    private TextView mTvDropRecordStatus;
    private TextView mTvTitle;
    private LinearLayout mLlDropRecord;
    private FrameLayout mFlDropNoRecord;

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_red_record);

        initView();

        initData();
    }

    private void initView() {
        mDropRedMagicIndicator = findViewById(R.id.drop_red_indicator);
        mDropRedViewPager = findViewById(R.id.drop_red_pager);
        mTvDropRecordLaveCount = findViewById(R.id.tv_drop_record_laveCount);
        mTvDropRecordCurrency = findViewById(R.id.tv_drop_record_currency);
        mTvDropRecordAirdrops = findViewById(R.id.tv_drop_record_airdrops);
        mTvDropRecordStatus = findViewById(R.id.tv_drop_record_status);
        mTvTitle = findViewById(R.id.tv_title);
        mLlDropRecord = findViewById(R.id.ll_drop_record);
        mFlDropNoRecord = findViewById(R.id.fl_drop_no_record);
    }

    @SuppressLint("CheckResult")
    private void initData() {
        mTvTitle.setText("空投记录");
        findViewById(R.id.rl_back).setOnClickListener(v -> finish());
        groupId = getIntent().getStringExtra("groupId");
        ServiceFactory.getInstance().getBaseService(Api.class)
                .releaseRecord(groupId)
                .compose(bindToLifecycle())
                .compose(RxSchedulers.normalTrans())
                .compose(RxSchedulers.ioObserver(CommonUtils.initDialog(this)))
                .subscribe(s -> {
                    if (!s.isEmpty()) {
                        mLlDropRecord.setVisibility(View.VISIBLE);
                        mFlDropNoRecord.setVisibility(View.GONE);
                        onMagicIndicator(s);
                    } else {
                        mLlDropRecord.setVisibility(View.GONE);
                        mFlDropNoRecord.setVisibility(View.VISIBLE);
                    }
                }, this::handleApiError);
    }


    private void onMagicIndicator(List<ReleaseRecord> releaseRecords) {
        mDropRedViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @NonNull
            @Override
            public Fragment getItem(int position) {
                DropRedRecordFragment dropRedRecordFragment = new DropRedRecordFragment();
                dropRedRecordFragment.groupId = groupId;
                dropRedRecordFragment.symbol = releaseRecords.get(position).getSymbol();

                return dropRedRecordFragment;
            }

            @Override
            public int getCount() {
                return releaseRecords.size();
            }
        });


        for (int i = 0; i < releaseRecords.size(); i++) {
            mDropRedMagicIndicator.addTab(mDropRedMagicIndicator.newTab());
            TabLayout.Tab tab = mDropRedMagicIndicator.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.pager_title_view);
                View view = tab.getCustomView();
                if (view != null) {
                    ImageView titleImg = view.findViewById(R.id.title_img);
                    GlideUtil.loadNormalImg(titleImg, releaseRecords.get(i).getLogo());
                    TextView titleText = view.findViewById(R.id.title_text);
                    titleText.setText(releaseRecords.get(i).getSymbol());
                }
            }

            mTvDropRecordLaveCount.setText(releaseRecords.get(i).getLaveCount());
            mTvDropRecordCurrency.setText(releaseRecords.get(i).getSymbol());
            mTvDropRecordAirdrops.setText(releaseRecords.get(i).getAirdrops());
            if (releaseRecords.get(i).getStatus().equals("0")) {
                mTvDropRecordStatus.setText("未开始");
                mTvDropRecordStatus.setTextColor(getResources().getColor(R.color.count_down));
            } else if (releaseRecords.get(i).getStatus().equals("1")) {
                mTvDropRecordStatus.setText("进行中");
                mTvDropRecordStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else if (releaseRecords.get(i).getStatus().equals("2")) {
                mTvDropRecordStatus.setText("已结束");
                mTvDropRecordStatus.setTextColor(getResources().getColor(R.color.text_select_color));
            } else if (releaseRecords.get(i).getStatus().equals("3")) {
                mTvDropRecordStatus.setText("已退回");
                mTvDropRecordStatus.setTextColor(getResources().getColor(R.color.text_select_color));
            }
        }
    }
}
