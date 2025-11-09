package com.planetgallium.kitpvp.game;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.api.Kit;
import com.planetgallium.kitpvp.util.CacheManager;
import com.planetgallium.kitpvp.api.util.Timespan;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Cooldowns {

	private final Stats stats;
	private final Infobase database;
	
	public Cooldowns(Game plugin, Arena arena) {
		this.stats = arena.getStats();
		this.database = plugin.getDatabase();
	}

	public void setAbilityCooldown(@NotNull UUID uniqueId, @NotNull Ability ability) {
		CacheManager.getPlayerAbilityCooldowns(uniqueId).put(ability.name(), System.currentTimeMillis());
	}

	public void clearPlayerAbilityCooldowns(UUID uniqueId) {
		CacheManager.getPlayerAbilityCooldowns(uniqueId).clear();
	}

	public void setKitCooldown(UUID uniqueId, String kitName) {
		stats.getOrCreateStatsCache(uniqueId).addKitCooldown(kitName, System.currentTimeMillis());
	}

	public Timespan getRemainingCooldown(Player p, Object type) {
		long currentTimeMillis = System.currentTimeMillis();
		long timeLastUsedMillis = 0;
		long actionCooldownMillis = 0;

		if (type instanceof Kit) {

			Kit kit = (Kit) type;
			if (kit.getCooldown() == null) return Timespan.ZERO;

			Object timeLastUsedResult = database.getData(kit.getName() + "_cooldowns", "last_used", p.getUniqueId());
			if (timeLastUsedResult != null) {
				timeLastUsedMillis = (int) timeLastUsedResult;
                // Convert to millis
                if (timeLastUsedMillis < 100000000000L) {
                    timeLastUsedMillis = timeLastUsedMillis * 1000L;
                }
			} else {
				return Timespan.ZERO;
			}

			actionCooldownMillis = kit.getCooldown().toMillis();

		} else if (type instanceof Ability) {

			Ability ability = (Ability) type;
			if (ability.cooldown() == Timespan.ZERO ||
					!CacheManager.getPlayerAbilityCooldowns(p.getUniqueId()).containsKey(ability.name()))
				return Timespan.ZERO;

			timeLastUsedMillis = CacheManager.getPlayerAbilityCooldowns(p.getUniqueId()).get(ability.name());
			actionCooldownMillis = ability.cooldown().toMillis();

		}

		long cooldownRemainingMillis = (timeLastUsedMillis + actionCooldownMillis - currentTimeMillis);
		return Timespan.valueOf(cooldownRemainingMillis);
	}
	
}
