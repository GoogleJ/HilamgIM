package io.hilamg.imservice.ui.im.rongIM;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DeleteClickActions;
import io.rong.imkit.actions.IClickActions;
import io.rong.imkit.fragment.ConversationFragment;

public class CusConversationFragment extends ConversationFragment {

    @Override
    public boolean showMoreClickItem() {
        return true;
    }

    @Override
    public List<IClickActions> getMoreClickActions() {
        ArrayList<IClickActions> actions = new ArrayList(1);
        actions.add(new DeleteClickActions());
        return actions;
    }

}
