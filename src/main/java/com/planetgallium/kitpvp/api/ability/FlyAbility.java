package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.api.util.Timespan;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FlyAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.FEATHER;

    private double jump = 0.5;
    private Timespan duration = Timespan.valueOf(3, TimeUnit.SECONDS);

    private transient final Set<UUID> flyAllowed = new HashSet<>();

    public FlyAbility() {
        super(ItemAbility.FLY);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.jump = section.getDouble("jump", 0.5);
        this.duration = Timespan.valueOf(section.get("duration")).orElseGet(() -> Timespan.valueOf(3, TimeUnit.SECONDS));
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("jump", this.jump);
        section.set("duration", this.duration.as(Timespan.CONFIG_FORMAT));
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        if (jump > 0) {
            player.setVelocity(new Vector(0, this.jump, 0));
        }
        if (!player.getAllowFlight()) {
            this.flyAllowed.add(player.getUniqueId());
            player.setAllowFlight(true);
        }
        player.setFlying(true);
        tag(player);
        use(event, player, item);

        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            if (untag(player)) {
                if (this.flyAllowed.remove(player.getUniqueId())) {
                    player.setFlying(false);
                }
                player.setFallDistance(config().getBoolean("Arena.PreventFallDamage") ? -1000000 : -30);
            }
        }, this.duration.toTicks());
    }

    @Override
    public void close(@NotNull Event event, @NotNull Player player) {
        if (this.flyAllowed.remove(player.getUniqueId())) {
            player.setFlying(false);
        }
        untag(player);
    }
}
