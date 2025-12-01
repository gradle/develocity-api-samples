package com.gradle.develocity.api.tests;

import com.gradle.develocity.api.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardOutputReporterTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2023, 11, 27, 9, 38, 39, 500, ZoneOffset.ofHours(2)).truncatedTo(ChronoUnit.SECONDS);

    private ByteArrayOutputStream outputStream;
    private PrintStream originalStdOut;

    @BeforeEach
    void setup() {
        originalStdOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void cleanup() {
        System.setOut(originalStdOut);
    }

    @Test
    @DisplayName("prints expected report for a single unstable container")
    void testReport() {
        // given
        TestContainerWithCases unstableContainer = new TestContainerWithCases(SampleTestData.UNSTABLE_CONTAINER, singletonList(SampleTestData.UNSTABLE_TEST));
        StandardOutputReporter reporter = new StandardOutputReporter("https://my.ge.com", NOW, singletonList(unstableContainer));

        // when
        reporter.report();

        // then
        assertEquals(
            "\norg.example.TestContainer (🔴 failed: 1, 🟡 flaky: 2, 💯 total: 5)\n" +
            "\tView in Tests dashboard: https://my.ge.com/scans/tests?search.startTimeMax=1701070719000&search.startTimeMin=1700465919000&tests.container=org.example.TestContainer\n" +
            "\tUnstable test cases:\n" +
            "\t\tsomeTest (🔴 failed: 2, 🟡 flaky: 4, 💯 total: 10)\n" +
            "\tWork units:\n" +
            "\t\tproject > :test\n" +
            "\tExample Build Scans:\n" +
            "\t\thttps://my.ge.com/s/123\n" +
            "\t\thttps://my.ge.com/s/456\n" +
            "\t\thttps://my.ge.com/s/789\n",
            outputStream.toString()
        );
    }

    @Test
    @DisplayName("shows only first 5 Build Scan IDs")
    void testShowLimitedNumberOfBuildScanIds() {
        // given
        TestContainerWithCases unstableContainer = new TestContainerWithCases(
            new TestOrContainer()
                .name("org.example.TestContainer")
                .outcomeDistribution(new TestOutcomeDistribution().failed(10).flaky(0).total(10))
                .addWorkUnitsItem(new TestWorkUnit().gradle(new GradleWorkUnit().projectName("project").taskPath(":test")))
                .buildScanIdsByOutcome(new BuildScanIdsByOutcome().failed(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")).flaky(emptyList())),
            singletonList(SampleTestData.UNSTABLE_TEST)
        );
        StandardOutputReporter reporter = new StandardOutputReporter("https://my.ge.com", NOW, singletonList(unstableContainer));

        // when
        reporter.report();

        // then
        assertEquals(
            "\norg.example.TestContainer (🔴 failed: 10, 🟡 flaky: 0, 💯 total: 10)\n" +
            "\tView in Tests dashboard: https://my.ge.com/scans/tests?search.startTimeMax=1701070719000&search.startTimeMin=1700465919000&tests.container=org.example.TestContainer\n" +
            "\tUnstable test cases:\n" +
            "\t\tsomeTest (🔴 failed: 2, 🟡 flaky: 4, 💯 total: 10)\n" +
            "\tWork units:\n" +
            "\t\tproject > :test\n" +
            "\tExample Build Scans:\n" +
            "\t\thttps://my.ge.com/s/1\n" +
            "\t\thttps://my.ge.com/s/2\n" +
            "\t\thttps://my.ge.com/s/3\n" +
            "\t\thttps://my.ge.com/s/4\n" +
            "\t\thttps://my.ge.com/s/5\n" +
            "\t\t+5 more\n",
            outputStream.toString()
        );
    }

}
