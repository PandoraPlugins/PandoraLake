package dev.minecraftplugins.pandora.pandoralake.settings.messages;

import com.google.gson.annotations.SerializedName;

public class Titlebar {
    @SerializedName("TitleBar")
    public TitlebarData title;
    @SerializedName("SubTitle")
    public TitlebarData subTitle;
}
