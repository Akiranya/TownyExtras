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
import org.bukkit.entity.Player;
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

    private final Map<UUID, TownListMask.PlayerEntry> playerEntryMap = new ConcurrentHashMap<>();
    private final TownyPortal addon;
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

    private boolean canView(UUID uuid, TownListMask.PlayerEntry targetPlayerEntry) {
        if (!viewSelf && uuid.equals(targetPlayerEntry.uuid)) {
            return false;
        }
        if (!targetPlayerEntry.activated.get()) {
            return false;
        }
        return targetPlayerEntry.viewerCondition.test(uuid);
    }

    private TownListMask.PlayerEntry newPlayerEntry(UUID uuid) {
        Map<String, Object> replacedButtonSettings = replaceShortcut(templateButton, uuid);
        Button button = ButtonBuilder.INSTANCE.build(new ButtonBuilder.Input(getMenu(), getName() + "_player_" + uuid + "_button", replacedButtonSettings))
            .map(Button.class::cast)
            .orElse(Button.EMPTY);
        button.init();

        List<String> replacedViewerConditions = replaceShortcut(viewerConditionTemplate, uuid);
        ConditionRequirement viewerCondition = new ConditionRequirement(new RequirementBuilder.Input(getMenu(), "condition", getName() + "_player_" + uuid + "_condition", replacedViewerConditions));
        return new TownListMask.PlayerEntry(uuid, button, uuid1 -> viewerCondition.check(uuid1).isSuccess);
    }

    private List<Button> getPlayerButtons(UUID uuid) {
        return Bukkit.getOnlinePlayers()
            .stream()
            .map(Player::getUniqueId)
            .map(playerEntryMap::get)
            .filter(Objects::nonNull)
            .filter(entry -> canView(uuid, entry))
            .map(entry -> entry.button)
            .collect(Collectors.toList());
    }

    @Override
    protected ButtonPaginatedMask createPaginatedMask(Map<String, Object> section) {
        templateButton = Optional.ofNullable(MapUtil.getIfFound(section, "template", "button"))
            .flatMap(MapUtil::castOptionalStringObjectMap)
            .orElse(Collections.emptyMap());
        viewSelf = Optional.ofNullable(MapUtil.getIfFound(section, "view-self", "self"))
            .map(String::valueOf)
            .map(Boolean::parseBoolean)
            .orElse(true);
        playerCondition = Optional.ofNullable(MapUtil.getIfFound(section, "player-condition"))
            .map(o -> new ConditionRequirement(new RequirementBuilder.Input(getMenu(), "condition", getName() + "_player_condition", o)))
            .orElse(null);
        viewerConditionTemplate = Optional.ofNullable(MapUtil.getIfFound(section, "viewer-condition"))
            .map(CollectionUtils::createStringListFromObject)
            .orElse(Collections.emptyList());
        return new ButtonPaginatedMask(getName(), MultiSlotUtil.getSlots(section)) {
            @Override
            public @NotNull List<@NotNull Button> getButtons(@NotNull UUID uuid) {
                return getPlayerButtons(uuid);
            }
        };
    }

    @Override
    public void init() {
        super.init();
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(addon.getPlugin(), this, 0L, 20L);
    }

    @Override
    public void stop() {
        super.stop();
        if (updateTask != null) {
            updateTask.cancel();
        }
        playerEntryMap.values().forEach(playerEntry -> playerEntry.button.stop());
        playerEntryMap.clear();
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerEntryMap.compute(player.getUniqueId(), (currentId, currentEntry) -> {
                if (currentEntry == null) {
                    currentEntry = newPlayerEntry(currentId);
                }
                currentEntry.activated.lazySet(playerCondition == null || playerCondition.check(currentId).isSuccess);
                return currentEntry;
            });
        }
    }

    private static class PlayerEntry {
        final UUID uuid;
        final Button button;
        final Predicate<UUID> viewerCondition;
        final AtomicBoolean activated = new AtomicBoolean();

        private PlayerEntry(UUID uuid, Button button, Predicate<UUID> viewerCondition) {
            this.uuid = uuid;
            this.button = button;
            this.viewerCondition = viewerCondition;
        }
    }

}