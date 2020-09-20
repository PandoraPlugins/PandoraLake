package dev.minecraftplugins.pandora.pandoralake.settings;

import com.google.gson.annotations.SerializedName;
import dev.minecraftplugins.pandora.pandoralake.settings.messages.Message;

import java.io.Serializable;

public class Settings implements Serializable {
    @SerializedName("WGFlag")
    public String worldguardFlag;
    @SerializedName("FishingSpeed")
    public int fishingSpeed;
    @SerializedName("FishingSpeedVariation")
    public int fishingSpeedRange;
    @SerializedName("InstantPickup")
    public boolean instantPickup;
    @SerializedName("NoFishingMessage")
    public Message noFishingMessage;
    @SerializedName("FishingMessage")
    public Message fishingMessage;
}
