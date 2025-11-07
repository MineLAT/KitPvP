package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ThunderboltAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.BLAZE_ROD;

    public ThunderboltAbility() {
        super(ItemAbility.THUNDERBOLT);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void run(@NotNull PlayerInteractEntityEvent event, @NotNull Player player, @NotNull Player agent, @NotNull ItemStack item) {
        player.getWorld().strikeLightningEffect(event.getRightClicked().getLocation());
        agent.damage(4.0);
        agent.setFireTicks(5 * 20);

        use(event, player, agent, item);
    }
}