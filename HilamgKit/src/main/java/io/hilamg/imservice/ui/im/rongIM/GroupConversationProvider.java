package io.hilamg.imservice.ui.im.rongIM;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.hilamg.imservice.R;

import io.rong.imkit.model.ConversationProviderTag;
import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.provider.IContainerItemProvider;

@ConversationProviderTag(
        conversationType = "group"
)
public class GroupConversationProvider extends io.rong.imkit.widget.provider.GroupConversationProvider implements IContainerItemProvider.ConversationProvider<UIConversation> {

    private int colorSocialSign;
    private int colorText;

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        if (colorSocialSign == 0) {
            colorSocialSign = Color.parseColor("#EC7A00");
            colorText = context.getColor(R.color.rc_text_color_primary);
        }
        return super.newView(context, viewGroup);
    }

    @Override
    public void bindView(View view, int position, UIConversation data) {
        super.bindView(view, position, data);

        TextView title = view.findViewById(io.rong.imkit.R.id.rc_conversation_title);
        TextView tvSocialSign = view.findViewById(io.rong.imkit.R.id.tvSocialSign);

        if (data.getUIConversationTitle().contains("おれは人间をやめるぞ！ジョジョ―――ッ!")) {
            tvSocialSign.setVisibility(View.VISIBLE);
            title.setTextColor(colorSocialSign);
            title.setText(data.getUIConversationTitle().replace("おれは人间をやめるぞ！ジョジョ―――ッ!", ""));
        } else {
            tvSocialSign.setVisibility(View.GONE);
            title.setTextColor(colorText);
        }
    }
}
