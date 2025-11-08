package com.planetgallium.kitpvp.api.util;

import com.planetgallium.kitpvp.Game;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class Cooldown {

    public static final Cooldown ZERO = new Cooldown(Duration.ofMillis(0));
    public static final ChronoFormat READABLE_FORMAT = new ChronoFormat(Arrays.asList(
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    ), " ", Integer.MAX_VALUE) {
        @Override
        public @NotNull String format(long amount, @NotNull ChronoUnit unit) {
            final boolean singular = amount == 1 || amount == -1;
            final String path = "duration.unit." + unit.name().toLowerCase() + "." + (singular ? "singular" : "plural");
            return amount + " " + Game.getInstance().getResources().getMessages().getString(path, singular ? ChronoResource.SINGULAR.get(unit) : ChronoResource.PLURAL.get(unit));
        }
    };
    public static final ChronoFormat CONFIG_FORMAT = new ChronoFormat(Arrays.asList(
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS,
            ChronoUnit.MILLIS
    ), " and ", Integer.MAX_VALUE);

    @NotNull
    public static Optional<Cooldown> valueOf(@Nullable Object object) {
        if (object instanceof ConfigurationSection) {
            final ConfigurationSection section = (ConfigurationSection) object;
            if (section.isSet("Cooldown")) {
                return valueOf(section.getString("Cooldown"));
            }
            final StringJoiner joiner = new StringJoiner(" AND ");
            for (String unit : section.getKeys(false)) {
                final long amount = section.getLong(unit);
                if (amount > 0) {
                    joiner.add(amount + " " + unit);
                }
            }
            return valueOf(joiner.toString());
        } else if (object instanceof String) {
            final String s = (String) object;
            if (s.trim().isEmpty()) {
                return Optional.empty();
            }
            if (s.trim().equals("0")) {
                return Optional.of(ZERO);
            }
            return Optional.of(new Cooldown(s));
        } else if (object instanceof Number) {
            return Optional.of(new Cooldown(Duration.ofMillis(((Number) object).longValue())));
        }
        return Optional.empty();
    }

    private final Duration duration;

    public Cooldown(@NotNull Duration duration) {
        this.duration = duration;
    }
	
	public Cooldown(int seconds) {
		this.duration = Duration.ofSeconds(seconds);
	}

	public Cooldown(@NotNull String s) {
        // Old format compatibility
        if (s.contains(":")) {
            long millis = 0;

            for (String part : s.split(":")) {
                final long time = Long.parseLong(part.substring(0, part.length() - 1));
                switch (part.charAt(part.length() - 1)) {
                    case 'D':
                        millis += TimeUnit.DAYS.toMillis(time);
                        break;
                    case 'H':
                        millis += TimeUnit.HOURS.toMillis(time);
                        break;
                    case 'M':
                        millis += TimeUnit.MINUTES.toMillis(time);
                        break;
                    case 'S':
                        millis += TimeUnit.SECONDS.toMillis(time);
                        break;
                    default:
                        break;
                }
            }

            this.duration = Duration.ofMillis(millis);
        } else {
            this.duration = ChronoFormat.parse(s);
        }
	}

    @NotNull
    public String as(@NotNull ChronoFormat format) {
        return format.format(duration);
    }

	public long toSeconds() {
		return duration.getSeconds();
	}
}
