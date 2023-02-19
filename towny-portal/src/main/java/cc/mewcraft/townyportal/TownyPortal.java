package cc.mewcraft.townyportal;

import cc.mewcraft.townyportal.mask.TownListMask;
import me.hsgamer.bettergui.lib.core.bukkit.addon.PluginAddon;
import me.hsgamer.bettergui.maskedgui.builder.MaskBuilder;

public class TownyPortal extends PluginAddon {

    @Override public void onEnable() {
        // TODO fix class not found
        MaskBuilder.INSTANCE.register(input -> new TownListMask(this, input), "town-list", "townlist", "towns");
    }

}
