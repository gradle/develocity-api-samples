package com.gradle.develocity.api.tests;

import com.gradle.develocity.api.model.TestOrContainer;
import com.gradle.develocity.api.model.TestOutcomeDistribution;

import java.time.OffsetDateTime;
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

    default String getBuildScanLink(String serverUrl, String buildScanId) {
        return String.format("%s/s/%s", serverUrl, buildScanId);
    }

    default String getTestsDashboardLink(String serverUrl, OffsetDateTime now, TestOrContainer container) {
        return String.format(
            "%s/scans/tests?search.startTimeMax=%d&search.startTimeMin=%d&tests.container=%s",
            serverUrl,
            now.toInstant().toEpochMilli(),
            now.minusDays(7).toInstant().toEpochMilli(),
            container.getName()
        );
    }
}
