package dev.minecraftplugins.pandora.pandoralake.rewards;

import dev.minecraftplugins.pandora.pandoralake.PandoraLake;
import dev.minecraftplugins.pandora.pandoralake.settings.rewards.Reward;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class RewardsManager {
    private final PandoraLake plugin;
    Map<String, Double> chanceMap = new HashMap<>();

    public RewardsManager(PandoraLake plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        int totalEnabledRewardsChance = 0;
        chanceMap.clear();
        Map<String, Reward> rewardMap = plugin.getSettingsManager().getRewards().rewardMap;
        for (Map.Entry<String, Reward> stringRewardEntry : rewardMap.entrySet()) {
            if (stringRewardEntry.getValue().enabled)
                totalEnabledRewardsChance += stringRewardEntry.getValue().chance;
        }
        for (Map.Entry<String, Reward> stringRewardEntry : rewardMap.entrySet()) {
            if (stringRewardEntry.getValue().enabled) {
                double chance = (stringRewardEntry.getValue().chance * 100.0) / (double) totalEnabledRewardsChance;
                chanceMap.put(stringRewardEntry.getKey(), chance);
            }
        }
    }

    public Reward getRandomReward() {
        List<Map.Entry<String, Reward>> rewards = new ArrayList<>();
        double roll = ThreadLocalRandom.current().nextDouble(100.0);
        for (Map.Entry<String, Reward> stringRewardEntry : plugin.getSettingsManager().getRewards().rewardMap.entrySet()) {
            if (stringRewardEntry.getValue().enabled && chanceMap.get(stringRewardEntry.getKey()) >= roll) {
                rewards.add(stringRewardEntry);
            }
        }
        if (rewards.size() > 1) {
            Collections.shuffle(rewards);
            return rewards.get(0).getValue();
        } else if (rewards.size() == 1) return rewards.get(0).getValue();
        else return null;
    }
}
