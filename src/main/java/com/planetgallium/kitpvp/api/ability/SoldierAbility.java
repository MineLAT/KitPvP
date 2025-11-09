package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoldierAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.IRON_HOE;

    private double velocity = 2.5;
    private double damage = 4.5;

    public SoldierAbility() {
        super(ItemAbility.SOLDIER);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.velocity = section.getDouble("velocity", 2.5);
        this.damage = section.getDouble("damage", 4.5);
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("velocity", this.velocity);
        section.set("damage", this.damage);
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final Snowball ammo = player.launchProjectile(Snowball.class);
        metadata(ammo);
        ammo.setVelocity(player.getLocation().getDirection().multiply(this.velocity));

        use(event, player, item);
    }

    @Override
    public void run(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull Player agent) {
        agent.damage(this.damage);
    }
}
