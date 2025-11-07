package com.planetgallium.kitpvp.listener;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.api.ability.ArcherAbility;
import com.planetgallium.kitpvp.api.ability.BomberAbility;
import com.planetgallium.kitpvp.api.ability.ItemAbility;
import com.planetgallium.kitpvp.api.ability.KangarooAbility;
import com.planetgallium.kitpvp.api.ability.NinjaAbility;
import com.planetgallium.kitpvp.api.ability.SoldierAbility;
import com.planetgallium.kitpvp.api.ability.ThunderboltAbility;
import com.planetgallium.kitpvp.api.ability.TricksterAbility;
import com.planetgallium.kitpvp.api.ability.VampireAbility;
import com.planetgallium.kitpvp.api.ability.WarperAbility;
import com.planetgallium.kitpvp.api.ability.WitchAbility;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.game.Utilities;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Resources;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ItemListener implements Listener {

	private final Game plugin;
	private final Arena arena;
	private final Utilities utilities;
	private final Resources resources;
	private final Resource config;

    private final Map<String, ItemAbility> itemAbilities;
	
	public ItemListener(Game plugin) {
		this.plugin = plugin;
		this.arena = plugin.getArena();
		this.utilities = plugin.getArena().getUtilities();
		this.resources = plugin.getResources();
		this.config = resources.getConfig();

        this.itemAbilities = new HashMap<>();
        Arrays.asList(
                new ArcherAbility(),
                new BomberAbility(),
                new KangarooAbility(),
                new NinjaAbility(),
                new SoldierAbility(),
                new ThunderboltAbility(),
                new TricksterAbility(),
                new VampireAbility(),
                new WarperAbility(),
                new WitchAbility()
        ).forEach(ability -> this.itemAbilities.put(ability.name(), ability));

        resources.getAbilities().listen(abilities -> {
            for (ItemAbility ability : this.itemAbilities.values()) {
                ConfigurationSection section = abilities.getConfigurationSection("Abilities." + ability.name());
                if (section == null || !section.getBoolean("Enabled", true)) {
                    section = Resource.empty();
                }

                ability.deserialize(section);
            }
        });
	}

    private void abilities(@NotNull Player player, @NotNull ItemStack item, @NotNull Consumer<Ability> consumer) {
        for (ItemAbility ability : this.itemAbilities.values()) {
            if (!ability.isItem(item)) {
                continue;
            }

            final String permission = "kp.ability." + ability.name().toLowerCase();
            if (!player.hasPermission(permission)) {
                player.sendMessage(resources.getMessages().fetchString("Messages.General.Permission").replace("%permission%", permission));
                continue;
            }

            if (config.getBoolean("Arena.AbilitiesRequireKit") && !arena.getKits().playerHasKit(player.getUniqueId())) {
                player.sendMessage(resources.getMessages().fetchString("Messages.Error.Kit"));
                continue;
            }

            if (!utilities.isCombatActionPermittedInRegion(player)) {
                continue;
            }

            consumer.accept(ability);
        }
    }

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		if (Toolkit.inArena(p) && (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)) {

			ItemStack interactedItem = Toolkit.getHandItemForInteraction(e);
            abilities(p, interactedItem, ability -> ability.run(e, p, interactedItem));

			if (isSplashPotion(interactedItem)) {
                final WitchAbility ability = (WitchAbility) this.itemAbilities.get(ItemAbility.WITCH);
                ability.runSplash(e, p, interactedItem);
			} else if (Toolkit.hasMatchingMaterial(interactedItem, "TNT")) {
				specialTNT(e, p, interactedItem);

			}

			/* Kit Item and custom Arena Items */

			final Map<String, ItemStack> items = arena.getItems().get(p.getUniqueId());
			if (items != null) {
				for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
					final ItemStack item = entry.getValue();
					if (interactedItem.getType() == item.getType() && Toolkit.hasMatchingDisplayName(interactedItem, item)) {
						Toolkit.runCommands(p, config.getStringList("Items." + entry.getKey() + ".Commands"), "none", "none");

						if (entry.getKey().equalsIgnoreCase("kits")) {
							arena.getMenus().getKitMenu().open(p);
						}

						e.setCancelled(true);
					}
				}
			}
			
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent e) {
		Player damager = e.getPlayer();

		if (Toolkit.inArena(damager) && e.getRightClicked().getType() == EntityType.PLAYER) {
			ItemStack interactedItem = Toolkit.getHandItemForInteraction(e);
			Player damagedPlayer = (Player) e.getRightClicked();

			if (config.getBoolean("Arena.NoKitProtection")) {
				if (!arena.getKits().playerHasKit(damagedPlayer.getUniqueId())) {
					return;
				}
			}

            abilities(damager, interactedItem, ability -> ability.run(e, damager, damagedPlayer, interactedItem));
		}
	}

	private void specialTNT(PlayerInteractEvent e, Player p, ItemStack abilityItem) {
		if (config.getBoolean("TNT.Enabled") &&
				Toolkit.hasMatchingDisplayName(abilityItem, config.fetchString("TNT.Name"))) {

			if (!utilities.isCombatActionPermittedInRegion(p)) {
				return;
			}

			Location handLocation = p.getLocation();
			handLocation.setY(handLocation.getY() + 1.0);
			Vector direction = handLocation.getDirection();

			Entity entity = p.getWorld().spawn(handLocation, TNTPrimed.class);
			entity.setVelocity(direction.multiply(1.5));
			entity.setCustomName(p.getName());

			e.setCancelled(true);

            abilityItem.setAmount(abilityItem.getAmount() - 1);
            Toolkit.setHandItemForInteraction(e, abilityItem);
		}
	}

	@EventHandler
	public void onProjectileHitsEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getCause() == DamageCause.PROJECTILE) {

			if (e.getDamager() instanceof Snowball) {
				Player damagedPlayer = (Player) e.getEntity();
				Snowball snowball = (Snowball) e.getDamager();
				hitBySnowball(damagedPlayer, snowball);

			} else if (e.getDamager() instanceof Egg) {
				Player damagedPlayer = (Player) e.getEntity();
				Egg egg = (Egg) e.getDamager();
				hitByEgg(damagedPlayer, egg);
			}
		}
	}

	private void hitBySnowball(Player damagedPlayer, Snowball snowball) {
		if (snowball.getCustomName() != null && snowball.getCustomName().equals("bullet")) {
			if (Toolkit.inArena(damagedPlayer) && arena.getKits().playerHasKit(damagedPlayer.getUniqueId())) {
				damagedPlayer.damage(4.5);
			}
		}
	}

	private void hitByEgg(Player damagedPlayer, Egg egg) {
        if (!(egg.getShooter() instanceof Player)) {
            return;
        }

        final TricksterAbility ability = (TricksterAbility) this.itemAbilities.get(ItemAbility.TRICKSTER);
        if (!ability.isItem(egg.getItem())) {
            return;
        }

        if (!Toolkit.inArena(damagedPlayer) || !arena.getKits().playerHasKit(damagedPlayer.getUniqueId())) {
            return;
        }

        Player shooter = (Player) egg.getShooter();
        Location shooterLocation = shooter.getLocation();

        if (!utilities.isCombatActionPermittedInRegion(damagedPlayer)) {
            shooter.sendMessage(resources.getMessages().fetchString("Messages.Error.PVP"));
            return;
        }

        shooter.teleport(damagedPlayer);
        damagedPlayer.teleport(shooterLocation);

        ability.run(shooter, damagedPlayer);
	}

	@EventHandler
	public void onBowShot(EntityShootBowEvent e) {
		if (e.getEntity() instanceof Player && Toolkit.inArena(e.getEntity())) {
			Player p = (Player) e.getEntity();

			if (!p.hasPermission("kp.ability.archer")) {
				return;
			}

            final ArcherAbility ability = (ArcherAbility) this.itemAbilities.get(ItemAbility.ARCHER);

			final Integer ammoSlot = ability.fireItem().slot(p);
            if (ammoSlot == null) {
                return;
            }

            ItemStack ammo = p.getInventory().getItem(ammoSlot);

            e.getProjectile().setFireTicks(1000);
            if (ability.sound() != null) {
                ability.sound().soundPlayer().play(p.getLocation());
            }

            if (ammo.getAmount() == 1) {
                p.getInventory().setItem(ammoSlot, new ItemStack(Material.AIR));
            } else {
                ammo.setAmount(ammo.getAmount() - 1);
            }
		}
	}

	public boolean isSplashPotion(ItemStack item) {
		int serverVersion = Toolkit.versionToNumber();
		return Toolkit.hasMatchingMaterial(item, serverVersion == 18 ? "POTION" : "SPLASH_POTION");
	}

}
