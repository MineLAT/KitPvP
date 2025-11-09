package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.api.util.Timespan;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class ThunderboltAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.BLAZE_ROD;

    private double damage = 4.0;
    private Timespan fireDuration = Timespan.valueOf(5, TimeUnit.SECONDS);

    public ThunderboltAbility() {
        super(ItemAbility.THUNDERBOLT);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.damage = section.getDouble("damage", 4.0);
        this.fireDuration = Timespan.valueOf(section.get("fire-duration")).orElseGet(() -> Timespan.valueOf(5, TimeUnit.SECONDS));
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("damage", this.damage);
        section.set("fire-duration", this.fireDuration.as(Timespan.CONFIG_FORMAT));
    }

    @Override
    public void run(@NotNull PlayerInteractEntityEvent event, @NotNull Player player, @NotNull Player agent, @NotNull ItemStack item) {
        player.getWorld().strikeLightningEffect(event.getRightClicked().getLocation());
        agent.damage(this.damage);
        agent.setFireTicks((int) this.fireDuration.toTicks());

        use(event, player, agent, item);
    }
}