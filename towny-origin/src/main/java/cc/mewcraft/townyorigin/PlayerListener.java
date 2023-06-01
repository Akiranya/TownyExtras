package cc.mewcraft.townyorigin;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.object.Resident;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class PlayerListener implements AutoCloseableListener {

    private final TownyOrigin plugin;
    private final LuckPerms luckPerms;

    /**
     * Get the current "server-id" of server in which the player is.
     * <p>
     * The static context "server-id" should be configured in the `[luckperms-data-folder]/contexts.json` file.
     */
    private final Supplier<Optional<String>> serverIdSupplier;
    /**
     * Get the current "server-name" of server in which the player is.
     * <p>
     * The static context "server-name" should be configured in the `[luckperms-data-folder]/contexts.json` file.
     */
    private final Supplier<Optional<String>> serverNameSupplier;
    /**
     * Create a meta node where key is {@link Constants#SERVER_ORIGIN_ID_KEY} and value is current server-id.
     */
    private final Supplier<Optional<MetaNode>> serverOriginIdCreator;
    /**
     * Create a meta node where key is {@link Constants#SERVER_ORIGIN_NAME_KEY} and value is current server-name.
     */
    private final Supplier<Optional<MetaNode>> serverOriginNameCreator;

    public PlayerListener(TownyOrigin plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();

        this.serverIdSupplier = () -> luckPerms.getContextManager().getStaticContext().getAnyValue(Constants.SERVER_ID_KEY);
        this.serverNameSupplier = () -> luckPerms.getContextManager().getStaticContext().getAnyValue(Constants.SERVER_NAME_KEY);
        this.serverOriginIdCreator = () -> serverIdSupplier.get().map(id -> MetaNode.builder(Constants.SERVER_ORIGIN_ID_KEY, id).build());
        this.serverOriginNameCreator = () -> serverNameSupplier.get().map(id -> MetaNode.builder(Constants.SERVER_ORIGIN_NAME_KEY, id).build());

        if (plugin.getServer().getPluginManager().getPlugin("Towny") == null) {
            plugin.getSLF4JLogger().warn("Towny is not installed on this server!");
        }
    }

    @EventHandler
    public void onJoinTown(TownAddResidentEvent event) {
        // We listen to the TownAddResidentEvent:
        // If it's the first time that the player joins a town,
        // we save the current server in his LuckPerms metadata.

        serverIdSupplier.get().ifPresent(serverId -> {
            Resident resident = Objects.requireNonNull(event.getResident());
            User user = luckPerms.getUserManager().getUser(resident.getUUID());
            if (user == null) {
                return; // User not found - usually because of fake player
            }

            boolean hasOrigin = user.getCachedData().getMetaData().getMetaValue(Constants.SERVER_ORIGIN_ID_KEY) != null;
            if (hasOrigin) {
                return; // The player already has server-origin set
            }

            serverOriginIdCreator.get().ifPresent(node -> user.data().add(node)); // Add meta data of server-origin-id
            serverOriginNameCreator.get().ifPresent(node -> user.data().add(node)); // Add meta data of server-origin-name

            luckPerms.getUserManager().saveUser(user);
        });
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        // We listen to the PlayerDeathEvent:
        // If the player is in his server-origin,
        // then don't drop his items (exp still drops)

        String serverOriginId = luckPerms.getPlayerAdapter(Player.class).getMetaData(event.getPlayer()).getMetaValue(Constants.SERVER_ORIGIN_ID_KEY);
        if (serverOriginId == null) {
            plugin.getLang().of("msg_inventory_not_kept_for_none_origin").title(event.getPlayer());
            return; // The player doesn't have server-origin-id set
        }

        if (serverIdSupplier.get().map(o -> o.equals(serverOriginId)).orElse(false)) {
            // The player is in the server-origin:
            event.setKeepInventory(true);
            event.getDrops().clear();
            plugin.getLang().of("msg_inventory_kept_for_inside_origin").title(event.getPlayer());
        } else {
            plugin.getLang().of("msg_inventory_not_kept_for_outside_origin").title(event.getPlayer());
        }
    }

}
