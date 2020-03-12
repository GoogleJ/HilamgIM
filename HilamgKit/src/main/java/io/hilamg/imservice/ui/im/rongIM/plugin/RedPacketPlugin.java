package io.hilamg.imservice.ui.im.rongIM.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ToastUtils;
import io.hilamg.imservice.R;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

public class RedPacketPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.icon_red_packet_btn);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.red_packet);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        ToastUtils.showShort(R.string.gotohilamg);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }

}
