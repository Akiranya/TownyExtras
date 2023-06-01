package cc.mewcraft.townyorigin;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.types.MetaNode;

import java.util.Optional;
import java.util.function.Supplier;

public final class Suppliers {

    /**
     * Get the current "server-id" of server in which the player is.
     * <p>
     * The static context "server-id" should be configured in the `[luckperms-data-folder]/contexts.json` file.
     */
    public static final Supplier<Optional<String>> serverIdSupplier;
    /**
     * Get the current "server-name" of server in which the player is.
     * <p>
     * The static context "server-name" should be configured in the `[luckperms-data-folder]/contexts.json` file.
     */
    public static final Supplier<Optional<String>> serverNameSupplier;
    /**
     * Create a meta node where key is {@link Constants#SERVER_ORIGIN_ID_KEY} and value is current server-id.
     */
    public static final Supplier<Optional<MetaNode>> serverOriginIdCreator;
    /**
     * Create a meta node where key is {@link Constants#SERVER_ORIGIN_NAME_KEY} and value is current server-name.
     */
    public static final Supplier<Optional<MetaNode>> serverOriginNameCreator;

    static {
        final LuckPerms luckPerms = LuckPermsProvider.get();
        serverIdSupplier = () -> luckPerms.getContextManager().getStaticContext().getAnyValue(Constants.SERVER_ID_KEY);
        serverNameSupplier = () -> luckPerms.getContextManager().getStaticContext().getAnyValue(Constants.SERVER_NAME_KEY);
        serverOriginIdCreator = () -> serverIdSupplier.get().map(id -> MetaNode.builder(Constants.SERVER_ORIGIN_ID_KEY, id).build());
        serverOriginNameCreator = () -> serverNameSupplier.get().map(id -> MetaNode.builder(Constants.SERVER_ORIGIN_NAME_KEY, id).build());
    }

}
