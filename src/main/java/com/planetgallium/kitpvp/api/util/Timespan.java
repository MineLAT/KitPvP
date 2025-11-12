package com.planetgallium.kitpvp.api.util;

import com.planetgallium.kitpvp.Game;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class Timespan {

    public static final Timespan ZERO = new Timespan(0L);

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#");
    public static final ChronoFormat READABLE_FORMAT = new ChronoFormat(Arrays.asList(
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    ), " ", Integer.MAX_VALUE) {
        @Override
        public @NotNull String format(double amount, @NotNull ChronoUnit unit) {
            final String formatted;
            final boolean singular;
            if (unit == ChronoUnit.SECONDS) {
                formatted = DECIMAL_FORMAT.format(amount);
                singular = amount == 1 || amount == -1;
            } else {
                final long num = (long) amount;
                formatted = String.valueOf(num);
                singular = num == 1 || num == -1;
            }
            final String path = "duration.unit." + unit.name().toLowerCase() + "." + (singular ? "singular" : "plural");
            return formatted + " " + Game.getInstance().getResources().getMessages().getString(path, singular ? ChronoResource.SINGULAR.get(unit) : ChronoResource.PLURAL.get(unit));
        }
    };
    public static final ChronoFormat CONFIG_FORMAT = new ChronoFormat(Arrays.asList(
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS,
            ChronoUnit.MILLIS
    ), " and ", Integer.MAX_VALUE) {
        @Override
        public long getLength(@NotNull Duration duration) {
            return duration.toMillis();
        }
    };

    private static boolean isOldFormat(@NotNull String s) {
        if (s.contains(":")) {
            return true;
        } else if (!s.contains(" ")) {
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isDigit(s.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    public static Optional<Timespan> valueOf(@Nullable Object object) {
        if (object instanceof ConfigurationSection) {
            final ConfigurationSection section = (ConfigurationSection) object;

            // Old format compatibility
            if (section.isSet("Cooldown")) {
                return valueOf((Object) section.getString("Cooldown"));
            }

            final StringJoiner joiner = new StringJoiner(" AND ");
            for (String unit : section.getKeys(false)) {
                final long amount = section.getLong(unit);
                if (amount > 0) {
                    joiner.add(amount + " " + unit);
                }
            }

            return valueOf((Object) joiner.toString());
        } else if (object instanceof String) {
            final String s = (String) object;
            if (s.trim().isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(valueOf(s));
        } else if (object instanceof Number) {
            return Optional.of(valueOf(((Number) object).longValue()));
        }
        return Optional.empty();
    }

    @NotNull
    public static Timespan valueOf(@NotNull String s) {
        s = s.trim();
        if (s.isEmpty() || s.equals("0")) {
            return ZERO;
        }

        // Old format compatibility
        if (isOldFormat(s)) {
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

            return valueOf(millis);
        }

        return valueOf(ChronoFormat.parse(s));
    }

    @NotNull
    public static Timespan valueOf(@NotNull Duration duration) {
        return valueOf(duration.toMillis());
    }

    @NotNull
    public static Timespan valueOf(long time, @NotNull TimeUnit unit) {
        return valueOf(unit.toMillis(time));
    }

    @NotNull
    public static Timespan valueOf(long millis) {
        if (millis < 1) {
            return ZERO;
        }
        return new Timespan(millis);
    }

    private final long millis;
    private final long ticks;

    Timespan(long millis) {
        this.millis = millis;
        this.ticks = (long) (millis * 0.02);
    }

    @NotNull
    public String as(@NotNull ChronoFormat format) {
        return format.format(Duration.ofMillis(millis));
    }

	public long toMillis() {
		return millis;
	}

    public long toTicks() {
        return ticks;
    }
}
