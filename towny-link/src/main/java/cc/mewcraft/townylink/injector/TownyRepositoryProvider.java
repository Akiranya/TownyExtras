package cc.mewcraft.townylink.injector;

import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.object.TownyRepository;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TownyRepositoryProvider implements Provider<TownyRepository> {

    private final TownyLink plugin;

    @Inject
    public TownyRepositoryProvider(final TownyLink plugin) {
        this.plugin = plugin;
    }

    @Override public TownyRepository get() {
        return new TownyRepository(this.plugin);
    }

}
