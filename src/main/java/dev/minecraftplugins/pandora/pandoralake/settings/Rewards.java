package dev.minecraftplugins.pandora.pandoralake.settings;

import com.google.gson.annotations.SerializedName;
import dev.minecraftplugins.pandora.pandoralake.settings.rewards.Reward;

import java.io.Serializable;
import java.util.Map;

public class Rewards implements Serializable {
    @SerializedName("Rewards")
    public Map<String, Reward> rewardMap;
}
