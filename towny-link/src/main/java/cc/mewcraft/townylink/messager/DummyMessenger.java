package cc.mewcraft.townylink.messager;

import java.util.List;

public class DummyMessenger implements Messenger {

    @Override public void fetch() {}

    @Override public void sendMessage(final String action, final List<String> names) {}

    @Override public void sendMessage(final String action, final String... names) {}

}
