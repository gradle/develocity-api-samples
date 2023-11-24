package com.gradle.develocity.api.tests;

import com.gradle.enterprise.api.model.TestOrContainer;
import com.gradle.enterprise.api.model.TestOutcomeDistribution;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

interface UnstableTestContainersReporter {

    void report();

    default String outcomeDistributionToDisplayString(TestOutcomeDistribution outcomeDistribution) {
        return String.format("🔴 failed: %d, 🟡 flaky: %d, 💯 total: %d", outcomeDistribution.getFailed(), outcomeDistribution.getFlaky(), outcomeDistribution.getTotal());
    }

    default Stream<String> unstableBuildScanIds(TestOrContainer container) {
        return Stream.concat(
            requireNonNull(container.getBuildScanIdsByOutcome()).getFailed().stream(),
            requireNonNull(container.getBuildScanIdsByOutcome()).getFlaky().stream()
        );
    }
}
