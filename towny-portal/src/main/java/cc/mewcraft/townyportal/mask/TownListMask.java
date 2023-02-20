/*
   Copyright 2023-2023 Huynh Tien

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package cc.mewcraft.townyportal.mask;


import cc.mewcraft.townyportal.TownyPortal;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import me.hsgamer.bettergui.builder.ButtonBuilder;
import me.hsgamer.bettergui.builder.RequirementBuilder;
import me.hsgamer.bettergui.lib.core.common.CollectionUtils;
import me.hsgamer.bettergui.lib.core.minecraft.gui.button.Button;
import me.hsgamer.bettergui.lib.core.minecraft.gui.mask.impl.ButtonPaginatedMask;
import me.hsgamer.bettergui.lib.core.variable.VariableManager;
import me.hsgamer.bettergui.maskedgui.builder.MaskBuilder;
import me.hsgamer.bettergui.maskedgui.mask.WrappedPaginatedMask;
import me.hsgamer.bettergui.maskedgui.util.MultiSlotUtil;
import me.hsgamer.bettergui.requirement.type.ConditionRequirement;
import me.hsgamer.bettergui.util.MapUtil;
import me.hsgamer.bettergui.util.StringReplacerApplier;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TownListMask extends WrappedPaginatedMask<ButtonPaginatedMask> implements Runnable {

    private static final Pattern pattern = Pattern.compile("\\{current_player(_([^{}]+))?}");

    static {
        VariableManager.register("current_", (original, uuid) -> {
            String[] split = original.split(";", 3);
            if (split.length < 2) {
                return null;
            }
            UUID targetId;
            try {
                targetId = UUID.fromString(split[0]);
            } catch (IllegalArgumentException e) {
                return null;
            }
            String variable = split[1];
            boolean isPAPI = split.length == 3 && Boolean.parseBoolean(split[2]);
            String finalVariable;
            if (isPAPI) {
                finalVariable = "%" + variable + "%";
            } else {
                finalVariable = "{" + variable + "}";
            }
            return StringReplacerApplier.replace(finalVariable, targetId, true);
        });
    }

    private final TownyPortal addon;
    private final Map<UUID, TownEntry> mayorEntryMap = new ConcurrentHashMap<>();
    private Map<String, Object> templateButton = Collections.emptyMap();
    private ConditionRequirement playerCondition;
    private List<String> viewerConditionTemplate = Collections.emptyList();
    private BukkitTask updateTask;
    private boolean viewSelf = true;

    public TownListMask(TownyPortal addon, MaskBuilder.Input input) {
        super(input);
        this.addon = addon;
    }

    private static String replaceShortcut(String string, UUID targetId) {
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String variable = matcher.group(2);
            String replacement;
            if (variable == null) {
                replacement = "{current_" + targetId.toString() + ";player}";
            } else {
                boolean isPAPI = variable.startsWith("papi_");
                if (isPAPI) {
                    variable = variable.substring(5);
                }
                replacement = "{current_" + targetId.toString() + ";" + variable + ";" + isPAPI + "}";
            }
            string = string.replace(matcher.group(), replacement);
        }
        return string;
    }

    private static Object replaceShortcut(Object obj, UUID targetId) {
        if (obj instanceof String) {
            return replaceShortcut((String) obj, targetId);
        } else if (obj instanceof Collection) {
            List<Object> replaceList = new ArrayList<>();
            ((Collection<?>) obj).forEach(o -> replaceList.add(replaceShortcut(o, targetId)));
            return replaceList;
        } else if (obj instanceof Map) {
            // noinspection unchecked, rawtypes
            ((Map) obj).replaceAll((k, v) -> replaceShortcut(v, targetId));
        }
        return obj;
    }

    private static Map<String, Object> replaceShortcut(Map<String, Object> map, UUID targetId) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        map.forEach((k, v) -> newMap.put(k, replaceShortcut(v, targetId)));
        return newMap;
    }

    private static List<String> replaceShortcut(List<String> list, UUID targetId) {
        List<String> newList = new ArrayList<>();
        list.forEach(s -> newList.add(replaceShortcut(s, targetId)));
        return newList;
    }

    private boolean canView(UUID viewer, TownEntry targetTownEntry) {
        if (!this.viewSelf && viewer.equals(targetTownEntry.uuid)) {
            return false;
        }
        if (!targetTownEntry.activated.get()) {
            return false;
        }
        return targetTownEntry.viewerCondition.test(viewer);
    }

    private TownEntry newTownEntry(UUID mayor) {
        Map<String, Object> replacedButtonSettings = replaceShortcut(this.templateButton, mayor);
        Button button = ButtonBuilder.INSTANCE.build(new ButtonBuilder.Input(getMenu(), getName() + "_player_" + mayor + "_button", replacedButtonSettings))
            .map(Button.class::cast)
            .orElse(Button.EMPTY);
        button.init();

        List<String> replacedViewerConditions = replaceShortcut(this.viewerConditionTemplate, mayor);
        ConditionRequirement viewerCondition = new ConditionRequirement(new RequirementBuilder.Input(getMenu(), "condition", getName() + "_player_" + mayor + "_condition", replacedViewerConditions));
        return new TownEntry(mayor, button, viewer -> viewerCondition.check(viewer).isSuccess);
    }

    private List<Button> getTownButtons(UUID viewer) {
        return TownyAPI.getInstance().getTowns()
            .stream()
            .map(Town::getMayor)
            .map(Resident::getUUID)
            .map(this.mayorEntryMap::get)
            .filter(Objects::nonNull)
            .filter(entry -> canView(viewer, entry))
            .map(entry -> entry.button)
            .collect(Collectors.toList());
    }

    @Override
    protected ButtonPaginatedMask createPaginatedMask(Map<String, Object> section) {
        this.templateButton = Optional.ofNullable(MapUtil.getIfFound(section, "template", "button"))
            .flatMap(MapUtil::castOptionalStringObjectMap)
            .orElse(Collections.emptyMap());
        this.viewSelf = Optional.ofNullable(MapUtil.getIfFound(section, "view-self", "self"))
            .map(String::valueOf)
            .map(Boolean::parseBoolean)
            .orElse(true);
        this.playerCondition = Optional.ofNullable(MapUtil.getIfFound(section, "player-condition"))
            .map(o -> new ConditionRequirement(new RequirementBuilder.Input(getMenu(), "condition", getName() + "_player_condition", o)))
            .orElse(null);
        this.viewerConditionTemplate = Optional.ofNullable(MapUtil.getIfFound(section, "viewer-condition"))
            .map(CollectionUtils::createStringListFromObject)
            .orElse(Collections.emptyList());
        return new ButtonPaginatedMask(getName(), MultiSlotUtil.getSlots(section)) {
            @Override
            public @NotNull List<@NotNull Button> getButtons(@NotNull UUID uuid) {
                return getTownButtons(uuid);
            }
        };
    }

    @Override
    public void init() {
        super.init();
        this.updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.addon.getPlugin(), this, 0L, 100L);
    }

    @Override
    public void stop() {
        super.stop();
        if (this.updateTask != null) {
            this.updateTask.cancel();
        }
        this.mayorEntryMap.values().forEach(mayorEntry -> mayorEntry.button.stop());
        this.mayorEntryMap.clear();
    }

    @Override
    public void run() {
        TownyAPI.getInstance().getTowns()
            .stream()
            .map(Town::getMayor)
            .map(Resident::getUUID)
            .forEach(mayor -> this.mayorEntryMap.compute(mayor, (currentId, currentEntry) -> {
                if (currentEntry == null) {
                    currentEntry = newTownEntry(currentId);
                }
                currentEntry.activated.lazySet(this.playerCondition == null || this.playerCondition.check(currentId).isSuccess);
                return currentEntry;
            }));
    }

    private static class TownEntry {
        final UUID uuid; // mayor's uuid
        final Button button;
        final Predicate<UUID> viewerCondition;
        final AtomicBoolean activated = new AtomicBoolean();

        private TownEntry(UUID uuid, Button button, Predicate<UUID> viewerCondition) {
            this.uuid = uuid;
            this.button = button;
            this.viewerCondition = viewerCondition;
        }
    }

}