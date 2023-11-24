package com.gradle.enterprise.api.tests;

import java.time.OffsetDateTime;

final class BuildsQueryUtils {
    private BuildsQueryUtils() {
    }

    static String projectNameEquals(String projectName) {
        return String.format("project:%s", projectName);
    }

    static String buildsBetween(Interval timeRange) {
        return String.format("buildStartTime:[%s to %s]", timeRange.getStart(), timeRange.getEnd());
    }

    static String buildsSince(OffsetDateTime startTimeInclusive) {
        return String.format("buildStartTime>=%s", startTimeInclusive);
    }

    static String and(String lhs, String rhs) {
        return String.format("%s and %s", lhs, rhs);
    }

}
