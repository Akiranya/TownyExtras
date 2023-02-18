package cc.mewcraft.townylink.listener;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.messager.Action;
import cc.mewcraft.townylink.messager.Messenger;
import cc.mewcraft.townylink.object.TownyRepository;
import com.google.inject.Inject;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.nation.PreNewNationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class TownyListener implements AutoCloseableListener {

    private final TownyLink plugin;
    private final Messenger messenger;
    private final TownyRepository repository;

    @Inject
    public TownyListener(
        final TownyLink plugin,
        final Messenger messenger,
        final TownyRepository repository
    ) {
        this.plugin = plugin;
        this.messenger = messenger;
        this.repository = repository;
    }

    //// Prevent duplicate towns/nations ////

    @EventHandler
    public void onPreNewTown(PreNewTownEvent event) {
        Player player = event.getPlayer();
        String townName = event.getTownName();
        if (this.repository.hasTown(townName)) {
            this.plugin.getLogger().info("Cancelled duplicate town creation: " + townName);
            event.setCancelled(true);
            event.setCancelMessage(this.plugin.getLang().of("town_name_already_exists")
                .replace("town", townName)
                .locale(player)
                .plain()
            );
        }
    }

    @EventHandler
    public void onPreNewNation(PreNewNationEvent event) {
        Player player = event.getTown().getMayor().getPlayer();
        String nationName = event.getNationName();
        if (this.repository.hasNation(nationName)) {
            this.plugin.getLogger().info("Cancelled duplicate nation creation: " + nationName);
            event.setCancelled(true);
            event.setCancelMessage(this.plugin.getLang().of("nation_name_already_exists")
                .replace("nation", nationName)
                .locale(player)
                .plain()
            );
        }
    }

    //// Sync town/nation names to other servers ////

    @EventHandler
    public void onNewTown(NewTownEvent event) {
        String townName = event.getTown().getName();
        this.messenger.sendMessage(Action.ADD_TOWN, townName);
    }

    @EventHandler
    public void onNewNation(NewNationEvent event) {
        String nationName = event.getNation().getName();
        this.messenger.sendMessage(Action.ADD_NATION, nationName);
    }

    @EventHandler
    public void onDeleteTown(DeleteTownEvent event) {
        String townName = event.getTownName();
        this.messenger.sendMessage(Action.DELETE_TOWN, townName);
    }

    @EventHandler
    public void onDeleteNation(DeleteNationEvent event) {
        String nationName = event.getNationName();
        this.messenger.sendMessage(Action.DELETE_NATION, nationName);
    }

}
