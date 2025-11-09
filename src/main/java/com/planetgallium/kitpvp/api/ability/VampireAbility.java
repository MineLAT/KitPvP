package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class VampireAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.GHAST_TEAR;

    private double damage = 4.0;

    public VampireAbility() {
        super(ItemAbility.VAMPIRE);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.damage = section.getDouble("damage", 4.0);
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("damage", this.damage);
    }

    @Override
    public void run(@NotNull PlayerInteractEntityEvent event, @NotNull Player player, @NotNull Player agent, @NotNull ItemStack item) {
        agent.damage(this.damage);
        XSound.ENTITY_GENERIC_DRINK.play(agent, 1.0f, -1f);

        if (player.getHealth() <= (20.0 - this.damage)) {
            player.setHealth(player.getHealth() + this.damage);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 2, 1));
        }

        use(event, player, agent, item);
    }
}
