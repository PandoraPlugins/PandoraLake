package dev.minecraftplugins.pandora.pandoralake;

import com.azortis.azortislib.utils.FormatUtil;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.minecraftplugins.pandora.pandoralake.commands.ReloadCommand;
import dev.minecraftplugins.pandora.pandoralake.rewards.RewardsManager;
import dev.minecraftplugins.pandora.pandoralake.settings.SettingsManager;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class PandoraLake extends JavaPlugin {
    private StateFlag fishingFlag;
    private SettingsManager settingsManager;
    private RewardsManager rewardsManager;

    @Override
    public void onLoad() {
        settingsManager = new SettingsManager(this);
        if (checkWorldGuard()) {
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
        new ReloadCommand(this);
        rewardsManager = new RewardsManager(this);
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

    public void consumeMessage(Player player, Message message, Map<String, String> placeholders) {
        // Clear titles and actionbar
        sendActionBar(player, "");
        clearTitles(player);
        if (!message.message.isEmpty()) {
            String m = message.message;
            for (String key : placeholders.keySet())
                m = m.replace(key, placeholders.get(key));
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

        if (message.titlebar.title.enabled) {

            if (message.titlebar.title.sound.enabled)
                Bukkit.getScheduler().runTaskLater(this, () -> player.playSound(player.getLocation(),
                        message.titlebar.title.sound.sound, message.titlebar.title.sound.volume,
                        message.titlebar.title.sound.pitch), message.titlebar.title.fadeIn);
            String m = message.titlebar.title.message;
            for (String key : placeholders.keySet())
                m = m.replace(key, placeholders.get(key));
            sendTitle(player, FormatUtil.color(m), message.titlebar.title.fadeIn,
                    message.titlebar.title.showingTime, message.titlebar.title.fadeOut, false);
        }

        if (message.titlebar.subTitle.enabled) {

            if (message.titlebar.subTitle.sound.enabled)
                Bukkit.getScheduler().runTaskLater(this, () -> player.playSound(player.getLocation(),
                        message.titlebar.subTitle.sound.sound, message.titlebar.subTitle.sound.volume,
                        message.titlebar.subTitle.sound.pitch), message.titlebar.subTitle.fadeIn);
            String m = message.titlebar.subTitle.message;
            for (String key : placeholders.keySet())
                m = m.replace(key, placeholders.get(key));
            sendTitle(player, FormatUtil.color(m), message.titlebar.subTitle.fadeIn,
                    message.titlebar.subTitle.showingTime, message.titlebar.subTitle.fadeOut, true);
        }
    }

    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(new ChatComponentText(message), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void sendTitle(Player player, String text, int fadeInTime, int showTime, int fadeOutTime, boolean subtitle) {
        PacketPlayOutTitle title = subtitle ? new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                new ChatComponentText(text), fadeInTime, showTime, fadeOutTime)
                : new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                new ChatComponentText(text), fadeInTime, showTime, fadeOutTime);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    }

    public void clearTitles(Player player) {
        PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, new ChatComponentText("dummy"));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(title);
    }
}
