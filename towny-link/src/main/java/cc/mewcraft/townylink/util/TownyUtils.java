package cc.mewcraft.townylink.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;

import java.util.List;

public final class TownyUtils {

    public static List<String> getAllTowns() {
        return TownyAPI.getInstance().getTowns().stream().map(Town::getName).toList();
    }

    public static List<String> getALlNations() {
        return TownyAPI.getInstance().getNations().stream().map(Nation::getName).toList();
    }

    public static boolean hasTown(String name) {
        return TownyAPI.getInstance().getTown(name) != null;
    }

    public static boolean hasNation(String name) {
        return TownyAPI.getInstance().getNation(name) != null;
    }

    private TownyUtils() {}

}
