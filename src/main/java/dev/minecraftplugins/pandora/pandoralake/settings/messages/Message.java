package dev.minecraftplugins.pandora.pandoralake.settings.messages;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("ActionBar")
    public Actionbar actionbar;
    @SerializedName("Bars")
    public Titlebar titlebar;
    @SerializedName("Message")
    public String message;
    @SerializedName("MessageSound")
    public Sound sound;
}
