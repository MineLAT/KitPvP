package com.planetgallium.kitpvp.api.util;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class ChronoFormat {

    private static final List<ChronoUnit> UNITS = Arrays.asList(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS
    );

    public static final ChronoFormat LONG = new ChronoFormat(UNITS, " ", Integer.MAX_VALUE);
    public static final ChronoFormat SHORT = new ChronoFormat(UNITS, " ", Integer.MAX_VALUE) {
        @Override
        public @NotNull String format(long amount, @NotNull ChronoUnit unit) {
            return amount + ChronoResource.SHORT.get(unit);
        }
    };

    private final Collection<ChronoUnit> units;
    private final String separator;
    private final int max;

    public ChronoFormat(@NotNull Collection<ChronoUnit> units, @NotNull String separator, int max) {
        this.units = units;
        this.separator = separator;
        this.max = max;
    }

    @NotNull
    public Collection<ChronoUnit> getUnits() {
        return units;
    }

    @NotNull
    public String getSeparator() {
        return separator;
    }

    public int getMax() {
        return max;
    }

    public long getLength(@NotNull Duration duration) {
        return duration.getSeconds();
    }

    @NotNull
    public String format(@NotNull Duration duration) {
        final StringJoiner joiner = new StringJoiner(this.separator);
        long length = getLength(duration);
        int count = 0;

        for (ChronoUnit unit : this.units) {
            final long unitLength = getLength(unit.getDuration());
            final long amount = length / unitLength;
            if (amount > 0) {
                length -= unitLength * amount;
                joiner.add(format(amount, unit));
                count++;
            }
            if (length <= 0 || count >= this.max) {
                break;
            }
        }

        return joiner.toString();
    }

    @NotNull
    public String format(long amount, @NotNull ChronoUnit unit) {
        if (amount == 1 || amount == -1) {
            return amount + " " + ChronoResource.SINGULAR.get(unit);
        } else {
            return amount + " " + ChronoResource.PLURAL.get(unit);
        }
    }

    @NotNull
    public static Duration parse(@NotNull String duration) {
        return parse(duration, TimeUnit.MILLISECONDS);
    }

    @NotNull
    public static Duration parse(@NotNull String duration, @NotNull TimeUnit fallback) {
        long millis = 0L;
        for (String s : duration.toUpperCase().split(" AND | and |, ")) {
            final String[] split = s.split(" ", 2);
            try {
                if (split.length < 2) {
                    millis += fallback.toMillis(Long.parseLong(split[0]));
                    continue;
                }

                final char last = split[1].charAt(split[1].length() - 1);
                if (last != 's' && last != 'S') {
                    split[1] = split[1] + "S";
                }

                millis += TimeUnit.valueOf(split[1].toUpperCase()).toMillis(Long.parseLong(split[0]));
            } catch (Throwable t) {
                throw new IllegalArgumentException("The String '" + duration + "' is not a valid Duration");
            }
        }
        return Duration.ofMillis(millis);
    }
}
