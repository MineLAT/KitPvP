package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.Ability;
import com.planetgallium.kitpvp.api.util.ItemPredicate;
import com.planetgallium.kitpvp.game.Arena;
import com.planetgallium.kitpvp.util.Resource;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ItemAbility extends Ability {

    public static final String ARCHER = "Archer";
    public static final String BOMBER = "Bomber";
    public static final String KANGAROO = "Kangaroo";
    public static final String NINJA = "Ninja";
    public static final String SOLDIER = "Soldier";
    public static final String THUNDERBOLT = "Thunderbolt";
    public static final String TRICKSTER = "Trickster";
    public static final String VAMPIRE = "Vampire";
    public static final String WARPER = "Warper";
    public static final String WITCH = "Witch";

    private int itemAmount = 0;

    public ItemAbility(@NotNull String name) {
        super(name);
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        // Item
        if (section.isSet("Item")) {
            this.activator = ItemPredicate.valueOf(type(), section.getConfigurationSection("Item"));

            this.itemAmount = section.getInt("Item.Amount", 0);
        } else {
            this.activator = ItemPredicate.empty();
            this.itemAmount = 0;
        }
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        section.set("Item.Name", Toolkit.toNormalColorCodes(this.activator.name()));
        section.set("Item.Amount", this.itemAmount);

        super.serialize(section);
        section.set("Activator", null);
    }

    @NotNull
    protected XMaterial type() {
        return XMaterial.AIR;
    }

    public int itemAmount() {
        return itemAmount;
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        // empty method
    }

    protected void use(@NotNull Event event, @NotNull Player player, @NotNull ItemStack item) {
        use(event, player, null, item);
    }

    protected void use(@NotNull Event event, @NotNull Player player, @Nullable Player agent, @NotNull ItemStack item) {
        item.setAmount(item.getAmount() - 1);
        Toolkit.setHandItemForInteraction(event, item);

        run(player, agent);
    }



    // TODO: Remove this methods

    @NotNull
    protected Game plugin() {
        return Game.getInstance();
    }

    @NotNull
    protected Arena arena() {
        return Game.getInstance().getArena();
    }

    @NotNull
    protected Resource config() {
        return Game.getInstance().getResources().getConfig();
    }
}
