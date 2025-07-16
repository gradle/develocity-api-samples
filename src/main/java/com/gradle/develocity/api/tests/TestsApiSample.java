package com.gradle.develocity.api.tests;

import com.gradle.develocity.api.shared.GradleEnterpriseApiProvider;
import com.gradle.enterprise.api.DevelocityApi;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.TestCasesQuery;
import com.gradle.enterprise.api.model.TestContainersQuery;
import com.gradle.enterprise.api.model.TestIncludeFields;
import com.gradle.enterprise.api.model.TestOrContainer;
import com.gradle.enterprise.api.model.TestOutcome;
import com.gradle.enterprise.api.model.TestOutcomeDistribution;
import com.gradle.enterprise.api.model.TestsResponse;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Command(
    name = "tests",
    description = "A sample program that demonstrates using the Develocity Tests API to determine test classes that have recently become unstable",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120
)
public class TestsApiSample implements Callable<Integer> {

    private static final List<TestOutcome> UNSTABLE_OUTCOMES = Arrays.asList(TestOutcome.FAILED, TestOutcome.FLAKY);
    private static final List<TestIncludeFields> INCLUDE_BUILD_SCAN_IDS_AND_WORK_UNITS = Arrays.asList(TestIncludeFields.BUILD_SCAN_IDS, TestIncludeFields.WORK_UNITS);
    private static final Comparator<TestOrContainer> UNSTABLE_TEST_COMPARATOR = Comparator.<TestOrContainer>comparingDouble(testOrContainer -> {
            TestOutcomeDistribution outcomeDistribution = testOrContainer.getOutcomeDistribution();
            return (double) (outcomeDistribution.getFailed() + outcomeDistribution.getFlaky()) / outcomeDistribution.getTotal();
        })
        .reversed()
        .thenComparing(TestOrContainer::getName);

    @Mixin
    GradleEnterpriseApiProvider apiProvider;

    @Option(
        names = "--project-name",
        description = "The name of the project to show the containers of (if omitted, containers from all builds are shown)",
        defaultValue = Option.NULL_VALUE,
        order = 2
    )
    String projectName;

    @Option(
        names = "--reporter-type",
        description = "The type of the reporter to use (if omitted, the report will be printed to the standard output)",
        defaultValue = "STANDARD_OUTPUT",
        order = 2
    )
    ReporterType reporterType;

    @Option(
        names = "--github-repo",
        description = "The URL of the GitHub repository to create issues in, required if reporter type is GITHUB_CLI.",
        order = 3
    )
    @Nullable
    String githubRepoUrl;

    public static void main(String[] args) {
        System.exit(new CommandLine(new TestsApiSample()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        DevelocityApi api = apiProvider.create();

        // builds query does not support a more fine-grained resolution
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Set<String> unstableTestContainersFromLastWeek = getUnstableTestContainersFromLastWeek(api, now);
        List<TestOrContainer> newUnstableTestContainers = getNewUnstableTestContainers(api, unstableTestContainersFromLastWeek, now);
        List<TestContainerWithCases> unstableTestContainersWithCases = getUnstableTestCases(api, newUnstableTestContainers, now);

        switch (reporterType) {
            case STANDARD_OUTPUT:
                new StandardOutputReporter(apiProvider.getServerUrl(), now, unstableTestContainersWithCases).report();
                break;
            case GITHUB_CLI:
                new GitHubCliReporter(
                    apiProvider.getServerUrl(),
                    requireNonNull(githubRepoUrl, "GitHub URL is missing"),
                    now,
                    unstableTestContainersWithCases,
                    new Interval(now.minusDays(1), now)
                ).report();
                break;
            default:
                throw new IllegalArgumentException("Unstable containers reporter of type " + reporterType + " is not supported");
        }

        return 0;
    }

    private Set<String> getUnstableTestContainersFromLastWeek(DevelocityApi api, OffsetDateTime now) throws ApiException {
        Interval lastWeek = new Interval(now.minusDays(8), now.minusDays(1));
        String buildsQuery = projectName == null ? BuildsQueryUtils.buildsBetween(lastWeek) : BuildsQueryUtils.and(BuildsQueryUtils.buildsBetween(lastWeek), BuildsQueryUtils.projectNameEquals(projectName));

        TestsResponse response = api.getTestContainers(new TestContainersQuery()
            .testOutcomes(UNSTABLE_OUTCOMES)
            .query(buildsQuery)
        );

        Set<String> unstableContainerNames = response.getContent().stream()
            .map(TestOrContainer::getName)
            .collect(Collectors.toSet());
        System.out.printf("Found %d unstable test containers between %s and %s.%n", unstableContainerNames.size(), lastWeek.getStart(), lastWeek.getEnd());

        return unstableContainerNames;
    }

    private List<TestOrContainer> getNewUnstableTestContainers(DevelocityApi api, Set<String> unstableTestContainersFromLastWeek, OffsetDateTime now) throws ApiException {
        OffsetDateTime oneDayAgo = now.minusDays(1);
        String buildsQuery = projectName == null ? BuildsQueryUtils.buildsSince(oneDayAgo) : BuildsQueryUtils.and(BuildsQueryUtils.buildsSince(oneDayAgo), BuildsQueryUtils.projectNameEquals(projectName));

        List<TestOrContainer> unstableTestContainersFromYesterday = api.getTestContainers(new TestContainersQuery()
            .testOutcomes(UNSTABLE_OUTCOMES)
            .include(INCLUDE_BUILD_SCAN_IDS_AND_WORK_UNITS)
            .query(buildsQuery)
        ).getContent();

        List<TestOrContainer> newUnstableTestContainers = unstableTestContainersFromYesterday.stream()
            .filter(container -> !unstableTestContainersFromLastWeek.contains(container.getName()))
            .sorted(UNSTABLE_TEST_COMPARATOR)
            .collect(Collectors.toList());

        System.out.printf("Found %d test classes that became unstable since %s.%n", newUnstableTestContainers.size(), oneDayAgo);

        return newUnstableTestContainers;
    }

    private List<TestContainerWithCases> getUnstableTestCases(DevelocityApi api, List<TestOrContainer> newUnstableTestContainers, OffsetDateTime now) {
        System.out.println("Determining unstable test cases in the newly unstable test containers...");
        return newUnstableTestContainers.stream()
            .map(container -> {
                try {
                    return new TestContainerWithCases(
                        container,
                        getUnstableTestCases(api, container, now)
                    );
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            })
            .collect(Collectors.toList());
    }

    private List<TestOrContainer> getUnstableTestCases(DevelocityApi api, TestOrContainer testContainer, OffsetDateTime now) throws ApiException {
        OffsetDateTime oneDayAgo = now.minusDays(1);
        String buildsQuery = projectName == null ? BuildsQueryUtils.buildsSince(oneDayAgo) : BuildsQueryUtils.and(BuildsQueryUtils.buildsSince(oneDayAgo), BuildsQueryUtils.projectNameEquals(projectName));

        return api.getTestCases(new TestCasesQuery()
                .container(testContainer.getName())
                .testOutcomes(UNSTABLE_OUTCOMES)
                .query(buildsQuery)
            ).getContent()
            .stream()
            .sorted(UNSTABLE_TEST_COMPARATOR)
            .collect(Collectors.toList());
    }

}
