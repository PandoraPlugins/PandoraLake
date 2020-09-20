package dev.minecraftplugins.pandora.pandoralake.settings.rewards;

import com.google.gson.annotations.SerializedName;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;

public class Reward {
    @SerializedName("Enabled")
    public boolean enabled;
    @SerializedName("Chance")
    public double chance;
    @SerializedName("XP")
    public int xp;
    @SerializedName("Commands")
    public String[] commands;
    @SerializedName("Item")
    public RewardItem item;
    @SerializedName("Message")
    public Message message;
}
