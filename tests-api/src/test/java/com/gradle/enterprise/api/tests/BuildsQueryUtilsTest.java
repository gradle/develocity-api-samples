package com.gradle.enterprise.api.tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static com.gradle.enterprise.api.tests.BuildsQueryUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuildsQueryUtilsTest {

    private static final ZoneOffset ZONE_ID = ZoneOffset.ofHours(2);

    private static OffsetDateTime dateTime(int year, int month, int dayOfMonth, int hour, int minute, int second) {
        return OffsetDateTime.of(year, month, dayOfMonth, hour, minute, second, 500, ZONE_ID).truncatedTo(ChronoUnit.SECONDS);
    }

    @Test
    @DisplayName("creates correct condition for closed buildStartTime range")
    void testBuildsBetweenCondition() {
        // given
        OffsetDateTime start = dateTime(2023, 10, 24, 9, 15, 30);
        OffsetDateTime end = dateTime(2023, 11, 25, 12, 30, 45);

        // expect
        assertEquals("buildStartTime:[2023-10-24T09:15:30+02:00 to 2023-11-25T12:30:45+02:00]", buildsBetween(start, end));
    }

    @Test
    @DisplayName("creates correct condition for open buildStartTime range")
    void testBuildSinceCondition() {
        // given
        OffsetDateTime start = dateTime(2023, 10, 24, 9, 15, 30);

        // expect
        assertEquals("buildStartTime>=2023-10-24T09:15:30+02:00", buildsSince(start));
    }

    @Test
    @DisplayName("creates correct condition for project name")
    void testProjectEqualsCondition() {
        assertEquals("project:myProject", projectNameEquals("myProject"));
    }

    @Test
    @DisplayName("correctly combines conditions using 'and' operator")
    void testAndCondition() {
        assertEquals("condition1 and condition2 and condition3", and("condition1", and("condition2", "condition3")));
    }

}