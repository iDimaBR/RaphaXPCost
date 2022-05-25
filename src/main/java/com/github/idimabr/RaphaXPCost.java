package com.github.idimabr;

import com.github.idimabr.utils.ActionBar;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.UUID;

public final class RaphaXPCost extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getPluginManager().registerEvents(this, this);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        reloadConfig();
    }

    private final HashMap<UUID, Triple<Integer, Integer, Integer>> enchanters = Maps.newHashMap();

    @EventHandler
    public void onPrepare(PrepareItemEnchantEvent e){
        final int[] xps = e.getExpLevelCostsOffered();
        enchanters.put(e.getEnchanter().getUniqueId(), Triple.of(xps[0], xps[1], xps[2]));
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent e){
        final FileConfiguration config = getConfig();
        final Player player = e.getEnchanter();
        if(enchanters.get(player.getUniqueId()) == null) return;

        final int slot = e.whichButton();
        final Triple<Integer, Integer, Integer> costValues = enchanters.get(player.getUniqueId());

        int cost = 0;
        switch(slot){
            case 0: cost = costValues.getLeft(); break;
            case 1: cost = costValues.getMiddle(); break;
            case 2: cost = costValues.getRight(); break;
        }

        final int fixedCost = cost - (slot + 1);
        player.setLevel(player.getLevel() - fixedCost);
        e.setExpLevelCost(0);

        if(config.getBoolean("Messages.EnchantedEnabled"))
            new ActionBar(
                    config.getString("Messages.Enchanted")
                            .replace("&","§")
                            .replace("%level%", cost+""),
                    player
            );
    }
}
