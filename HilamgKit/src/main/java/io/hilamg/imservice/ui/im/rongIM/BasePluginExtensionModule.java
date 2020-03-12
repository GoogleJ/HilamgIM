package io.hilamg.imservice.ui.im.rongIM;

import io.hilamg.imservice.ui.im.rongIM.plugin.FilePlugin;
import io.hilamg.imservice.ui.im.rongIM.plugin.PhotoSelectorPlugin;
import io.hilamg.imservice.ui.im.rongIM.plugin.RedPacketPlugin;
import io.hilamg.imservice.ui.im.rongIM.plugin.TakePhotoPlugin;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongExtension;
import io.rong.imkit.emoticon.IEmoticonTab;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;

public class BasePluginExtensionModule extends DefaultExtensionModule {
    private String targetId;
    private Conversation.ConversationType conversationType;

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {

        List<IPluginModule> list = super.getPluginModules(conversationType);

        if (list != null) {
            list.clear();
            list.add(new PhotoSelectorPlugin());
            list.add(new TakePhotoPlugin());
            list.add(new RedPacketPlugin());
            list.add(new FilePlugin());
        }

        return list;
    }

    @Override
    public List<IEmoticonTab> getEmoticonTabs() {
        List<IEmoticonTab> tabs = super.getEmoticonTabs();

        CusEmoteTab cusEmoteTab = new CusEmoteTab(targetId, conversationType);
        CusEmoteTab2 cusEmoteTab2 = new CusEmoteTab2(targetId, conversationType);

        tabs.add(cusEmoteTab);
        tabs.add(cusEmoteTab2);
        return tabs;
    }

    @Override
    public void onAttachedToExtension(RongExtension extension) {
        super.onAttachedToExtension(extension);
        targetId = extension.getTargetId();
        conversationType = extension.getConversationType();
    }
}
