package cc.mewcraft.townylink;

import cc.mewcraft.mewcore.message.Translations;
import cc.mewcraft.townylink.config.LinkConfig;
import cc.mewcraft.townylink.injector.ServerListenerProvider;
import cc.mewcraft.townylink.injector.TownyListenerProvider;
import cc.mewcraft.townylink.injector.TownyRepositoryProvider;
import cc.mewcraft.townylink.listener.ServerListener;
import cc.mewcraft.townylink.listener.TownyListener;
import cc.mewcraft.townylink.messager.ConnectorMessenger;
import cc.mewcraft.townylink.messager.DummyMessenger;
import cc.mewcraft.townylink.messager.Messenger;
import cc.mewcraft.townylink.object.TownyRepository;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.plugin.Plugin;

public class TownyLink extends ExtendedJavaPlugin {

    private LinkConfig config;
    private Translations translations;

    public LinkConfig getLinkConfig() {
        return this.config;
    }

    public Translations getLang() {
        return this.translations;
    }

    @Override protected void enable() {
        saveDefaultConfig();
        this.config = new LinkConfig(this);
        this.translations = new Translations(this);

        Injector injector = Guice.createInjector(new MainModule());

        registerListener(injector.getInstance(ServerListener.class)).bindWith(this);
        registerListener(injector.getInstance(TownyListener.class)).bindWith(this);
    }

    @Override protected void disable() {

    }

    private class MainModule extends AbstractModule {
        @Override protected void configure() {
            bind(TownyLink.class).toInstance(TownyLink.this);

            Plugin connectorPlugin = getServer().getPluginManager().getPlugin("ConnectorPlugin");
            if (connectorPlugin == null) {
                bind(Messenger.class).to(DummyMessenger.class).in(Scopes.SINGLETON);
            } else {
                bind(Messenger.class).to(ConnectorMessenger.class).in(Scopes.SINGLETON);
                bind(BukkitConnectorPlugin.class).toInstance((BukkitConnectorPlugin) connectorPlugin);
            }

            bind(TownyRepository.class).toProvider(TownyRepositoryProvider.class).in(Scopes.SINGLETON);
            bind(ServerListener.class).toProvider(ServerListenerProvider.class).in(Scopes.SINGLETON);
            bind(TownyListener.class).toProvider(TownyListenerProvider.class).in(Scopes.SINGLETON);
        }
    }

}
