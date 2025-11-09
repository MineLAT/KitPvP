package com.planetgallium.kitpvp.api;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.planetgallium.kitpvp.Game;
import com.planetgallium.kitpvp.api.util.ItemPredicate;
import com.planetgallium.kitpvp.api.util.Cooldown;
import com.planetgallium.kitpvp.util.Toolkit;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public class Ability {

    public static final String META_KEY = "kitpvp:ability";

    @NotNull
    public static Ability sample(@NotNull String prefix) {
        return new Ability(
                prefix + "-Blank",
                new ItemPredicate(XMaterial.EMERALD),
                Cooldown.ZERO,
                new Message(true, "%prefix% &7You have used your ability.", null, 0),
                XSound.BLOCK_NOTE_BLOCK_PLING.record(),
                Collections.singletonList(Toolkit.parsePotionEffect(XPotion.SPEED, 1, 10)),
                Arrays.asList(
                        "console: This command is run from the console, you can use %player%",
                        "player: This command is run from the player, you can use %player%"
                )
        );
    }

    private final String name;
    protected ItemPredicate activator;
    protected Cooldown cooldown;
    protected Message message;
    protected XSound.Record sound;
    protected final List<PotionEffect> effects;
    protected final List<String> commands;

    private Set<Class<? extends Event>> listeners;

    public Ability(@NotNull String name) {
        this.name = name;
        this.activator = ItemPredicate.empty();
        this.cooldown = Cooldown.ZERO;
        this.effects = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    public Ability(@NotNull String name, @NotNull ItemPredicate activator, @Nullable Cooldown cooldown, @Nullable Message message, @Nullable XSound.Record sound, @NotNull List<PotionEffect> effects, @NotNull List<String> commands) {
        this.name = name;
        this.activator = activator;
        this.cooldown = cooldown;
        this.message = message;
        this.sound = sound;
        this.effects = effects;
        this.commands = commands;
    }

    @ApiStatus.Internal
    public void deserialize(@NotNull ConfigurationSection section) {
        // Activator
        if (section.isSet("Activator")) {
            this.activator = ItemPredicate.valueOf(section.getConfigurationSection("Activator")).orElse(ItemPredicate.empty());
        } else {
            this.activator = ItemPredicate.empty();
        }

        // Cooldown
        if (section.isSet("Cooldown")) {
            this.cooldown = Cooldown.valueOf(section.get("Cooldown")).orElse(Cooldown.ZERO);
        } else {
            this.cooldown = Cooldown.ZERO;
        }

        // Message
        if (section.isSet("Message")) {
            final boolean enabled = section.getBoolean("Message.Enabled", false);
            final String self = Toolkit.translate(section.getString("Message.Message"));
            final String broadcast = Toolkit.translate(section.getString("Message.Broadcast"));
            final int range = section.getInt("Message.Range", 5);

            this.message = new Ability.Message(enabled, self, broadcast, range);
        } else {
            this.message = null;
        }

        // Sound
        if (section.isSet("Sound") && section.getBoolean("Sound.Enabled", true)) {
            final String category = section.getString("Sound.Category", null);
            final String soundName = section.getString("Sound.Sound");
            final double pitch = section.getDouble("Sound.Pitch", 1.0d);
            final double volume = section.getDouble("Sound.Volume", 1.0d);
            final long seed = section.getLong("Sound.Seed", Long.MIN_VALUE);

            this.sound = XSound.parse((category == null ? "" : category + "@") + soundName + ", " + pitch + ", " + volume + (seed == Long.MIN_VALUE ? "" : ", " + seed));
        } else {
            this.sound = null;
        }

        // Effects
        if (section.isSet("Effects")) {
            ConfigurationSection effectSection = section.getConfigurationSection("Effects");

            for (String effectName : effectSection.getKeys(false)) {
                XPotion potion = XPotion.of(effectName).get();
                int amplifier = section.getInt("Effects." + effectName + ".Amplifier");
                int duration = section.getInt("Effects." + effectName + ".Duration");

                this.effects.add(Toolkit.parsePotionEffect(potion, amplifier, duration));
            }
        } else {
            this.effects.clear();
        }

        // Commands
        if (section.isSet("Commands")) {
            this.commands.addAll(section.getStringList("Commands"));
        } else {
            this.commands.clear();
        }
    }

    @ApiStatus.Internal
    public void serialize(@NotNull ConfigurationSection section) {
        section.set("Activator.Material", activator.material().name());
        section.set("Activator.Name", Toolkit.toNormalColorCodes(activator.name()));
        if (cooldown != Cooldown.ZERO) {
            section.set("Cooldown.Cooldown", cooldown.as(Cooldown.CONFIG_FORMAT));
        }
        if (message != null) {
            section.set("Message.Enabled", message.enabled());
            section.set("Message.Message", Toolkit.toNormalColorCodes(message.self()));
            if (message.broadcast() != null) {
                section.set("Message.Broadcast", Toolkit.toNormalColorCodes(message.broadcast()));
                section.set("Message.Range", message.range());
            }
        }
        if (sound != null) {
            if (sound.getCategory() != XSound.Category.MASTER) {
                section.set("Sound.Category", sound.getCategory().name());
            }
            section.set("Sound.Sound", sound.std());
            section.set("Sound.Pitch", sound.getPitch() != 1 ? sound.getPitch() : null);
            section.set("Sound.Volume", sound.getVolume() != 1 ? sound.getVolume() : null);
            if (sound.getSeed() != null) {
                section.set("Sound.Seed", sound.getSeed());
            }
        }

        for (PotionEffect effect : effects) {
            String type = XPotion.of(effect.getType()).name();
            int amplifierNonZeroBased = effect.getAmplifier() + 1;
            int durationSeconds = effect.getDuration() / 20;

            section.set("Effects." + type + ".Amplifier", amplifierNonZeroBased);
            section.set("Effects." + type + ".Duration", durationSeconds);
        }

        section.set("Commands", commands.toArray());
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public ItemPredicate activator() {
        return activator;
    }

    @NotNull
    public Cooldown cooldown() {
        return cooldown;
    }

    @Nullable
    public Message message() {
        return message;
    }

    @Nullable
    public XSound.Record sound() {
        return sound;
    }

    @NotNull
    public List<PotionEffect> effects() {
        return Collections.unmodifiableList(effects);
    }

    @NotNull
    public List<String> commands() {
        return Collections.unmodifiableList(commands);
    }

    public boolean isListening(@NotNull Class<?> event) {
        if (this.listeners == null) {
            this.listeners = new HashSet<>();
            if (getClass() == Ability.class) {
                this.listeners.add(PlayerInteractEvent.class);
            } else {
                for (Method method : getClass().getDeclaredMethods()) {
                    if (method.getName().equals("run") && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                        this.listeners.add(method.getParameterTypes()[0].asSubclass(Event.class));
                    }
                }
            }
        }
        return this.listeners.contains(event);
    }

    public boolean isItem(@NotNull ItemStack item) {
        return activator.test(item);
    }

    public boolean isAllowed(@NotNull Player player, boolean message) {
        final String permission = "kp.ability." + this.name.toLowerCase();
        if (!player.hasPermission(permission)) {
            if (message) {
                player.sendMessage(Game.getInstance().getResources().getMessages().fetchString("Messages.General.Permission").replace("%permission%", permission));
            }
            return false;
        }

        return true;
    }

    public boolean isReady(@NotNull Player player, boolean message) {
        if (this.cooldown == Cooldown.ZERO) {
            return true;
        }

        final Cooldown cooldownRemaining = Game.getInstance().getArena().getCooldowns().getRemainingCooldown(player, this);
        if (cooldownRemaining.toMillis() > 0) {
            if (message) {
                player.sendMessage(Game.getInstance().getResources().getMessages().fetchString("Messages.Error.CooldownAbility").replace("%cooldown%", cooldownRemaining.as(Cooldown.READABLE_FORMAT)));
            }
            return false;
        }

        return true;
    }

    public void run(@NotNull Player player) {
        run(player, null, s -> Toolkit.translate(player, s));
    }

    public void run(@NotNull Player player, @Nullable Player agent) {
        run(player, agent, s -> {
            if (agent != null) {
                s = s.replace("%player%", agent.getName());
            }
            return Toolkit.translate(player, s);
        });
    }

    public void run(@NotNull Player player, @Nullable Player agent, @NotNull UnaryOperator<String> parser) {
        if (this.message != null) {
            this.message.send(player, parser);
        }

        if (this.sound != null) {
            this.sound.soundPlayer().play(Collections.singletonList(player), player.getLocation());
            if (agent != null) {
                this.sound.soundPlayer().play(Collections.singletonList(agent), agent.getLocation());
            }
        }

        if (!this.effects.isEmpty()) {
            this.effects.forEach(player::addPotionEffect);
        }

        if (!this.commands.isEmpty()) {
            Toolkit.runCommands(player, this.commands, "none", "none");
        }
    }

    public void run(@NotNull PlayerInteractEvent event, @NotNull Player player, @NotNull ItemStack item) {
        final PlayerAbilityEvent abilityEvent = new PlayerAbilityEvent(player, this, event);
        Bukkit.getPluginManager().callEvent(abilityEvent);
        if (abilityEvent.isCancelled()) {
            return;
        }

        event.setCancelled(true);

        if (isAllowed(player, true) && isReady(player, true)) {
            run(player);

            if (this.cooldown == Cooldown.ZERO) {
                item.setAmount(item.getAmount() - 1);
            } else {
                cooldown(player);
            }
        }
    }

    public void run(@NotNull PlayerInteractEntityEvent event, @NotNull Player player, @NotNull Player agent, @NotNull ItemStack item) {
        // empty method
    }

    public void run(@NotNull ProjectileLaunchEvent event, @NotNull Player player) {
        // empty method
    }

    public void run(@NotNull EntityShootBowEvent event, @NotNull Player player) {
        // empty method
    }

    public void run(@NotNull EntityDamageByEntityEvent event, @NotNull Player player, @NotNull Player agent) {
        // empty method
    }

    protected void cooldown(@NotNull Player player) {
        if (this.cooldown != Cooldown.ZERO) {
            Game.getInstance().getArena().getCooldowns().setAbilityCooldown(player.getUniqueId(), this);
        }
    }

    protected void metadata(@NotNull Metadatable metadatable) {
        metadatable.setMetadata(META_KEY, new FixedMetadataValue(Game.getInstance(), this));
    }

    public static class Message {

        private final boolean enabled;
        private final String self;
        private final String broadcast;
        private final int range;

        public Message(boolean enabled, @Nullable String self, @Nullable String broadcast, int range) {
            this.enabled = enabled;
            this.self = self;
            this.broadcast = broadcast;
            this.range = range;
        }

        public boolean enabled() {
            return enabled;
        }

        @Nullable
        public String self() {
            return self;
        }

        @Nullable
        public String broadcast() {
            return broadcast;
        }

        public int range() {
            return range;
        }

        public void send(@NotNull Player player) {
            send(player, s -> Toolkit.translate(player, s));
        }

        public void send(@NotNull Player player, @NotNull UnaryOperator<String> parser) {
            if (!this.enabled) {
                return;
            }

            if (this.self != null) {
                player.sendMessage(parser.apply(this.self));
            }

            if (this.broadcast != null) {
                final String msg = parser.apply(this.broadcast);
                if (this.range > 0) {
                    for (Entity entity : player.getNearbyEntities(this.range, this.range, this.range)) {
                        if (entity instanceof Player) {
                            entity.sendMessage(msg);
                        }
                    }
                } else {
                    for (Entity entity : player.getWorld().getEntities()) {
                        if (entity instanceof Player) {
                            entity.sendMessage(msg);
                        }
                    }
                }
            }
        }
    }
}
