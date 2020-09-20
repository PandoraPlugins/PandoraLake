package dev.minecraftplugins.pandora.pandoralake.settings;


import com.azortis.azortislib.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.minecraftplugins.pandora.pandoralake.PandoraLake;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@SuppressWarnings("all")
public class SettingsManager {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final File settingsFile;
    private final File rewardsFile;
    private Settings settings;
    private Rewards rewards;

    public SettingsManager(PandoraLake plugin) {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
        settingsFile = new File(plugin.getDataFolder(), "settings.json");
        rewardsFile = new File(plugin.getDataFolder(), "rewards.json");
        if (!settingsFile.exists()) FileUtils.copy(plugin.getResource("settings.json"), settingsFile);
        if (!rewardsFile.exists()) FileUtils.copy(plugin.getResource("rewards.json"), rewardsFile);
        try {
            settings = gson.fromJson(new FileReader(settingsFile), Settings.class);
            rewards = gson.fromJson(new FileReader(rewardsFile), Rewards.class);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public Rewards getRewards() {
        return rewards;
    }

    public void saveRewards() {
        try {
            final String json = gson.toJson(rewards);
            rewardsFile.delete();
            Files.write(rewardsFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadRewards() {
        try {
            rewards = gson.fromJson(new FileReader(rewardsFile), Rewards.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Settings getSettings() {
        return settings;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public void saveSettings() {
        try {
            final String json = gson.toJson(settings);
            settingsFile.delete();
            Files.write(settingsFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadSettings() {
        try {
            settings = gson.fromJson(new FileReader(settingsFile), Settings.class);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public File getRewardsFile() {
        return rewardsFile;
    }
}

