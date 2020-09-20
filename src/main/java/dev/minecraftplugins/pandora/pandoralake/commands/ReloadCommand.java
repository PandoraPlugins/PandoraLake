package dev.minecraftplugins.pandora.pandoralake.commands;

import com.azortis.azortislib.command.Command;
import com.azortis.azortislib.command.CommandInjector;
import com.azortis.azortislib.command.builders.CommandBuilder;
import com.azortis.azortislib.command.executors.ICommandExecutor;
import com.azortis.azortislib.utils.FormatUtil;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements ICommandExecutor {
    private final PandoraLake plugin;

    public ReloadCommand(PandoraLake plugin) {
        this.plugin = plugin;
        Command c = new CommandBuilder()
                .setDescription("Reload lake plugin")
                .setExecutor(this)
                .setName("lakerl")
                .setPlugin(plugin)
                .setUsage("/lakerl")
                .build();
        CommandInjector.injectCommand("lakerl", c, true);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission("lakes.reload")) {
            commandSender.sendMessage(FormatUtil.color("&6Reloaded Lakes plugin!"));
            plugin.getSettingsManager().reloadRewards();
            plugin.getSettingsManager().reloadSettings();
            plugin.getRewardsManager().reload();
        }
        return true;
    }
}
