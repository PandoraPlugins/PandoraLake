package dev.minecraftplugins.pandora.pandoralake;

import com.azortis.azortislib.utils.FormatUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.minecraftplugins.pandora.pandoralake.commands.GiveCommand;
import dev.minecraftplugins.pandora.pandoralake.commands.ReloadCommand;
import dev.minecraftplugins.pandora.pandoralake.glowing.Glow;
import dev.minecraftplugins.pandora.pandoralake.listener.FishingListener;
import dev.minecraftplugins.pandora.pandoralake.rewards.RewardsManager;
import dev.minecraftplugins.pandora.pandoralake.settings.SettingsManager;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

public final class PandoraLake extends JavaPlugin {
    private StateFlag fishingFlag;
    private SettingsManager settingsManager;
    private RewardsManager rewardsManager;

    @Override
    public void onLoad() {
        settingsManager = new SettingsManager(this);
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            Bukkit.getLogger().severe("Could not hook into WorldGuard!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        FlagRegistry registry = WGBukkit.getPlugin().getFlagRegistry();
        fishingFlag = new StateFlag(settingsManager.getSettings().worldguardFlag, false);
        registry.register(fishingFlag);
    }

    public RewardsManager getRewardsManager() {
        return rewardsManager;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        if (checkWorldGuard()) {
            Bukkit.getLogger().severe("Could not hook into WorldGuard!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        registerGlow();
        new ReloadCommand(this);
        new GiveCommand(this);
        rewardsManager = new RewardsManager(this);
        Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);
        Bukkit.getLogger().info("Hooked into WorldGuard!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public StateFlag getFishingFlag() {
        return fishingFlag;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    private boolean checkWorldGuard() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") == null ||
                !Bukkit.getPluginManager().getPlugin("WorldGuard").isEnabled();
    }

    private void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);

            Glow glow = new Glow(70);
            Enchantment.registerEnchantment(glow);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void consumeMessage(Player player, Message message, Map<String, String> placeholders) {
        // Clear titles and actionbar
        String titleBarMessage = "";
        String subTitleBarMessage = "";
        if (message.titlebar != null) {
            if (message.titlebar.title != null) {
                titleBarMessage = FormatUtil.color(message.titlebar.title.message);
            }
            if (message.titlebar.subTitle != null) {
                subTitleBarMessage = FormatUtil.color(message.titlebar.subTitle.message);
            }
        }
        for (String key : placeholders.keySet()) {
            titleBarMessage = titleBarMessage.replace(key, placeholders.get(key));
            subTitleBarMessage = subTitleBarMessage.replace(key, placeholders.get(key));
        }
        if (message.titlebar != null) {
            if (message.titlebar.title != null)
                if (message.titlebar.title.sound.enabled)
                    Bukkit.getScheduler().runTaskLater(this, () -> player.playSound(player.getLocation(),
                            message.titlebar.title.sound.sound, message.titlebar.title.sound.volume,
                            message.titlebar.title.sound.pitch), message.titlebar.title.fadeIn);
            if (message.titlebar.subTitle != null)
                if (message.titlebar.subTitle.sound.enabled)
                    Bukkit.getScheduler().runTaskLater(this, () -> player.playSound(player.getLocation(),
                            message.titlebar.subTitle.sound.sound, message.titlebar.subTitle.sound.volume,
                            message.titlebar.subTitle.sound.pitch), message.titlebar.subTitle.fadeIn);
        }
        // Readability 0, coolness factor 100 - ternary operators FTW
        sendTitle(player,
                message.titlebar.title.enabled ? titleBarMessage : "",
                message.titlebar.subTitle.enabled ? subTitleBarMessage : "",
                message.titlebar.title.enabled ? message.titlebar.title.fadeIn : message.titlebar.subTitle.fadeIn,
                message.titlebar.title.enabled ? message.titlebar.title.showingTime : message.titlebar.subTitle.showingTime,
                message.titlebar.title.enabled ? message.titlebar.title.fadeOut : message.titlebar.subTitle.fadeOut);

        if (!message.message.isEmpty()) {
            String m = message.message;
            for (String key : placeholders.keySet()) {
                m = m.replace(key, placeholders.get(key));
            }
            player.sendMessage(FormatUtil.color(message.message));
        }

        if (message.sound.enabled)

            player.playSound(player.getLocation(), message.sound.sound,
                    message.sound.volume, message.sound.pitch);


        if (message.actionbar.enabled) {

            if (message.actionbar.sound.enabled)
                player.playSound(player.getLocation(), message.actionbar.sound.sound,
                        message.actionbar.sound.volume, message.actionbar.sound.pitch);
            String m = message.actionbar.message;
            for (String key : placeholders.keySet())
                m = m.replace(key, placeholders.get(key));
            sendActionBar(player, FormatUtil.color(m));
        }


    }

    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendTitle(Player player, String titleMessage, String subTitleMessage, int fadeInTime, int showTime, int fadeOutTime) {
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, new ChatComponentText(titleMessage));
        PacketPlayOutTitle subTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, new ChatComponentText(subTitleMessage));
        PacketPlayOutTitle duration = new PacketPlayOutTitle(fadeInTime, showTime, fadeOutTime);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subTitle);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(duration);
    }


}
