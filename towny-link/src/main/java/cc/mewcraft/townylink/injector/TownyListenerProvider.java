package cc.mewcraft.townylink.injector;

import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.listener.TownyListener;
import cc.mewcraft.townylink.messager.Messenger;
import cc.mewcraft.townylink.object.TownyRepository;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TownyListenerProvider implements Provider<TownyListener> {

    private final TownyLink plugin;
    private final Messenger messenger;
    private final TownyRepository repository;

    @Inject
    public TownyListenerProvider(final TownyLink plugin, final Messenger messenger, final TownyRepository repository) {
        this.plugin = plugin;
        this.messenger = messenger;
        this.repository = repository;
    }

    @Override
    public TownyListener get() {
        return new TownyListener(this.plugin, this.messenger, this.repository);
    }

}
