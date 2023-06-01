package cc.mewcraft.townyorigin.listener;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import cc.mewcraft.townyorigin.Constants;
import cc.mewcraft.townyorigin.Suppliers;
import cc.mewcraft.townyorigin.TownyOrigin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerListener implements AutoCloseableListener {

    private final TownyOrigin plugin;
    private final LuckPerms luckPerms;

    public PlayerListener(TownyOrigin plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        // We listen to the PlayerDeathEvent:
        // If the player is in his server-origin,
        // then don't drop his items (exp still drops)

        if (event.getKeepInventory() && event.getDrops().isEmpty()) {
            // The keepInventory is already set to true by other plugins:
            // We simply notify the player that he is blessed
            plugin.getLang().of("msg_inventory_kept_for_other_reasons").title(event.getPlayer());
            return;
        }

        String serverOriginId = luckPerms.getPlayerAdapter(Player.class).getMetaData(event.getPlayer()).getMetaValue(Constants.SERVER_ORIGIN_ID_KEY);
        if (serverOriginId == null) {
            plugin.getLang().of("msg_inventory_not_kept_for_none_origin").title(event.getPlayer());
            return; // The player doesn't have server-origin-id set
        }

        if (Suppliers.serverIdSupplier.get().map(o -> o.equals(serverOriginId)).orElse(false)) {
            // The player is in the server-origin:
            event.setKeepInventory(true);
            event.getDrops().clear();
            plugin.getLang().of("msg_inventory_kept_for_inside_origin").title(event.getPlayer());
        } else {
            plugin.getLang().of("msg_inventory_not_kept_for_outside_origin").title(event.getPlayer());
        }
    }

}
