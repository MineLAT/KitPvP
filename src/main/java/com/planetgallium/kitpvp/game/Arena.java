package com.planetgallium.kitpvp.game;

import java.util.*;
import java.util.stream.Collectors;

import com.planetgallium.kitpvp.util.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Scoreboard;

import com.planetgallium.kitpvp.Game;

public class Arena {

	private final Game plugin;
	private final Random random;

	private final Resources resources;
	private final Resource config;

	private final Map<UUID, UUID> hitCache;
	private final Map<UUID, Map<String, ItemStack>> items;

	private final Utilities utilties;
	private final Leaderboards leaderboards;
	private final Stats stats;
	private final Kits kits;
	private final Abilities abilities;
	private final KillStreaks killstreaks;
	private final Cooldowns cooldowns;
	private final Menus menus;

	public Arena(Game plugin, Resources resources) {
		this.plugin = plugin;
		this.random = new Random();

		this.resources = resources;
		this.config = resources.getConfig();

		this.hitCache = new HashMap<>();
		this.items = new HashMap<>();

		this.utilties = new Utilities(plugin, this);
		this.leaderboards = new Leaderboards(plugin);
		this.stats = new Stats(plugin, this);
		this.kits = new Kits(plugin, this);
		this.abilities = new Abilities(plugin);
		this.killstreaks = new KillStreaks(resources);
		this.cooldowns = new Cooldowns(plugin, this);
		this.menus = new Menus(resources);
	}
	
	public void addPlayer(Player p, boolean toSpawn, boolean giveItems) {
		cooldowns.clearPlayerAbilityCooldowns(p.getUniqueId());

		kits.resetPlayerKit(p.getUniqueId());

		if (config.getBoolean("Arena.ResetKillStreakOnLeave")) {
			killstreaks.setStreak(p, 0);
		}
		
		if (config.getBoolean("Arena.ClearPotionEffectsOnJoin")) {
			for (PotionEffect effect : p.getActivePotionEffects()) {
				p.removePotionEffect(effect.getType());
			}
		}
		
		if (p.getFireTicks() > 0) {
			p.setFireTicks(0);
		}

		p.setGameMode(GameMode.SURVIVAL);

		if (config.getBoolean("Arena.ResetMaxHealthOnDeath")) {
			Toolkit.setMaxHealth(p, 20);
		}

		if (config.getBoolean("Arena.FancyDeath")) {
//			p.setHealth(20.0);
			p.setHealth(Toolkit.getMaxHealth(p));
		}
		
		p.setExp(0f);
		p.setFoodLevel(20);

		if (giveItems) {
			giveArenaItems(p);
		}

		if (toSpawn) {
			toSpawn(p, p.getWorld().getName());
		}

		if (resources.getScoreboard().getBoolean("Scoreboard.General.Enabled")) {
			updateScoreboards(p, false);
		}
	}
	
	public void removePlayer(Player p) {
		CacheManager.getPlayerAbilityCooldowns(p.getUniqueId()).clear();
		CacheManager.getPotionSwitcherUsers().remove(p.getUniqueId());

		for (PotionEffect effect : p.getActivePotionEffects()) {
			p.removePotionEffect(effect.getType());
		}
		
		kits.resetPlayerKit(p.getUniqueId());

		if (config.getBoolean("Arena.ResetKillStreakOnLeave")) {
			getKillStreaks().resetStreak(p);
		}
		
		p.setExp(0f);
		p.setFoodLevel(20);

		if (resources.getScoreboard().getBoolean("Scoreboard.General.Enabled")) {
			updateScoreboards(p, true);
		}

		stats.pushCachedStatsToDatabase(p.getUniqueId(), false); // cached stats are pushed to database on death
		hitCache.remove(p.getUniqueId());
	}
	
	public void deletePlayer(Player p) {
		if (config.getBoolean("Arena.ClearInventoryOnLeave")) {
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
		}

		CacheManager.getPlayerAbilityCooldowns(p.getUniqueId()).clear();
		hitCache.remove(p.getUniqueId());
		stats.pushCachedStatsToDatabase(p.getUniqueId(), true);
	}
	
	public void giveArenaItems(Player p) {
		ConfigurationSection items = config.getConfigurationSection("Items");

		final Map<String, ItemStack> arenaItems = new HashMap<>();
		for (String identifier : items.getKeys(false)) {
			String itemPath = "Items." + identifier;

			if (config.getBoolean(itemPath + ".Enabled")) {
				ItemStack item = Toolkit.safeItemStack(utilties.addPlaceholdersIfPossible(p, config.fetchString(itemPath + ".Material")));
				ItemMeta meta = item.getItemMeta();

				meta.setDisplayName(utilties.addPlaceholdersIfPossible(p, config.fetchString(itemPath + ".Name")));
				meta.setLore(config.getStringList(itemPath + ".Lore").stream().map(s -> utilties.addPlaceholdersIfPossible(p, s)).collect(Collectors.toList()));

				item.setItemMeta(meta);

				arenaItems.put(identifier, item);
				p.getInventory().setItem(config.getInt(itemPath + ".Slot"), item);
			}
		}
		this.items.put(p.getUniqueId(), arenaItems);
	}

	public void toSpawn(Player p, String arenaName) {
		if (config.contains("Arenas." + arenaName)) {
			p.teleport(Toolkit.getLocationFromResource(config,
					"Arenas." + arenaName + "." + generateRandomArenaSpawn(arenaName)));
		} else {
			p.sendMessage(resources.getMessages().fetchString("Messages.Error.Arena")
					.replace("%arena%", arenaName));
		}
	}
	
	public void updateScoreboards(Player p, boolean hide) {
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
		String scoreboardTitle = utilties.addPlaceholdersIfPossible(p,
				resources.getScoreboard().fetchString("Scoreboard.General.Title"));
		Infoboard scoreboard = new Infoboard(board, scoreboardTitle);
		
		if (!hide) {
			for (String line : resources.getScoreboard().getStringList("Scoreboard.Lines")) {
				scoreboard.add(utilties.addPlaceholdersIfPossible(p, line));
			}
		} else {
			scoreboard.hide();
		}

		scoreboard.update(p);
	}

	public String generateRandomArenaSpawn(String arenaName) {
		ConfigurationSection section = config.getConfigurationSection("Arenas." + arenaName);
		List<String> spawnKeys = new ArrayList<>(section.getKeys(false));

		return spawnKeys.get(random.nextInt(spawnKeys.size()));
	}

	public Map<UUID, UUID> getHitCache() { return hitCache; }

	public Map<UUID, Map<String, ItemStack>> getItems() { return items; }

	public Stats getStats() { return stats; }

	public Utilities getUtilities() { return utilties; }

	public Leaderboards getLeaderboards() { return leaderboards; }
	
	public Kits getKits() { return kits; }

	public Abilities getAbilities() { return abilities; }
	
	public KillStreaks getKillStreaks() { return killstreaks; }
	
	public Cooldowns getCooldowns() { return cooldowns; }

	public Menus getMenus() { return menus; }
	
}
