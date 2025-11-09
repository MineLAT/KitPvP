package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SoldierAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.IRON_HOE;

    public SoldierAbility() {
        super(ItemAbility.SOLDIER);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final Snowball ammo = player.launchProjectile(Snowball.class);
        metadata(ammo);
        ammo.setVelocity(player.getLocation().getDirection().multiply(2.5));

        use(event, player, item);
    }

    @Override
    public void run(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull Player agent) {
        agent.damage(4.5);
    }
}
