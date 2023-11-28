package com.develocity.api.tests;

import com.gradle.enterprise.api.model.*;

import java.util.Arrays;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

final class SampleTestData {

    static final TestOrContainer UNSTABLE_CONTAINER = new TestOrContainer()
        .name("org.example.TestContainer")
        .outcomeDistribution(new TestOutcomeDistribution().failed(1).flaky(2).total(5))
        .addWorkUnitsItem(new TestWorkUnit().gradle(new GradleWorkUnit().projectName("project").taskPath(":test")))
        .buildScanIdsByOutcome(new BuildScanIdsByOutcome().failed(singletonList("123")).flaky(Arrays.asList("456", "789")));

    static final TestOrContainer ANOTHER_UNSTABLE_CONTAINER = new TestOrContainer()
        .name("org.example.AnotherTestContainer")
        .outcomeDistribution(new TestOutcomeDistribution().failed(1).flaky(0).total(1))
        .addWorkUnitsItem(new TestWorkUnit().gradle(new GradleWorkUnit().projectName("project").taskPath(":test")))
        .buildScanIdsByOutcome(new BuildScanIdsByOutcome().failed(singletonList("111")).flaky(emptyList()));

    static final TestOrContainer UNSTABLE_TEST = new TestOrContainer()
        .name("someTest")
        .outcomeDistribution(new TestOutcomeDistribution().failed(2).flaky(4).total(10));

    private SampleTestData() {
    }

}
