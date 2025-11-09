package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class KangarooAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.SADDLE;

    private int power = 2;

    public KangarooAbility() {
        super(ItemAbility.KANGAROO);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        this.power = section.getInt("power", 2);
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        section.set("power", this.power);
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        player.setVelocity(new Vector(0, this.power, 0));
        player.setFallDistance(config().getBoolean("Arena.PreventFallDamage") ? -1000000 : -30);

        use(event, player, item);
    }
}
