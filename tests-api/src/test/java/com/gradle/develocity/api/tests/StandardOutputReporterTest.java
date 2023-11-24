package com.gradle.develocity.api.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardOutputReporterTest {

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
        StandardOutputReporter reporter = new StandardOutputReporter("https://my.ge.com", singletonList(unstableContainer));

        // when
        reporter.report();

        // then
        assertEquals(
            "\norg.example.TestContainer (🔴 failed: 1, 🟡 flaky: 2, 💯 total: 5)\n" +
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

}