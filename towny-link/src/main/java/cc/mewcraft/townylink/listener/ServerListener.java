package cc.mewcraft.townylink.listener;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import cc.mewcraft.mewcore.util.UtilComponent;
import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.messager.Action;
import cc.mewcraft.townylink.messager.Messenger;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.List;

@Singleton
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

        this.plugin.getComponentLogger().info(
            UtilComponent.asComponent("<aqua>Sending town and nation names to other servers...")
        );

        List<String> townNames = TownyAPI.getInstance().getTowns().stream().map(Town::getName).toList();
        this.messenger.sendMessage(Action.ADD_TOWN, townNames);

        List<String> nationNames = TownyAPI.getInstance().getNations().stream().map(Nation::getName).toList();
        this.messenger.sendMessage(Action.ADD_NATION, nationNames);

        this.plugin.getComponentLogger().info(
            UtilComponent.asComponent("<aqua>Sending completed!")
        );

    }

}
