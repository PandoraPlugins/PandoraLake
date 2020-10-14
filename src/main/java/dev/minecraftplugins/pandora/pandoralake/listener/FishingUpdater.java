package dev.minecraftplugins.pandora.pandoralake.listener;

import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class FishingUpdater implements Runnable {
    private final PandoraLake plugin;
    private final Map<OfflinePlayer, Integer> fishingMap;
    private final Set<OfflinePlayer> catchers;

    public FishingUpdater(PandoraLake plugin) {
        this.plugin = plugin;
        fishingMap = new HashMap<>();
        catchers = new HashSet<>();
    }

    @Override
    public void run() {
        fishingMap.forEach((offlinePlayer, integer) -> {
            if (!offlinePlayer.isOnline()) {
                fishingMap.remove(offlinePlayer);
                return;
            }
            integer--;
            fishingMap.put(offlinePlayer, integer);
            if (integer < 0) {
                if (integer < (plugin.getSettingsManager().getSettings().fishingTime * -1)) {
                    // remove them from the map, they're over the time limit
                    fishingMap.remove(offlinePlayer);
                    catchers.remove(offlinePlayer);
                    plugin.consumeMessage(offlinePlayer.getPlayer(), plugin.getSettingsManager().getSettings().fishTooLateMessage,
                            Collections.emptyMap());
                }
            } else if (integer == 0) {
                catchers.add(offlinePlayer);
                plugin.consumeMessage(offlinePlayer.getPlayer(), plugin.getSettingsManager().getSettings().readyToCatchMessage,
                        Collections.emptyMap());
            }
        });
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
