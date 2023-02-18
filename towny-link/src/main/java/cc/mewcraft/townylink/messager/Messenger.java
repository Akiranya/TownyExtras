package cc.mewcraft.townylink.messager;

import java.util.List;

public interface Messenger {
    /**
     * Sends names of Towny objects to other servers.
     *
     * @param action an {@link Action}
     * @param names  names to sync
     */
    void sendMessage(String action, List<String> names);

    /**
     * The same as {@link #sendMessage(String, List)}.
     */
    void sendMessage(String action, String... names);
}
