package dev.minecraftplugins.pandora.pandoralake.listener;

import com.sk89q.worldguard.bukkit.WGBukkit;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class FishingUpdater implements Runnable {
    private final PandoraLake plugin;
    private final Map<OfflinePlayer, Integer> fishingMap;
    private final Set<OfflinePlayer> catchers;
    private final Map<FishHook, Integer> fishingHookTimer;

    public FishingUpdater(PandoraLake plugin) {
        this.plugin = plugin;
        fishingMap = new ConcurrentHashMap<>();
        catchers = new ConcurrentSet<>();
        fishingHookTimer = new ConcurrentHashMap<>();

    }

    @Override
    public void run() {
        for (Map.Entry<OfflinePlayer, Integer> offlinePlayerIntegerEntry : fishingMap.entrySet()) {
            if (!offlinePlayerIntegerEntry.getKey().isOnline()) {
                fishingMap.remove(offlinePlayerIntegerEntry.getKey());
                return;
            }
            int integer = offlinePlayerIntegerEntry.getValue() - 1;
            fishingMap.put(offlinePlayerIntegerEntry.getKey(), integer);
            if (integer < 0) {
                if (integer < (plugin.getSettingsManager().getSettings().fishingTime * -1)) {
                    // remove them from the map, they're over the time limit
                    fishingMap.remove(offlinePlayerIntegerEntry.getKey());
                    catchers.remove(offlinePlayerIntegerEntry.getKey());
                    plugin.consumeMessage(offlinePlayerIntegerEntry.getKey().getPlayer(), plugin.getSettingsManager().getSettings().fishTooLateMessage,
                            Collections.emptyMap());
                }
            } else if (integer == 0) {
                catchers.add(offlinePlayerIntegerEntry.getKey());
                plugin.consumeMessage(offlinePlayerIntegerEntry.getKey().getPlayer(), plugin.getSettingsManager().getSettings().readyToCatchMessage,
                        Collections.emptyMap());
            }
        }
        for (Map.Entry<FishHook, Integer> fishHookIntegerEntry : fishingHookTimer.entrySet()) {
            int integer1 = fishHookIntegerEntry.getValue() - 1;
            FishHook hook = fishHookIntegerEntry.getKey();
            Block b = hook.getLocation().getBlock();
            boolean add = true;
            if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
                if (WGBukkit.getPlugin().getRegionContainer().createQuery().testState(hook.getLocation(), (Player) null,
                        plugin.getFishingFlag())) {
                    int catchTime = plugin.getSettingsManager().getSettings().fishingSpeed;
                    // We add to the base ticks a certain number within the range given.
                    if (plugin.getSettingsManager().getSettings().fishingSpeedRange > 0)
                        catchTime += ThreadLocalRandom.current().nextInt(plugin.getSettingsManager().getSettings().fishingSpeedRange);
                    this.addPlayer((Player) hook.getShooter(), catchTime);
                    // todo: add in placeholders
                    plugin.consumeMessage((Player) hook.getShooter(),
                            plugin.getSettingsManager().getSettings().fishingMessage,
                            Collections.emptyMap());
                    fishingHookTimer.remove(hook);
                    add = false;
                }
            }
            if (add & integer1 <= 0) {
                fishingHookTimer.remove(hook);
            } else if (add) {
                fishingHookTimer.put(hook, integer1);
            }
        }
    }

    public void addHook(FishHook entity, int timeout) {
        fishingHookTimer.put(entity, timeout);
    }

    public void removeHook(FishHook entity) {
        fishingHookTimer.remove(entity);
    }

    public boolean tryCatch(Player player) {
        boolean contains = catchers.contains(player);

        // we remove them from the fishingmap because they failed to catch it and give them failed to catch msg.
        if (!contains && fishingMap.containsKey(player)) {
            removePlayer(player);
            return false;
        }

        if (contains) {
            removePlayer(player);
            return true;
        }
        return false;
    }

    public void addPlayer(Player player, int time) {
        fishingMap.put(player, time);
    }

    public void removePlayer(Player player) {
        catchers.remove(player);
        fishingMap.remove(player);
    }
}
