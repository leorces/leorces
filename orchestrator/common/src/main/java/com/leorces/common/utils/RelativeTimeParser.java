package com.leorces.common.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class RelativeTimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)\\s*(d|h|m|s|ms)");

    public static LocalDateTime parseRelative(String input) {
        var matcher = TIME_PATTERN.matcher(input);
        var duration = Duration.ZERO;

        while (matcher.find()) {
            var value = Long.parseLong(matcher.group(1));
            var unit = matcher.group(2);

            switch (unit) {
                case "d" -> duration = duration.plusDays(value);
                case "h" -> duration = duration.plusHours(value);
                case "m" -> duration = duration.plusMinutes(value);
                case "s" -> duration = duration.plusSeconds(value);
                case "ms" -> duration = duration.plusMillis(value);
                default -> throw new IllegalArgumentException("Unknown time unit: " + unit);
            }
        }

        return LocalDateTime.now().plus(duration);
    }

}

