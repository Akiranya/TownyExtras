package cc.mewcraft.townylink.listener;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.messager.Action;
import cc.mewcraft.townylink.messager.Messenger;
import cc.mewcraft.townylink.util.TownyUtils;
import com.google.inject.Inject;
import me.lucko.helper.Schedulers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.concurrent.TimeUnit;

public class ServerListener implements AutoCloseableListener {

    private final TownyLink plugin;
    private final Messenger messenger;

    @Inject
    public ServerListener(final TownyLink plugin, final Messenger messenger) {
        this.plugin = plugin;
        this.messenger = messenger;
    }

    @EventHandler
    public void onServerStart(ServerLoadEvent event) {

        if (event.getType() != ServerLoadEvent.LoadType.STARTUP)
            return;

        this.plugin.getComponentLogger().info(
            UtilComponent.asComponent("<aqua>Sending town and nation names to other servers...")
        );

        Schedulers.async().runLater(() -> {
            this.messenger.fetch();
            this.messenger.sendMessage(Action.ADD_TOWN, TownyUtils.getAllTowns());
            this.messenger.sendMessage(Action.ADD_NATION, TownyUtils.getALlNations());
        }, 5, TimeUnit.SECONDS);

        this.plugin.getComponentLogger().info(
            UtilComponent.asComponent("<aqua>Sending completed!")
        );

    }

}
