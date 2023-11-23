package com.gradle.enterprise.api.tests;

import java.time.OffsetDateTime;

final class BuildsQueryUtils {
    private BuildsQueryUtils() {
    }

    static String buildsBetween(OffsetDateTime startTimeInclusive, OffsetDateTime endTimeExclusive) {
        return String.format("buildStartTime:[%s to %s]", startTimeInclusive, endTimeExclusive);
    }

    static String buildsSince(OffsetDateTime startTimeInclusive) {
        return String.format("buildStartTime>=%s", startTimeInclusive);
    }

}
