package dev.minecraftplugins.pandora.pandoralake.settings.messages;

import com.google.gson.annotations.SerializedName;

public class Actionbar {
    @SerializedName("Enabled")
    public boolean enabled;
    @SerializedName("Message")
    public String message;
    @SerializedName("Sound")
    public Sound sound;
}
