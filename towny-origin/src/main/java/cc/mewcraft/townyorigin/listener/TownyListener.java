package cc.mewcraft.townyorigin.listener;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import cc.mewcraft.townyorigin.Constants;
import cc.mewcraft.townyorigin.Suppliers;
import cc.mewcraft.townyorigin.TownyOrigin;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.object.Resident;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.event.EventHandler;

import java.util.Objects;

public class TownyListener implements AutoCloseableListener {

    private final TownyOrigin plugin;
    private final LuckPerms luckPerms;

    public TownyListener(TownyOrigin plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();

        if (plugin.getServer().getPluginManager().getPlugin("Towny") == null) {
            plugin.getSLF4JLogger().warn("Towny is not installed on this server!");
        }
    }

    @EventHandler
    public void onJoinTown(TownAddResidentEvent event) {
        // We listen to the TownAddResidentEvent:
        // If it's the first time that the player joins a town,
        // we save the current server in his LuckPerms metadata.

        Suppliers.serverIdSupplier.get().ifPresent(serverId -> {
            Resident resident = Objects.requireNonNull(event.getResident());
            User user = luckPerms.getUserManager().getUser(resident.getUUID());
            if (user == null) {
                return; // User not found - usually because of fake player
            }

            boolean hasOrigin = user.getCachedData().getMetaData().getMetaValue(Constants.SERVER_ORIGIN_ID_KEY) != null;
            if (hasOrigin) {
                return; // The player already has server-origin set
            }

            Suppliers.serverOriginIdCreator.get().ifPresent(node -> user.data().add(node)); // Add meta data of server-origin-id
            Suppliers.serverOriginNameCreator.get().ifPresent(node -> user.data().add(node)); // Add meta data of server-origin-name

            luckPerms.getUserManager().saveUser(user);
        });
    }

}
