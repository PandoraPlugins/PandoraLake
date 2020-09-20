package dev.minecraftplugins.pandora.pandoralake.settings.messages;

import com.google.gson.annotations.SerializedName;

public class TitlebarData {
    @SerializedName("Enabled")
    public boolean enabled;
    @SerializedName("Message")
    public String message;
    @SerializedName("FadeInTime")
    public int fadeIn;
    @SerializedName("FadeOutTime")
    public int fadeOut;
    @SerializedName("ViewingTime")
    public int showingTime;
    @SerializedName("Sound")
    public Sound sound;
}
