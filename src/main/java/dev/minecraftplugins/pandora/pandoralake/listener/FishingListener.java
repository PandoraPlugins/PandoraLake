package dev.minecraftplugins.pandora.pandoralake.listener;

import com.azortis.azortislib.inventory.item.ItemBuilder;
import com.sk89q.worldguard.bukkit.WGBukkit;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import dev.minecraftplugins.pandora.pandoralake.glowing.Glow;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;
import dev.minecraftplugins.pandora.pandoralake.settings.rewards.Reward;
import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.EntityFishingHook;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FishingListener implements Listener {
    private final PandoraLake plugin;
    private final FishingUpdater updater;

    public FishingListener(PandoraLake plugin) {
        this.plugin = plugin;
        updater = new FishingUpdater(plugin);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, updater, 0, 20);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // We need to determine if they're fishing within a special designated region with the fishing flag allowed.
        // We check whether the hook is in the region so we can make sure they're not fishing from outside/inside a
        // different regions

        if (WGBukkit.getPlugin().getRegionContainer().createQuery().testState(event.getHook().getLocation(), (Player) null,
                plugin.getFishingFlag())) {
            // We have determined that they're able to fish and get custom rewards inside this region,

            switch (event.getState()) {
                case FISHING:
                    // They cast our their rod, we set the time required to bite to the correct time based on values.
                    // We find the minimal number of ticks required for a player to bite.
                    int catchTime = plugin.getSettingsManager().getSettings().fishingSpeed;
                    // We add to the base ticks a certain number within the range given.
                    if (plugin.getSettingsManager().getSettings().fishingSpeedRange > 0)
                        catchTime += ThreadLocalRandom.current().nextInt(plugin.getSettingsManager().getSettings().fishingSpeedRange);
                    // We now set the bobber to catch after 1 second, usually impossible, but it is to throw off autofishers.
                    setBiteTime(event.getHook(), 20);
                    // Now we add them to the updater to make sure they're kept track of.
                    updater.addPlayer(event.getPlayer(), catchTime);
                    plugin.consumeMessage(event.getPlayer(), plugin.getSettingsManager().getSettings().fishingMessage,
                            Collections.emptyMap());
                    break;
                case CAUGHT_FISH:
                    // Player has successfully caught the bobber
                    event.setCancelled(true);
                    event.getHook().remove();
                    event.getCaught().remove();

                    updater.removePlayer(event.getPlayer());
                    plugin.consumeMessage(event.getPlayer(), plugin.getSettingsManager().getSettings().catchBobberMessage,
                            Collections.emptyMap());

                    break;
                case FAILED_ATTEMPT:
                    // We try to catch an item
                    if (updater.tryCatch(event.getPlayer())) {
                        // This means we successfully caught an item.
                        Reward reward = plugin.getRewardsManager().getRandomReward();
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("{reward}", reward.item.name);
                        plugin.consumeMessage(event.getPlayer(), reward.message, placeholders);
                        if (event.getCaught() instanceof Item) {
                            Item item = (Item) event.getCaught();
                            if (reward.item.shouldGive) {
                                ItemStack itemStack = ItemBuilder.start(Material.getMaterial(reward.item.id))
                                        .lore(reward.item.lore).amount(reward.item.amount)
                                        .data((short) reward.item.data).name(reward.item.name)
                                        .build();
                                for (Map.Entry<String, Integer> stringIntegerEntry : reward.item.enchantmentMap.entrySet()) {
                                    itemStack.addUnsafeEnchantment(new EnchantmentWrapper(
                                            Enchantment.getByName(stringIntegerEntry.getKey().toLowerCase()).id), stringIntegerEntry.getValue());
                                }
                                if (reward.item.glowing)
                                    itemStack.addUnsafeEnchantment(new Glow(70), 1);
                                if (reward.item.nbtTags.size() > 0) {
                                    net.minecraft.server.v1_8_R3.ItemStack nmsI = CraftItemStack.asNMSCopy(itemStack);
                                    NBTTagCompound compound = nmsI.getTag();
                                    if (compound == null) compound = new NBTTagCompound();
                                    reward.item.nbtTags.forEach((compound::setString));
                                    nmsI.setTag(compound);
                                    itemStack = CraftItemStack.asBukkitCopy(nmsI);
                                }
                                if (!plugin.getSettingsManager().getSettings().instantPickup)
                                    item.setItemStack(itemStack);
                                else {
                                    item.remove();
                                    int slotsLeft = event.getPlayer().getInventory().firstEmpty();
                                    if (slotsLeft < 0) {
                                        plugin.consumeMessage(event.getPlayer(),
                                                plugin.getSettingsManager().getSettings().slotsFullMessage,
                                                Collections.emptyMap());

                                    } else event.getPlayer().getInventory().setItem(slotsLeft, itemStack);
                                }
                            } else {
                                item.remove();
                            }
                        }
                        if (reward.commands.length > 0) {
                            for (String command : reward.commands) {
                                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "/" + command);
                            }
                        }
                        event.setExpToDrop(reward.xp);
                    }
                default:
                    break;
            }


        } else {
            // If they try fishing like normal outside of special zone, we cancel it.
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
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
