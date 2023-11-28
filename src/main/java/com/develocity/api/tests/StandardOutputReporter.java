package com.develocity.api.tests;

import com.gradle.enterprise.api.model.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

final class StandardOutputReporter implements UnstableTestContainersReporter {

    private final String serverUrl;
    private final OffsetDateTime now;
    private final List<TestContainerWithCases> unstableTestContainersWithCases;

    StandardOutputReporter(String serverUrl, OffsetDateTime now, List<TestContainerWithCases> unstableTestContainersWithCases) {
        this.serverUrl = serverUrl;
        this.now = now;
        this.unstableTestContainersWithCases = unstableTestContainersWithCases;
    }

    @Override
    public void report() {
        unstableTestContainersWithCases.forEach(containerWithCases -> {
            TestOrContainer container = containerWithCases.getContainer();
            List<TestOrContainer> cases = containerWithCases.getCases();

            System.out.println();
            System.out.println(toOutcomeDistribution(container));
            System.out.printf("\tView in Tests dashboard: %s%n", getTestsDashboardLink(serverUrl, now, container));
            System.out.println("\tUnstable test cases:");
            cases.forEach(testCase -> System.out.printf("\t\t%s%n", toOutcomeDistribution(testCase)));
            System.out.println("\tWork units:");
            requireNonNull(container.getWorkUnits()).forEach(workUnit -> System.out.printf("\t\t%s%n", toDisplayName(workUnit)));
            System.out.println("\tExample Build Scans:");
            List<String> unstableBuildScanIds = unstableBuildScanIds(container);
            unstableBuildScanIds.stream().limit(MAX_BUILD_SCAN_IDS_TO_SHOW).forEach(buildScan -> System.out.printf("\t\t%s%n", getBuildScanLink(serverUrl, buildScan)));
            if (unstableBuildScanIds.size() > MAX_BUILD_SCAN_IDS_TO_SHOW) {
                System.out.printf("\t\t+%d more%n", unstableBuildScanIds.size() - MAX_BUILD_SCAN_IDS_TO_SHOW);
            }
        });
    }

    private String toOutcomeDistribution(TestOrContainer testOrContainer) {
        return String.format("%s (%s)", testOrContainer.getName(), outcomeDistributionToDisplayString(testOrContainer.getOutcomeDistribution()));
    }

    private static String toDisplayName(TestWorkUnit workUnit) {
        return Optional.ofNullable(workUnit.getGradle())
            .map(StandardOutputReporter::toString)
            .orElseGet(() -> Optional.ofNullable(workUnit.getMaven())
                .map(StandardOutputReporter::toString)
                .orElseGet(() -> Optional.ofNullable(workUnit.getBazel())
                    .map(StandardOutputReporter::toString)
                    .orElseThrow(() -> new IllegalStateException("No work units found for unstable container"))));
    }

    private static String toString(GradleWorkUnit gradleWorkUnit) {
        return String.format("%s > %s", gradleWorkUnit.getProjectName(), gradleWorkUnit.getTaskPath());
    }

    private static String toString(MavenWorkUnit mavenWorkUnit) {
        return String.format("%s:%s:%s@%s", mavenWorkUnit.getGroupId(), mavenWorkUnit.getArtifactId(), mavenWorkUnit.getGoalName(), mavenWorkUnit.getExecutionId());
    }

    private static String toString(BazelWorkUnit bazelWorkUnit) {
        return String.format("%s %s", bazelWorkUnit.getPackageName(), bazelWorkUnit.getTargetName());

    }
}
