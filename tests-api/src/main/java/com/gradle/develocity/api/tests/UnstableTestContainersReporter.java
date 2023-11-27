package com.gradle.develocity.api.tests;

import com.gradle.enterprise.api.model.TestOrContainer;
import com.gradle.enterprise.api.model.TestOutcomeDistribution;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

interface UnstableTestContainersReporter {

    int MAX_BUILD_SCAN_IDS_TO_SHOW = 5;

    void report();

    default String outcomeDistributionToDisplayString(TestOutcomeDistribution outcomeDistribution) {
        return String.format("🔴 failed: %d, 🟡 flaky: %d, 💯 total: %d", outcomeDistribution.getFailed(), outcomeDistribution.getFlaky(), outcomeDistribution.getTotal());
    }

    default List<String> unstableBuildScanIds(TestOrContainer container) {
        return Stream.concat(
            requireNonNull(container.getBuildScanIdsByOutcome()).getFailed().stream(),
            requireNonNull(container.getBuildScanIdsByOutcome()).getFlaky().stream()
        ).collect(Collectors.toList());
    }
}
