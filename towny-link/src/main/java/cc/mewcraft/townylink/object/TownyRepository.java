package cc.mewcraft.townylink.object;

import cc.mewcraft.townylink.TownyLink;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Singleton
public class TownyRepository {

    private final TownyLink plugin;
    private final Set<String> townNames;
    private final Set<String> nationNames;

    @Inject
    public TownyRepository(final TownyLink plugin) {
        this.plugin = plugin;
        this.townNames = new HashSet<>();
        this.nationNames = new HashSet<>();
    }

    public void addTown(final String name) {
        if (!this.townNames.add(name.toLowerCase(Locale.ROOT))) {
            this.plugin.getLogger().warning("Trying to add a duplicate town name (%s) to repository".formatted(name));
        }
    }

    public void addAllTowns(final List<String> townNames) {
        for (final String townName : townNames) addTown(townName);
    }

    public void removeTown(final String name) {
        this.townNames.remove(name.toLowerCase(Locale.ROOT));
    }

    public void removeAllTowns(final List<String> townNames) {
        for (final String townName : townNames) removeTown(townName);
    }

    public void addNation(final String name) {
        if (!this.nationNames.add(name.toLowerCase(Locale.ROOT))) {
            this.plugin.getLogger().warning("Trying to add a duplicate nation name (%s) to repository".formatted(name));
        }
    }

    public void addAllNations(final List<String> nationNames) {
        for (final String nationName : nationNames) addNation(nationName);
    }

    public void removeNation(final String name) {
        this.nationNames.remove(name.toLowerCase(Locale.ROOT));
    }

    public void removeAllNations(final List<String> nationNames) {
        for (final String nationName : nationNames) removeNation(nationName);
    }

    public boolean hasTown(final String name) {
        return this.townNames.contains(name.toLowerCase(Locale.ROOT));
    }

    public boolean hasNation(final String name) {
        return this.nationNames.contains(name.toLowerCase(Locale.ROOT));
    }

}
