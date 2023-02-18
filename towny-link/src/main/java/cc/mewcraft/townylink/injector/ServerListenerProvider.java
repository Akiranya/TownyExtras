package cc.mewcraft.townylink.injector;

import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.listener.ServerListener;
import cc.mewcraft.townylink.messager.Messenger;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ServerListenerProvider implements Provider<ServerListener> {

    private final TownyLink plugin;
    private final Messenger messenger;

    @Inject
    public ServerListenerProvider(final TownyLink plugin, final Messenger messenger) {
        this.plugin = plugin;
        this.messenger = messenger;
    }

    @Override public ServerListener get() {
        return new ServerListener(this.plugin, this.messenger);
    }

}
