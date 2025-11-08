package com.planetgallium.kitpvp.api.util;

import org.jetbrains.annotations.NotNull;

import java.time.temporal.ChronoUnit;

public class ChronoResource {

    public static final ChronoResource EMPTY = new ChronoResource() {
        @Override
        public @NotNull String get(@NotNull ChronoUnit unit) {
            return "";
        }
    };
    public static final ChronoResource PLURAL = new ChronoResource() {
        @Override
        public @NotNull String get(@NotNull ChronoUnit unit) {
            switch (unit) {
                case NANOS:
                    return "nanoseconds";
                case MICROS:
                    return "microseconds";
                case MILLIS:
                    return "milliseconds";
                case SECONDS:
                    return "seconds";
                case MINUTES:
                    return "minutes";
                case HOURS:
                    return "hours";
                case HALF_DAYS:
                    return "half days";
                case DAYS:
                    return "days";
                case WEEKS:
                    return "weeks";
                case MONTHS:
                    return "months";
                case YEARS:
                    return "years";
                case DECADES:
                    return "decades";
                case CENTURIES:
                    return "centuries";
                case MILLENNIA:
                    return "millennia";
                case ERAS:
                    return "eras";
                case FOREVER:
                    return "forever";
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    public static final ChronoResource SINGULAR = new ChronoResource() {
        @Override
        public @NotNull String get(@NotNull ChronoUnit unit) {
            switch (unit) {
                case NANOS:
                    return "nanosecond";
                case MICROS:
                    return "microsecond";
                case MILLIS:
                    return "millisecond";
                case SECONDS:
                    return "second";
                case MINUTES:
                    return "minute";
                case HOURS:
                    return "hour";
                case HALF_DAYS:
                    return "half day";
                case DAYS:
                    return "day";
                case WEEKS:
                    return "week";
                case MONTHS:
                    return "month";
                case YEARS:
                    return "year";
                case DECADES:
                    return "decade";
                case CENTURIES:
                    return "century";
                case MILLENNIA:
                    return "millennium";
                case ERAS:
                    return "era";
                case FOREVER:
                    return "forever";
                default:
                    throw new IllegalArgumentException();
            }
        }
    };
    public static final ChronoResource SHORT = new ChronoResource() {
        @Override
        public @NotNull String get(@NotNull ChronoUnit unit) {
            switch (unit) {
                case NANOS:
                    return "nano";
                case MICROS:
                    return "micro";
                case MILLIS:
                    return "milli";
                case SECONDS:
                    return "s";
                case MINUTES:
                    return "m";
                case HOURS:
                    return "h";
                case HALF_DAYS:
                    return "h/d";
                case DAYS:
                    return "d";
                case WEEKS:
                    return "w";
                case MONTHS:
                    return "mo";
                case YEARS:
                    return "y";
                case DECADES:
                    return "deca";
                case CENTURIES:
                    return "centi";
                case MILLENNIA:
                    return "mille";
                case ERAS:
                    return "era";
                case FOREVER:
                    return "ever";
                default:
                    throw new IllegalArgumentException();
            }
        }
    };

    @NotNull
    public String get(@NotNull ChronoUnit unit) {
        throw new UnsupportedOperationException();
    }
}
