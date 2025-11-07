package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.planetgallium.kitpvp.api.util.ItemPredicate;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcherAbility extends ItemAbility {

    private static final XMaterial FIRE_MATERIAL = XMaterial.MAGMA_CREAM;
    private static final XMaterial NO_FIRE_MATERIAL = XMaterial.SLIME_BALL;

    private ItemPredicate fireItem = ItemPredicate.empty();
    private ItemPredicate noFireItem = ItemPredicate.empty();
    private Message fireMessage;
    private Message noFireMessage;

    public ArcherAbility() {
        super(ItemAbility.ARCHER);
    }

    @Override
    public void deserialize(@NotNull ConfigurationSection section) {
        super.deserialize(section);

        // Item
        if (section.isSet("Item")) {
            this.fireItem = new ItemPredicate(FIRE_MATERIAL, Toolkit.translate(section.getString("Item.Fire")));
            this.noFireItem = new ItemPredicate(NO_FIRE_MATERIAL, Toolkit.translate(section.getString("Item.NoFire")));
        } else {
            this.fireItem = ItemPredicate.empty();
            this.noFireItem = ItemPredicate.empty();
        }

        // Message
        if (section.isSet("Message")) {
            final boolean messageEnabled = message() == null || message().enabled();
            this.fireMessage = new Message(
                    messageEnabled,
                    Toolkit.translate(section.getString("Message.Fire")),
                    null,
                    -1
            );
            this.noFireMessage = new Message(
                    messageEnabled,
                    Toolkit.translate(section.getString("Message.NoFire")),
                    null,
                    -1
            );
        } else {
            this.fireMessage = null;
            this.noFireMessage = null;
        }
    }

    @Override
    public void serialize(@NotNull ConfigurationSection section) {
        super.serialize(section);

        if (this.fireMessage != null) {
            section.set("Message.Fire", this.fireMessage.self());
        }

        if (this.noFireMessage != null) {
            section.set("Message.NoFire", this.noFireMessage.self());
        }
    }

    @NotNull
    public ItemPredicate fireItem() {
        return fireItem;
    }

    @NotNull
    public ItemPredicate noFireItem() {
        return noFireItem;
    }

    @Nullable
    public Message fireMessage() {
        return fireMessage;
    }

    @Nullable
    public Message noFireMessage() {
        return noFireMessage;
    }

    @Override
    public boolean isItem(@NotNull ItemStack item) {
        return fireItem.test(item) || noFireItem.test(item);
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final Material newType;
        final String newName;
        final Message message;
        if (XMaterial.matchXMaterial(item) == NO_FIRE_MATERIAL) {
            newType = FIRE_MATERIAL.get();
            newName = this.fireItem.name();
            message = this.fireMessage;
        } else {
            newType = NO_FIRE_MATERIAL.get();
            newName = this.noFireItem.name();
            message = this.noFireMessage;
        }

        item.setType(newType);
        if (newName != null) {
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(newName);
            item.setItemMeta(meta);
        }

        Toolkit.setHandItemForInteraction(event, item);

        if (message != null) {
            message.send(player);
        }

        XSound.UI_BUTTON_CLICK.play(player);
    }
}
