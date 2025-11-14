package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class RocketAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.FIREWORK_ROCKET;

    private double boost = 1.5;

    public RocketAbility() {
        super(ItemAbility.ROCKET);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.boost = section.getDouble("boost", 1.5);
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("boost", this.boost);
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final Vector boost = player.getLocation().getDirection().multiply(this.boost);
        player.setVelocity(boost);
        player.setFallDistance(config().getBoolean("Arena.PreventFallDamage") ? -1000000 : -30);

        use(event, player, item);
    }
}
