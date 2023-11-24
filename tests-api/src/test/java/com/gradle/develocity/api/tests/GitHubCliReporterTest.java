package com.gradle.develocity.api.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GitHubCliReporterTest {

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
    @DisplayName("issues correct GitHub CLI commands to create issues for unstable containers")
    void testCreateIssues() throws Exception {
        // given
        GitHubCliReporter reporter = new GitHubCliReporter(
            "https://my.ge.com",
            "https://github.com/owner/repo",
            singletonList(new TestContainerWithCases(SampleTestData.UNSTABLE_CONTAINER, singletonList(SampleTestData.UNSTABLE_TEST))),
            new Interval(
                OffsetDateTime.of(2023, 11, 24, 14, 19, 51, 0, ZoneOffset.ofHours(2)).truncatedTo(ChronoUnit.SECONDS),
                OffsetDateTime.of(2023, 11, 23, 14, 19, 51, 0, ZoneOffset.ofHours(2)).truncatedTo(ChronoUnit.SECONDS)
            ),
            true
        );

        // when
        reporter.report();

        // then
        assertEquals(
            "gh issue create --repo https://github.com/owner/repo --title Investigate unstable outcomes of `org.example.TestContainer` --body ## Summary\n" +
            "Previously stable test container `org.example.TestContainer` became unstable between `2023-11-24T14:19:51+02:00` and `2023-11-23T14:19:51+02:00`.\n" +
            "Outcome distribution: 🔴 failed: 1, 🟡 flaky: 2, 💯 total: 5.\n\n" +
            "### Unstable cases\n" +
            "* `someTest` (🔴 failed: 2, 🟡 flaky: 4, 💯 total: 10)\n\n" +
            "### Example Build Scans\n" +
            "* https://my.ge.com/s/123\n" +
            "* https://my.ge.com/s/456\n" +
            "* https://my.ge.com/s/789\n\n" +
            "Powered by Develocity API: https://docs.gradle.com/enterprise/api-manual/",
            outputStream.toString()
        );
    }

}