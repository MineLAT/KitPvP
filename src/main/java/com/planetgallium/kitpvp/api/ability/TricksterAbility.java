package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
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
}
