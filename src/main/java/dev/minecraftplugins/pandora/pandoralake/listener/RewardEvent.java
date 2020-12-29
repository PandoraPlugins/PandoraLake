package dev.minecraftplugins.pandora.pandoralake.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class RewardEvent extends Event {
    private static final HandlerList HANDLER = new HandlerList();

    private final ItemStack item;
    private final Player player;

    public RewardEvent(ItemStack item, Player player){
        this.item = item;
        this.player = player;
    }

    public ItemStack getItem() {
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHanderList(){
        return HANDLER;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER;
    }
}
