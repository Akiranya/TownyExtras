package cc.mewcraft.townyorigin;

import cc.mewcraft.mewcore.listener.AutoCloseableListener;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.object.Resident;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.MetaNode;
import net.luckperms.api.util.Tristate;
import org.bukkit.event.EventHandler;

import java.util.Objects;
import java.util.Optional;

public class TownyOrigin extends ExtendedJavaPlugin implements AutoCloseableListener {

    private LuckPerms luckPerms;

    @Override protected void enable() {
        luckPerms = LuckPermsProvider.get();

        registerListener(this).bindWith(this);
    }

    @Override protected void disable() {

    }

    @EventHandler
    public void onJoinTown(TownAddResidentEvent event) {
        // We listen to the TownAddResidentEvent:
        // If it's the first time that the player joins a town,
        // we save the current server in his LuckPerms metadata.

        // Get the current server in which the player is.
        // The static context "server-type" should be configured
        // in the `[LuckPerms Data Folder]/contexts.json` file.
        Optional<String> serverType = luckPerms.getContextManager().getStaticContext().getAnyValue("server-type");
        if (serverType.isEmpty()) return;

        Resident resident = Objects.requireNonNull(event.getResident());
        User user = luckPerms.getUserManager().getUser(resident.getUUID());
        if (user == null) return;

        MetaNode node = MetaNode.builder("server-origin", serverType.get()).build();
        Tristate contains = user.data().contains(node, (o1, o2) -> o1.getKey().equals(o2.getKey()));

        // The player already has an origin - Don't update it
        if (contains == Tristate.TRUE) {
            getSLF4JLogger().info("Origin server already exists: {} -> {}", user.getUsername(), serverType.get());
            return;
        }

        user.data().add(node);
        luckPerms.getUserManager().saveUser(user).whenCompleteAsync((v, ex) -> getSLF4JLogger().info(
            "Saved origin server: {} -> {}", user.getUsername(), serverType.get()
        ));
    }

}
