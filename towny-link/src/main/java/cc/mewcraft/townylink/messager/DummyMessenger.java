package cc.mewcraft.townylink.messager;

import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class DummyMessenger implements Messenger {

    @Override public void sendMessage(final String action, final List<String> names) {}

    @Override public void sendMessage(final String action, final String... names) {}

}
