package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.jetbrains.annotations.NotNull;

public class TricksterAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.EGG;

    public TricksterAbility() {
        super(ItemAbility.TRICKSTER);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void run(@NotNull ProjectileLaunchEvent event, @NotNull Player player) {
        metadata(event.getEntity());
    }

    @Override
    public void run(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull Player agent) {
        final Location location = player.getLocation();

        player.teleport(agent);
        agent.teleport(location);

        run(player, agent);
        if (this.message != null) {
            this.message.send(agent, s -> Toolkit.translate(agent, s.replace("%player%", player.getName())));
        }

        cooldown(player);
    }
}
