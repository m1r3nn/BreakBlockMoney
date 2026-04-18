package ru.m1r3nn.breakblockmoney.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?$");

    private TimeParser() {
    }

    public static long parseMillis(String input) {
        if (input == null || input.isEmpty()) return -1;

        Matcher matcher = TIME_PATTERN.matcher(input.toLowerCase());
        if (!matcher.matches()) return -1;

        long days = parseGroup(matcher, 1);
        long hours = parseGroup(matcher, 2);
        long minutes = parseGroup(matcher, 3);
        long seconds = parseGroup(matcher, 4);

        if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) return -1;

        return (days * 86400 + hours * 3600 + minutes * 60 + seconds) * 1000L;
    }

    public static String format(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) result.append(days).append("д ");
        if (hours > 0) result.append(hours).append("ч ");
        if (minutes > 0) result.append(minutes).append("м ");
        if (seconds > 0) result.append(seconds).append("с");
        return result.toString().trim();
    }

    private static long parseGroup(Matcher matcher, int group) {
        String value = matcher.group(group);
        return value != null ? Long.parseLong(value) : 0;
    }
}