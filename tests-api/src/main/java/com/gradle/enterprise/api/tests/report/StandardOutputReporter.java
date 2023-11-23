package com.gradle.enterprise.api.tests.report;

import com.gradle.enterprise.api.model.*;
import com.gradle.enterprise.api.tests.TestContainerWithCases;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public final class StandardOutputReporter implements UnstableTestContainersReporter {

    private final String serverUrl;
    private final List<TestContainerWithCases> unstableTestContainersWithCases;

    public StandardOutputReporter(String serverUrl, List<TestContainerWithCases> unstableTestContainersWithCases) {
        this.serverUrl = serverUrl;
        this.unstableTestContainersWithCases = unstableTestContainersWithCases;
    }

    @Override
    public void report() {
        unstableTestContainersWithCases.forEach(containerWithCases -> {
            TestOrContainer container = containerWithCases.getContainer();
            List<TestOrContainer> cases = containerWithCases.getCases();
            BuildScanIdsByOutcome buildScanIdsByOutcome = requireNonNull(container.getBuildScanIdsByOutcome());

            System.out.println();
            System.out.println(toOutcomeDistribution(container));
            System.out.println("\tUnstable test cases:");
            cases.forEach(testCase -> System.out.printf("\t\t%s%n", toOutcomeDistribution(testCase)));
            System.out.println("\tWork units:");
            requireNonNull(container.getWorkUnits()).forEach(workUnit -> System.out.printf("\t\t%s%n", toDisplayName(workUnit)));
            System.out.println("\tExample Build Scans:");
            unstableBuildScanIds(buildScanIdsByOutcome).forEach(buildScanId -> System.out.printf("\t\t%s/s/%s%n", serverUrl, buildScanId));
        });
    }

    private Stream<String> unstableBuildScanIds(BuildScanIdsByOutcome buildScanIdsByOutcome) {
        return Stream.concat(
            buildScanIdsByOutcome.getFailed().stream(),
            buildScanIdsByOutcome.getFlaky().stream()
        );
    }

    private static String toOutcomeDistribution(TestOrContainer testOrContainer) {
        return String.format("%s (🔴 failed: %d, 🟡 flaky: %d, 💯 total: %d)", testOrContainer.getName(), testOrContainer.getOutcomeDistribution().getFailed(), testOrContainer.getOutcomeDistribution().getFlaky(), testOrContainer.getOutcomeDistribution().getTotal());
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
