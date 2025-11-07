package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class VampireAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.GHAST_TEAR;

    public VampireAbility() {
        super(ItemAbility.VAMPIRE);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void run(@NotNull PlayerInteractEntityEvent event, @NotNull Player player, @NotNull Player agent, @NotNull ItemStack item) {
        agent.damage(4.0);
        XSound.ENTITY_GENERIC_DRINK.play(agent, 1.0f, -1f);

        if (player.getHealth() <= 16.0) {
            player.setHealth(player.getHealth() + 4.0);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 2, 1));
        }

        use(event, player, agent, item);
    }
}
