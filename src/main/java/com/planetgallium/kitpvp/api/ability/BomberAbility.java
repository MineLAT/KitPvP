package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BomberAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.COAL;

    private int amount = 5;

    public BomberAbility() {
        super(ItemAbility.BOMBER);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.amount = section.getInt("amount", 5);
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("amount", this.amount);
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, this.amount * 20, 2));

        new BukkitRunnable() {
            public int iterations = BomberAbility.this.amount;

            @Override
            public void run() {
                if (iterations != 0 && player.getGameMode() != GameMode.SPECTATOR && arena().getKits().playerHasKit(player.getUniqueId())) {
                    Entity entity = player.getWorld().spawn(player.getLocation(), TNTPrimed.class);
                    entity.setCustomName(player.getName());

                    if (BomberAbility.this.sound != null) {
                        BomberAbility.this.sound.soundPlayer().play(player.getLocation());
                    }

                    iterations--;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin(), 0L, 20L);

        use(event, player, item);
    }
}