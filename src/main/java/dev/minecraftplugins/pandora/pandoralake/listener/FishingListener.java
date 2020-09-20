package dev.minecraftplugins.pandora.pandoralake.listener;

import com.azortis.azortislib.experimental.inventory.StackBuilder;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;
import dev.minecraftplugins.pandora.pandoralake.settings.rewards.Reward;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FishingListener implements Listener {
    private final PandoraLake plugin;
    private final List<Player> playerList = new ArrayList<>();

    public FishingListener(PandoraLake plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // We need to determine if they're fishing within a special designated region with the fishing flag allowed.
        // We check whether the hook is in the region so we can make sure they're not fishing from outside/inside a
        // different region.
        LocalPlayer player = WGBukkit.getPlugin().wrapPlayer(event.getPlayer());
        if (WGBukkit.getPlugin().getRegionContainer().createQuery().testState(event.getHook().getLocation(), player,
                plugin.getFishingFlag())) {
            // We have determined that they're able to fish and get custom rewards inside this region,
            switch (event.getState()) {
                case FISHING:
                    // They cast our their rod, we set the time required to bite to the correct time based on values.
                    // We find the minimal number of ticks required for a player to bite.
                    int catchTime = plugin.getSettingsManager().getSettings().fishingSpeed;
                    // We add to the base ticks a certain number within the range given.
                    catchTime += ThreadLocalRandom.current().nextInt(plugin.getSettingsManager().getSettings().fishingSpeedRange);
                    // We now set the time required for a bite.
                    setBiteTime(event.getHook(), catchTime);
                    // Now we add them to the list.
                    playerList.add(event.getPlayer());
                    plugin.consumeMessage(event.getPlayer(), plugin.getSettingsManager().getSettings().fishingMessage,
                            Collections.emptyMap());
                    break;
                case CAUGHT_FISH:
                    // Player has successfully caught the fish.
                    Reward reward = plugin.getRewardsManager().getRandomReward();
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("{reward}", reward.item.name);
                    plugin.consumeMessage(event.getPlayer(), reward.message, placeholders);
                    if (event.getCaught() instanceof Item) {
                        Item item = (Item) event.getCaught();
                        if (reward.item.shouldGive) {
                            ItemStack itemStack = StackBuilder.start(Material.getMaterial(reward.item.id))
                                    .lore(reward.item.lore).amount(reward.item.amount)
                                    .data((short) reward.item.data).name(reward.item.name)
                                    .build();
                            item.setItemStack(itemStack);
                        } else {
                            item.setItemStack(null);
                        }
                    }
                    if (reward.commands.length > 0) {
                        for (String command : reward.commands) {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "/" + command);
                        }
                    }
                    event.setExpToDrop(reward.xp);
                    break;
                default:
                    break;
            }


        } else {
            // If they try fishing like normal outside of special zone, we cancel it.
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                event.setCancelled(true);
                // We cancelled their event if they try fishing.
                // Now we send them configured messages from their overlords about fishing.
                Message message = plugin.getSettingsManager().getSettings().noFishingMessage;
                plugin.consumeMessage(event.getPlayer(), message, Collections.emptyMap());
            }
        }
    }


    // Credit to https://www.spigotmc.org/threads/how-do-i-make-it-so-you-catch-fish-faster.133418/#post-1470619
    private void setBiteTime(FishHook hook, int time) {
        net.minecraft.server.v1_8_R3.EntityFishingHook hookCopy = (EntityFishingHook) ((CraftEntity) hook).getHandle();

        Field fishCatchTime = null;

        try {
            fishCatchTime = net.minecraft.server.v1_8_R3.EntityFishingHook.class.getDeclaredField("aw");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        assert fishCatchTime != null;
        fishCatchTime.setAccessible(true);

        try {
            fishCatchTime.setInt(hookCopy, time);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        fishCatchTime.setAccessible(false);
    }


}