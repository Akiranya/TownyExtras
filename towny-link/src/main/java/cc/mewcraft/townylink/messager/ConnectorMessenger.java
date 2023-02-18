package cc.mewcraft.townylink.messager;

import cc.mewcraft.townylink.TownyLink;
import cc.mewcraft.townylink.object.TownyRepository;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.connector.ConnectingPlugin;
import de.themoep.connectorplugin.connector.Message;
import de.themoep.connectorplugin.connector.MessageTarget;
import me.lucko.helper.Schedulers;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

@Singleton
@SuppressWarnings("UnstableApiUsage")
public class ConnectorMessenger implements Messenger, Terminable {

    private final TownyLink plugin;
    private final TownyRepository repository;
    private final BukkitConnectorPlugin connectorPlugin;
    private final ConnectingPlugin connectingPlugin = () -> "TownyLink";

    @Inject
    public ConnectorMessenger(
        final TownyLink plugin,
        final TownyRepository repository,
        final BukkitConnectorPlugin connectorPlugin
    ) {
        this.plugin = plugin;
        this.repository = repository;
        this.connectorPlugin = connectorPlugin;
        Schedulers.bukkit().runTask(plugin, this::registerHandlers); // Will run after "Done!"
    }

    /**
     * Handles incoming messages.
     */
    private void registerHandlers() {
        registerHandler(Action.ADD_TOWN, (player, message) -> {
            List<String> townNames = readNames(message.getData());
            this.repository.addAllTowns(townNames);
            reportReceived(Action.ADD_TOWN, townNames);
        });
        registerHandler(Action.DELETE_TOWN, (player, message) -> {
            List<String> townNames = readNames(message.getData());
            this.repository.removeAllTowns(townNames);
            reportReceived(Action.DELETE_TOWN, townNames);
        });
        registerHandler(Action.ADD_NATION, (player, message) -> {
            List<String> nationNames = readNames(message.getData());
            this.repository.addAllNations(nationNames);
            reportReceived(Action.ADD_NATION, nationNames);
        });
        registerHandler(Action.DELETE_NATION, (player, message) -> {
            List<String> nationNames = readNames(message.getData());
            this.repository.removeAllNations(nationNames);
            reportReceived(Action.DELETE_NATION, nationNames);
        });
    }

    @Override public void sendMessage(String action, List<String> names) {
        sendData(action, MessageTarget.OTHERS_QUEUE, writeNames(names));
        reportSent(action, names);
    }

    @Override public void sendMessage(String action, String... names) {
        sendMessage(action, Arrays.asList(names));
    }

    //// Convenient methods to send/receive data ////

    private void sendData(String action, MessageTarget target, byte[] data) {
        this.connectorPlugin.getConnector().sendData(this.connectingPlugin, action, target, data);
    }

    private void sendData(String action, MessageTarget target, String server, byte[] data) {
        this.connectorPlugin.getConnector().sendData(this.connectingPlugin, action, target, server, data);
    }

    private void registerHandler(String action, BiConsumer<Player, Message> handler) {
        this.connectorPlugin.getConnector().registerMessageHandler(this.connectingPlugin, action, handler);
    }

    //// Methods to read & write byte data ////

    private List<String> readNames(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        int size = in.readInt();
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            names.add(in.readUTF());
        return names;
    }

    private byte[] writeNames(List<String> names) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        int size = names.size();
        out.writeInt(size);
        names.forEach(out::writeUTF);
        return out.toByteArray();
    }

    private byte[] writeNames(String... names) {
        return writeNames(Arrays.asList(names));
    }

    //// Methods to report sent & received /////

    private void reportReceived(String action, List<String> names) {
        this.plugin.getLogger().info(
            "Received - %s : %s".formatted(action, names.stream().reduce((a, b) -> a + ", " + b).orElse(""))
        );
    }

    private void reportSent(String action, List<String> names) {
        this.plugin.getLogger().info(
            "Sent - %s : %s".formatted(action, names.stream().reduce((a, b) -> a + ", " + b).orElse(""))
        );
    }

    @Override public void close() {
        this.connectorPlugin.getConnector().unregisterMessageHandlers(this.connectingPlugin);
    }

}
