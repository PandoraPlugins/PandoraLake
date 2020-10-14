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
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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
                    // Cast out rod, we should do nothing other than set it to bite immediately.
                    setBiteTime(event.getHook(), 1);
                    break;
                case FAILED_ATTEMPT:
                    // Reeled rod back in.
                    if (updater.tryCatch(event.getPlayer())) {
                        // Able to catch.
                        plugin.consumeMessage(event.getPlayer(),
                                plugin.getSettingsManager().getSettings().caughtFishMessage, Collections.emptyMap());
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


                    } else {
                        // Failed to catch
                        event.getCaught().remove();
                        updater.removePlayer(event.getPlayer());
                        plugin.consumeMessage(event.getPlayer(),
                                plugin.getSettingsManager().getSettings().fishTooEarlyMessage, Collections.emptyMap());

                    }
                    break;
                case CAUGHT_FISH:
                    // Caught fake fish
                    event.getCaught().remove();
                    updater.removePlayer(event.getPlayer());
                    // todo: add placeholders.
                    plugin.consumeMessage(event.getPlayer(), plugin.getSettingsManager().getSettings().catchBobberMessage,
                            Collections.emptyMap());
                    break;
                default:
                    break;
            }

        } else {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                event.getCaught().remove();
                // We cancelled their event if they try fishing.
                // Now we send them configured messages from their overlords about fishing.
                Message message = plugin.getSettingsManager().getSettings().noFishingMessage;
                // todo: add placeholders.
                plugin.consumeMessage(event.getPlayer(), message, Collections.emptyMap());
            }
        }
    }

    @EventHandler
    public void onBobberHit(ProjectileHitEvent event) {
        // Detect if hook has hit water
        if (event.getEntity() instanceof FishHook) {
            // Made sure this is hook
            Block b = event.getEntity().getLocation().getBlock();
            System.out.println(b);
            // get the block the hook hit
            if (b.getType() == Material.WATER || b.getType() == Material.STATIONARY_WATER) {
                // Make sure its water
                if (WGBukkit.getPlugin().getRegionContainer().createQuery().testState(event.getEntity().getLocation(), (Player) null,
                        plugin.getFishingFlag())) {
                    // The hook has hit water and is inside our wg region. We add them to the list of things.
                    int catchTime = plugin.getSettingsManager().getSettings().fishingSpeed;
                    // We add to the base ticks a certain number within the range given.
                    if (plugin.getSettingsManager().getSettings().fishingSpeedRange > 0)
                        catchTime += ThreadLocalRandom.current().nextInt(plugin.getSettingsManager().getSettings().fishingSpeedRange);
                    updater.addPlayer((Player) event.getEntity().getShooter(), catchTime);
                    // todo: add in placeholders
                    plugin.consumeMessage((Player) event.getEntity().getShooter(),
                            plugin.getSettingsManager().getSettings().fishingMessage,
                            Collections.emptyMap());
                }
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
