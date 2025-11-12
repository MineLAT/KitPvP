package com.planetgallium.kitpvp.api;

import com.planetgallium.kitpvp.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.Toolkit;

public class EventListener implements Listener {

	private final Arena arena;
	
	public EventListener(Game plugin) {
		this.arena = plugin.getArena();
	}
	
	@EventHandler
	public void onAbility(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (Toolkit.inArena(event.getPlayer())) {
				final Player player = event.getPlayer();
                if (!Toolkit.isAbilityPlayer(player, false)) {
                    return;
                }

				final ItemStack item = Toolkit.getHandItemForInteraction(event);

				if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
					Ability abilityResult = arena.getAbilities().getAbilityByActivator(item);

					if (abilityResult != null && arena.getUtilities().isCombatActionPermittedInRegion(player)) {
                        abilityResult.run(event, player, item);
					}
				}
			}
		}
	}

}
