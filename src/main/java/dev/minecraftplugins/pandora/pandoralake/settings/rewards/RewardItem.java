package dev.minecraftplugins.pandora.pandoralake.settings.rewards;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class RewardItem {
    @SerializedName("GiveOnFish")
    public boolean shouldGive;
    @SerializedName("Name")
    public String name;
    @SerializedName("Lore")
    public String[] lore;
    @SerializedName("Amount")
    public int amount;
    @SerializedName("MaterialID")
    public int id;
    @SerializedName("MaterialData")
    public int data;
    @SerializedName("NBTTags")
    public String[] nbtTags;
    @SerializedName("Glowing")
    public boolean glowing;
    @SerializedName("Enchantments")
    public Map<String, Integer> enchantmentMap;
}
