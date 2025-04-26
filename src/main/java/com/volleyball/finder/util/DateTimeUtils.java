package com.volleyball.finder.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtils {

    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    public static long minutesSince(LocalDateTime dbTimeAssumeTaipei) {
        ZonedDateTime zonedDbTime = dbTimeAssumeTaipei.atZone(TAIPEI_ZONE);
        ZonedDateTime nowTaipei = ZonedDateTime.now(TAIPEI_ZONE);
        return Duration.between(zonedDbTime, nowTaipei).toMinutes();
    }
}
