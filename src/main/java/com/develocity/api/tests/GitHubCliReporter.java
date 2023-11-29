package com.develocity.api.tests;

import com.gradle.enterprise.api.model.TestOrContainer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;

final class GitHubCliReporter implements UnstableTestContainersReporter {

    private final String serverUrl;
    private final String githubRepoUrl;
    private final OffsetDateTime now;
    private final List<TestContainerWithCases> unstableContainers;
    private final Interval buildTimeRange;
    private final boolean isDryRunMode;

    GitHubCliReporter(
        String serverUrl,
        String githubRepoUrl,
        OffsetDateTime now,
        List<TestContainerWithCases> unstableContainers,
        Interval buildTimeRange
    ) {
        this(serverUrl, githubRepoUrl, now, unstableContainers, buildTimeRange, false);
    }

    GitHubCliReporter(
        String serverUrl,
        String githubRepoUrl,
        OffsetDateTime now,
        List<TestContainerWithCases> unstableContainers,
        Interval buildTimeRange,
        boolean isDryRunMode
    ) {
        this.serverUrl = serverUrl;
        this.githubRepoUrl = githubRepoUrl;
        this.now = now;
        this.unstableContainers = unstableContainers;
        this.buildTimeRange = buildTimeRange;
        this.isDryRunMode = isDryRunMode;
    }

    @Override
    public void report() {
        unstableContainers.forEach(container -> {
            List<String> cmd = new ArrayList<>();
            addAll(cmd, "gh", "issue", "create", "--repo", githubRepoUrl);
            addAll(cmd, "--title", toIssueTitle(container.getContainer()));
            addAll(cmd, "--body", toIssueBody(container));

            try {
                if (isDryRunMode) {
                    System.out.print(String.join(" ", cmd));
                } else {
                    new ProcessBuilder()
                        .command(cmd)
                        .inheritIO()
                        .start()
                        .waitFor();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to create GitHub issues for discovered unstable containers", e);
            }
        });

    }

    private static String toIssueTitle(TestOrContainer container) {
        return String.format("Investigate unstable outcomes of `%s`", container.getName());
    }

    private String toIssueBody(TestContainerWithCases containerWithCases) {
        TestOrContainer container = containerWithCases.getContainer();
        List<TestOrContainer> cases = containerWithCases.getCases();

        StringBuilder sb = new StringBuilder();
        sb.append("## Summary\n");
        sb.append("Previously stable test container `").append(container.getName()).append("` became unstable between `").append(buildTimeRange.getStart()).append("` and `").append(buildTimeRange.getEnd()).append("`.\n");
        sb.append("[View in Tests dashboard.](").append(getTestsDashboardLink(serverUrl, now, container)).append(")\n");
        sb.append("\n");

        if (!cases.isEmpty()) {
            sb.append("### Unstable cases\n");
            cases.forEach(testCase -> sb.append("* `").append(testCase.getName()).append("` (").append(outcomeDistributionToDisplayString(testCase.getOutcomeDistribution())).append(")\n"));
            sb.append("\n");
        }

        sb.append("### Example Build Scans\n");
        List<String> unstableBuildScanIds = unstableBuildScanIds(container);
        unstableBuildScanIds.stream().limit(MAX_BUILD_SCAN_IDS_TO_SHOW).forEach(buildScan -> sb.append("* ").append(getBuildScanLink(serverUrl, buildScan)).append("\n"));
        if (unstableBuildScanIds.size() > MAX_BUILD_SCAN_IDS_TO_SHOW) {
            sb.append("* +").append(unstableBuildScanIds.size() - MAX_BUILD_SCAN_IDS_TO_SHOW).append(" more\n");
        }

        sb.append("\n");

        sb.append("Powered by Develocity API: https://docs.gradle.com/enterprise/api-manual/");

        return sb.toString();
    }
}
