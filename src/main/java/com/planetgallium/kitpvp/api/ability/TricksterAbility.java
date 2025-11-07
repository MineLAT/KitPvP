package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void run(@NotNull Player player, @Nullable Player agent) {
        super.run(player, agent);
        if (agent != null && this.message != null) {
            this.message.send(agent, s -> Toolkit.translate(agent, s.replace("%player%", player.getName())));
        }
    }
}
