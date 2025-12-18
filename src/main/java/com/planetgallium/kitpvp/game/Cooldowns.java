package com.planetgallium.kitpvp.game;

import com.cryptomorin.xseries.XMaterial;
import com.google.gson.JsonObject;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.api.Kit;
import com.planetgallium.kitpvp.apollo.util.JsonPacketUtil;
import com.planetgallium.kitpvp.apollo.util.JsonUtil;
import com.planetgallium.kitpvp.util.CacheManager;
import com.planetgallium.kitpvp.api.util.Timespan;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class Cooldowns {

	private final Stats stats;
	private final Infobase database;
	
	public Cooldowns(Game plugin, Arena arena) {
		this.stats = arena.getStats();
		this.database = plugin.getDatabase();
	}

	public void setAbilityCooldown(@NotNull Player player, @NotNull Ability ability) {
		CacheManager.getPlayerAbilityCooldowns(player.getUniqueId()).put(ability.name(), System.currentTimeMillis());

        // LunarClient cooldown
        if (Game.getInstance().getApolloListener().isPlayerRunningApollo(player)) {
            JsonObject message = new JsonObject();
            message.addProperty("@type", "type.googleapis.com/lunarclient.apollo.cooldown.v1.DisplayCooldownMessage");
            message.addProperty("name", "ability-" + ability.name());
            message.addProperty("duration", JsonUtil.createDurationObject(Duration.ofMillis(ability.cooldown().toMillis())));
            // TODO: Create an utility class to convert XMaterial to relevant asset name
            message.add("icon", JsonUtil.createItemStackIconObject(ability.activator().material() == XMaterial.FIREWORK_ROCKET ? "FIREWORKS" : ability.activator().material().name(), 0, 0));

            JsonPacketUtil.sendPacket(player, message);
        }
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
