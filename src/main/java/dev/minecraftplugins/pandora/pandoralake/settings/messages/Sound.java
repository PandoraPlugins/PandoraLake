package dev.minecraftplugins.pandora.pandoralake.settings.messages;

import com.google.gson.annotations.SerializedName;

public class Sound {
    @SerializedName("Enabled")
    public boolean enabled;
    @SerializedName("SoundName")
    public org.bukkit.Sound sound;
    @SerializedName("Pitch")
    public float pitch;
    @SerializedName("Volume")
    public float volume;
}
