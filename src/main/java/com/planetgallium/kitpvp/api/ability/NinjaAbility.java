package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.api.util.Timespan;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class NinjaAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.NETHER_STAR;

    private Timespan duration = Timespan.valueOf(5, TimeUnit.SECONDS);

    public NinjaAbility() {
        super(ItemAbility.NINJA);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.duration = Timespan.valueOf(section.get("duration")).orElseGet(() -> Timespan.valueOf(5, TimeUnit.SECONDS));
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("duration", this.duration.as(Timespan.CONFIG_FORMAT));
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final ItemStack previousHelmet = player.getInventory().getHelmet();
        final ItemStack previousChestplate = player.getInventory().getChestplate();
        final ItemStack previousLeggings = player.getInventory().getLeggings();
        final ItemStack previousBoots = player.getInventory().getBoots();

        player.getInventory().setArmorContents(null);

        final int ticks = (int) this.duration.toTicks();

        for (Entity entity : player.getNearbyEntities(3, 3, 3)) {
            if (entity instanceof Player) {
                Player nearby = (Player) entity;
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, ticks, 0));
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, 0));
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ticks, 0));

        use(event, player, item);

        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            if (arena().getKits().playerHasKit(player.getUniqueId())) {
                player.getInventory().setHelmet(previousHelmet);
                player.getInventory().setChestplate(previousChestplate);
                player.getInventory().setLeggings(previousLeggings);
                player.getInventory().setBoots(previousBoots);
            }
        }, ticks);
    }
}