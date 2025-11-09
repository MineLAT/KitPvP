package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.api.Kit;
import com.planetgallium.kitpvp.util.CacheManager;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class WitchAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.GLASS_BOTTLE;

    public WitchAbility() {
        super(ItemAbility.WITCH);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    public boolean isPotionItem(@NotNull ItemStack item) {
        return XMaterial.matchXMaterial(item) == XMaterial.SPLASH_POTION && Toolkit.singleLineLoreMatches(item, "X");
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        event.setCancelled(true);
        CacheManager.getPotionSwitcherUsers().add(player.getUniqueId());
        Toolkit.setHandItemForInteraction(event, createWitchPotion());
    }

    public void runSplash(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        Toolkit.SlotWrapper slotUsed = Toolkit.getSlotUsedForInteraction(event);
        ItemStack randomizedWitchPotion = createWitchPotion();

        Bukkit.getScheduler().runTaskLater(plugin(), () -> {
            Kit playerKit = arena().getKits().getKitOfPlayer(player.getUniqueId());
            if (playerKit != null && CacheManager.getPotionSwitcherUsers().contains(player.getUniqueId())) {
                slotUsed.placeItemInSlot(player, randomizedWitchPotion);

                run(player);

                CacheManager.getPotionSwitcherUsers().add(player.getUniqueId());
            }
        }, 100L);

        cooldown(player);
    }

    @NotNull
    public static ItemStack createWitchPotion() {
        PotionType randomPotionType = randomPotionType();

        if (Toolkit.versionToNumber() >= 121) { // 1.21+
            ItemStack potionStack = new ItemStack(Material.SPLASH_POTION);
            Toolkit.appendToLore(potionStack, "X");
            PotionMeta potionMeta = (PotionMeta) potionStack.getItemMeta();

            if (potionMeta != null) {
                potionMeta.setBasePotionType(randomPotionType);
                potionStack.setItemMeta(potionMeta);
            }

            return potionStack;
        } else { // pre 1.21
            Potion potion = new Potion(randomPotionType, 1);
            potion.setSplash(true);

            ItemStack potionStack = potion.toItemStack(1);
            Toolkit.appendToLore(potionStack, "X");

            return potionStack;
        }
    }

    @NotNull
    public static PotionType randomPotionType() {
        PotionType potion = null;

        Random ran = new Random();
        int chance = ran.nextInt(100);

        if (chance < 10) {
            potion = PotionType.INSTANT_DAMAGE;
        } else if (chance < 20) {
            potion = PotionType.INSTANT_HEAL;
        } else if (chance < 40) {
            potion = PotionType.POISON;
        } else if (chance < 60) {
            potion = PotionType.REGEN;
        } else if (chance < 80) {
            potion = PotionType.SPEED;
        } else if (chance < 100) {
            potion = PotionType.SLOWNESS;
        }
        return potion;
    }
}
