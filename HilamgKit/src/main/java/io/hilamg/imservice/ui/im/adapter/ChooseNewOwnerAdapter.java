package io.hilamg.imservice.ui.im.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import io.hilamg.imservice.R;
import io.hilamg.imservice.bean.response.AllGroupMembersResponse;
import io.hilamg.imservice.utils.GlideUtil;


public class ChooseNewOwnerAdapter extends BaseQuickAdapter<AllGroupMembersResponse, BaseViewHolder> {
    public ChooseNewOwnerAdapter() {
        super(R.layout.item_choose_new_owner);
    }

    @Override
    protected void convert(BaseViewHolder helper, AllGroupMembersResponse item) {
        helper.setText(R.id.user_name, item.getNick())
                .addOnClickListener(R.id.item_choose);
        ImageView headerImage = helper.getView(R.id.headers);
        GlideUtil.loadCircleImg(headerImage, item.getHeadPortrait());


    }
}
