package com.planetgallium.kitpvp.api.ability;

import com.cryptomorin.xseries.XMaterial;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class WarperAbility extends ItemAbility {

    private static final XMaterial MATERIAL = XMaterial.ENDER_EYE;

    public WarperAbility() {
        super(ItemAbility.WARPER);
    }

    @Override
    protected @NotNull XMaterial type() {
        return MATERIAL;
    }

    @Override
    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final Player nearestPlayer = Toolkit.getNearestPlayer(player, config().getInt("PlayerTracker.TrackBelowY"));

        if (Bukkit.getServer().getOnlinePlayers().size() > 1 && nearestPlayer != null) {
            if (arena().getKits().playerHasKit(nearestPlayer.getUniqueId())) {
                Bukkit.getScheduler().runTaskLater(plugin(), () -> player.teleport(nearestPlayer.getLocation()), 5L);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 5));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 5));

                use(event, player, item);
            } else {
                player.sendMessage(plugin().getResources().getMessages().fetchString("Messages.Other.Players"));
            }
        } else {
            player.sendMessage(plugin().getResources().getMessages().fetchString("Messages.Other.Players"));
        }
    }
}
