package com.planetgallium.kitpvp.game;

import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Resources;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Abilities {

    private final Resources resources;
    private final List<Ability> loaded;

    public Abilities(Game plugin) {
        this.resources = plugin.getResources();
        this.loaded = new ArrayList<>();

        rebuildCache();
    }

    public void rebuildCache() {
        for (Resource abilityResource : resources.getAbilityResources()) {
            loaded.add(getAbilityFromResource(abilityResource));
        }
    }

    public Ability getAbilityByActivator(ItemStack potentialActivator) {
        for (Ability ability : loaded) {
            if (ability.activator().test(potentialActivator)) {
                return ability;
            }
        }
        return null;
    }

    private Ability getAbilityFromResource(Resource resource) {
        try {
            final Ability ability = new Ability(resource.getName());
            ability.deserialize(resource);
            return ability;
        } catch (Throwable t) {
            return null;
        }
    }
}
