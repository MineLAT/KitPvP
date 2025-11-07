package com.planetgallium.kitpvp.api.util;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public class ItemPredicate implements Predicate<ItemStack> {

    private static final ItemPredicate EMPTY = new ItemPredicate(XMaterial.AIR) {
        @Override
        public boolean test(ItemStack item) {
            return false;
        }
    };

    @NotNull
    public static ItemPredicate empty() {
        return EMPTY;
    }

    @NotNull
    public static ItemPredicate valueOf(@NotNull ItemStack item) {
        final XMaterial material = XMaterial.matchXMaterial(item);
        String name = null;

        if (item.hasItemMeta()) {
            final ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName()) {
                name = meta.getDisplayName();
            }
        }

        return new ItemPredicate(material, name);
    }

    @NotNull
    public static Optional<ItemPredicate> valueOf(@Nullable ConfigurationSection section) {
        if (section == null) {
            return Optional.empty();
        }
        return XMaterial.matchXMaterial(section.getString("Material", "")).map(material -> valueOf(material, section));
    }

    @NotNull
    public static ItemPredicate valueOf(@NotNull XMaterial material, @Nullable ConfigurationSection section) {
        if (material == XMaterial.AIR) {
            return empty();
        }

        String name = null;

        if (section != null) {
            if (section.isSet("Name")) {
                name = Toolkit.translate(section.getString("Name"));
            }
        }

        return new ItemPredicate(material, name);
    }

    private final XMaterial material;
    private final String name;

    public ItemPredicate(@NotNull XMaterial material) {
        this(material, null);
    }

    public ItemPredicate(@NotNull XMaterial material, @Nullable String name) {
        this.material = material;
        this.name = name;
    }

    @Override
    public boolean test(ItemStack item) {
        if (item == null) {
            return false;
        }
        if (XMaterial.matchXMaterial(item) != material) {
            return false;
        }
        if (item.hasItemMeta()) {
            if (name != null) {
                final ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && !name.equals(meta.getDisplayName())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    public Integer slot(@NotNull Player player) {
        int inventorySize = Toolkit.versionToNumber() == 18 ? 39 : 45;
        for (int i = 0; i <= inventorySize; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (test(item)) {
                return i;
            }
        }
        return null;
    }

    @NotNull
    public XMaterial material() {
        return material;
    }

    @Nullable
    public String name() {
        return name;
    }
}
