package dev.minecraftplugins.pandora.pandoralake.commands;

import com.azortis.azortislib.command.Command;
import com.azortis.azortislib.command.CommandInjector;
import com.azortis.azortislib.command.builders.CommandBuilder;
import com.azortis.azortislib.command.executors.ICommandExecutor;
import com.azortis.azortislib.inventory.item.ItemBuilder;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import dev.minecraftplugins.pandora.pandoralake.glowing.Glow;
import dev.minecraftplugins.pandora.pandoralake.settings.rewards.Reward;
import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GiveCommand implements ICommandExecutor {

    private final PandoraLake plugin;

    public GiveCommand(PandoraLake plugin) {
        this.plugin = plugin;
        Command c = new CommandBuilder()
                .setDescription("Give lake item.")
                .setExecutor(this)
                .setName("lakegive")
                .setPlugin(plugin)
                .setUsage("/lakegive (ItemName)")
                .build();
        CommandInjector.injectCommand("lakes", c, true);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission("lakes.reload")) {
            if (strings.length > 0) {
                if (commandSender instanceof Player) {
                    StringBuilder rewardName = new StringBuilder(strings[0]);
                    for(int i = 1; i < strings.length; i++) {
                        rewardName.append(" ").append(strings[i]);
                    }
                    ((Player) commandSender).getInventory().addItem(getReward(
                            plugin.getSettingsManager().getRewards().rewardMap.get(rewardName.toString())));
                }
            }
        }
        return true;
    }


    private ItemStack getReward(Reward reward) {
        ItemStack itemStack = ItemBuilder.start(Material.getMaterial(reward.item.id))
                .lore(reward.item.lore).amount(reward.item.amount)
                .name(reward.item.name)
                .build();
        if(reward.item.data != 0) itemStack.setDurability((short) reward.item.data);
        for (Map.Entry<String, Integer> stringIntegerEntry : reward.item.enchantmentMap.entrySet()) {
            itemStack.addUnsafeEnchantment(new EnchantmentWrapper(
                    Enchantment.getByName(stringIntegerEntry.getKey().toLowerCase()).id), stringIntegerEntry.getValue());
        }
        if (reward.item.glowing)
            itemStack.addUnsafeEnchantment(new Glow(75), 1);
        if (reward.item.nbtTags.size() > 0) {
            net.minecraft.server.v1_8_R3.ItemStack nmsI = CraftItemStack.asNMSCopy(itemStack);
            NBTTagCompound compound = nmsI.getTag();
            if (compound == null) compound = new NBTTagCompound();
            reward.item.nbtTags.forEach((compound::setString));
            nmsI.setTag(compound);
            itemStack = CraftItemStack.asBukkitCopy(nmsI);
        }
        return itemStack;
    }


}
