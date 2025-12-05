package com.planetgallium.kitpvp.listener;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.api.ability.ArcherAbility;
import com.planetgallium.kitpvp.api.ability.BomberAbility;
import com.planetgallium.kitpvp.api.ability.FlyAbility;
import com.planetgallium.kitpvp.api.ability.ItemAbility;
import com.planetgallium.kitpvp.api.ability.KangarooAbility;
import com.planetgallium.kitpvp.api.ability.NinjaAbility;
import com.planetgallium.kitpvp.api.ability.RocketAbility;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class ItemListener implements Listener {

    private static final boolean PROJECTILE_COMPATIBLE;

    static {
        boolean projectileCompatible = false;
        try {
            Class.forName("org.bukkit.entity.ThrowableProjectile");
            projectileCompatible = true;
        } catch (Throwable ignored) { }
        PROJECTILE_COMPATIBLE = projectileCompatible;
    }

	private final Game plugin;
	private final Arena arena;
	private final Utilities utilities;
	private final Resources resources;
	private final Resource config;

    private final Map<String, ItemAbility> itemAbilities;
    private transient final Map<UUID, List<ItemAbility>> projectileSent = new HashMap<>();
	
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
                new FlyAbility(),
                new KangarooAbility(),
                new NinjaAbility(),
                new RocketAbility(),
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

    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends ItemAbility> T getAbility(@NotNull @MagicConstant(valuesFromClass = ItemAbility.class) String name) {
        return (T) this.itemAbilities.get(name);
    }

    @NotNull
    public Optional<ItemAbility> getAbility(@NotNull Predicate<ItemAbility> predicate) {
        for (ItemAbility ability : this.itemAbilities.values()) {
            if (predicate.test(ability)) {
                return Optional.of(ability);
            }
        }
        return Optional.empty();
    }

    @NotNull
    public Collection<ItemAbility> getAbilities() {
        return this.itemAbilities.values();
    }

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && Toolkit.inArena(player)) {
			final ItemStack interactedItem = Toolkit.getHandItemForInteraction(event);
            final XMaterial material = XMaterial.matchXMaterial(interactedItem);

            for (ItemAbility ability : getAbilities()) {
                if (ability.isListening(PlayerInteractEvent.class)) {
                    if (ability.isItem(interactedItem)) {
                        if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true)) {
                            event.setCancelled(true);
                            break;
                        }
                        if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                            if (material == XMaterial.ENDER_PEARL || material == XMaterial.FIREWORK_ROCKET) {
                                event.setCancelled(true);
                            }

                            ability.run(event, player, interactedItem);
                        }
                    } else if (ability instanceof WitchAbility) {
                        final WitchAbility witchAbility = (WitchAbility) ability;
                        if (witchAbility.isPotionItem(interactedItem)) {
                            if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true)) {
                                event.setCancelled(true);
                                break;
                            }
                            if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                                witchAbility.runSplash(event, player, interactedItem);
                            }
                        }
                    }
                } else if (!PROJECTILE_COMPATIBLE && ability.isListening(ProjectileLaunchEvent.class) && ability.isItem(interactedItem)) {
                    if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true)) {
                        event.setCancelled(true);
                        break;
                    }
                    if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                        projectileSent.computeIfAbsent(player.getUniqueId(), __ -> new ArrayList<>()).add(ability);
                    }
                }
            }

            if (material == XMaterial.TNT) {
				specialTNT(event, player, interactedItem);

			}

			/* Kit Item and custom Arena Items */

			final Map<String, ItemStack> items = arena.getItems().get(player.getUniqueId());
			if (items != null) {
				for (Map.Entry<String, ItemStack> entry : items.entrySet()) {
					final ItemStack item = entry.getValue();
					if (interactedItem.getType() == item.getType() && Toolkit.hasMatchingDisplayName(interactedItem, item)) {
						Toolkit.runCommands(player, config.getStringList("Items." + entry.getKey() + ".Commands"), "none", "none");

						if (entry.getKey().equalsIgnoreCase("kits")) {
							arena.getMenus().getKitMenu().open(player);
						}

						event.setCancelled(true);
					}
				}
			}
			
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		final Player player = event.getPlayer();
		if (event.getRightClicked() instanceof Player && Toolkit.inArena(player)) {
			final ItemStack item = Toolkit.getHandItemForInteraction(event);
			final Player agent = (Player) event.getRightClicked();

            for (ItemAbility ability : getAbilities()) {
                if (ability.isListening(PlayerInteractEntityEvent.class) && ability.isItem(item)) {
                    if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true) || !Toolkit.inCombatArena(player, agent)) {
                        event.setCancelled(true);
                        break;
                    }
                    if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                        ability.run(event, player, agent, item);
                    }
                }
            }
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
	public void onProjectileHitsEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
            final Player agent = (Player) event.getEntity();
            if (event.getDamager() instanceof Projectile) {
                final Projectile projectile = (Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    final Player player = (Player) projectile.getShooter();
                    for (MetadataValue meta : projectile.getMetadata(Ability.META_KEY)) {
                        final Object value = meta.value();
                        if (value instanceof ItemAbility) {
                            final ItemAbility ability = (ItemAbility) value;
                            if (Toolkit.inCombatArena(player, agent)) {
                                ability.run(event, player, agent);
                            }
                        }
                    }
                }
            } else if (event.getDamager() instanceof Player) {
                // TODO: Detect ENTITY_ATTACK & ENTITY_SWEEP_ATTACK
            }
		}
	}

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (PROJECTILE_COMPATIBLE) {
            if (event.getEntity() instanceof ThrowableProjectile && event.getEntity().getShooter() instanceof Player) {
                final ThrowableProjectile projectile = (ThrowableProjectile) event.getEntity();
                final Player player = (Player) projectile.getShooter();
                if (Toolkit.inArena(player)) {
                    for (ItemAbility ability : getAbilities()) {
                        if (ability.isListening(ProjectileLaunchEvent.class) && ability.isItem(projectile.getItem())) {
                            if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true)) {
                                event.setCancelled(true);
                                break;
                            }
                            if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                                ability.run(event, player);
                            }
                        }
                    }
                }
            }
        } else if (event.getEntity().getShooter() instanceof Player) {
            final Player player = (Player) event.getEntity().getShooter();
            final List<ItemAbility> abilities = projectileSent.get(player.getUniqueId());
            if (abilities != null && !abilities.isEmpty()) {
                for (ItemAbility ability : abilities) {
                    ability.run(event, player);
                }
                abilities.clear();
            }
        }
    }

	@EventHandler
	public void onBowShot(EntityShootBowEvent event) {
		if (event.getEntity() instanceof Player && Toolkit.inArena(event.getEntity())) {
			final Player player = (Player) event.getEntity();
            for (ItemAbility ability : getAbilities()) {
                if (ability.isListening(EntityShootBowEvent.class) && ability.activator().test(event.getBow())) {
                    if (!utilities.isCombatActionPermittedInRegion(player) || !Toolkit.isAbilityPlayer(player, true)) {
                        event.setCancelled(true);
                        break;
                    }
                    if (ability.isAllowed(player, true) && ability.isReady(player, true)) {
                        ability.run(event, player);
                    }
                }
            }
		}
	}

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        for (ItemAbility ability : getAbilities()) {
            if (ability.isTagged(event.getPlayer())) {
                ability.close(event, event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        for (ItemAbility ability : getAbilities()) {
            if (ability.isTagged(event.getPlayer())) {
                ability.close(event, event.getPlayer());
            }
        }
    }
}
