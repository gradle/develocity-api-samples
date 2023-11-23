package com.gradle.enterprise.api.tests;

import com.gradle.enterprise.api.GradleEnterpriseApi;
import com.gradle.enterprise.api.client.ApiClient;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.*;
import com.gradle.enterprise.api.tests.report.StandardOutputReporter;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static com.gradle.enterprise.api.tests.BuildsQueryUtils.buildsBetween;
import static com.gradle.enterprise.api.tests.BuildsQueryUtils.buildsSince;

@CommandLine.Command(
    name = "gradle-enterprise-tests-api-sample",
    description = "A sample program that demonstrates using the Gradle Enterprise Tests API to determine test classes that have recently become unstable",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120
)
public class TestsApiSampleMain implements Callable<Integer> {

    private static final List<TestOutcome> UNSTABLE_OUTCOMES = Arrays.asList(TestOutcome.FAILED, TestOutcome.FLAKY);
    private static final List<TestIncludeFields> INCLUDE_BUILD_SCAN_IDS_AND_WORK_UNITS = Arrays.asList(TestIncludeFields.BUILD_SCAN_IDS, TestIncludeFields.WORK_UNITS);
    private static final Comparator<TestOrContainer> UNSTABLE_TEST_COMPARATOR = Comparator.<TestOrContainer>comparingDouble(testOrContainer -> {
            TestOutcomeDistribution outcomeDistribution = testOrContainer.getOutcomeDistribution();
            return (double) (outcomeDistribution.getFailed() + outcomeDistribution.getFlaky()) / outcomeDistribution.getTotal();
        })
        .reversed()
        .thenComparing(TestOrContainer::getName);

    @CommandLine.Option(
        names = "--server-url",
        description = "The address of the Gradle Enterprise server",
        required = true,
        order = 0
    )
    String serverUrl;

    @CommandLine.Option(
        names = "--access-key-file",
        description = "The path to the file containing the access key",
        required = true,
        order = 1
    )
    String accessKeyFile;

    public static void main(String[] args) {
        System.exit(new CommandLine(new TestsApiSampleMain()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        String serverUrl = this.serverUrl.endsWith("/")
            ? this.serverUrl.substring(0, this.serverUrl.length() - 1)
            : this.serverUrl;
        GradleEnterpriseApi api = configureGradleEnterpriseApi(serverUrl);

        // builds query does not support a more fine-grained resolution
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        Set<String> unstableTestContainersFromLastWeek = getUnstableTestContainersFromLastWeek(api, now);
        List<TestOrContainer> newUnstableTestContainers = getNewUnstableTestContainers(api, unstableTestContainersFromLastWeek, now);
        List<TestContainerWithCases> unstableTestContainersWithCases = getUnstableTestCases(api, newUnstableTestContainers, now);

        new StandardOutputReporter(serverUrl, unstableTestContainersWithCases).report();

        return 0;
    }

    private GradleEnterpriseApi configureGradleEnterpriseApi(String serverUrl) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(accessKeyFile));
        String accessKey = reader.readLine();
        reader.close();

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(serverUrl);
        apiClient.setBearerToken(accessKey);

        return new GradleEnterpriseApi(apiClient);
    }

    private Set<String> getUnstableTestContainersFromLastWeek(GradleEnterpriseApi api, OffsetDateTime now) throws ApiException {
        OffsetDateTime eightDaysAgo = now.minusDays(8);
        OffsetDateTime oneDayAgo = now.minusDays(1);

        TestsResponse response = api.getTestContainers(new TestContainersQuery()
            .testOutcomes(UNSTABLE_OUTCOMES)
            .query(buildsBetween(eightDaysAgo, oneDayAgo))
        );

        Set<String> unstableContainerNames = response.getContent().stream()
            .map(TestOrContainer::getName)
            .collect(Collectors.toSet());
        System.out.printf("Found %d unstable test containers between %s and %s.%n", unstableContainerNames.size(), eightDaysAgo, oneDayAgo);

        return unstableContainerNames;
    }

    private List<TestOrContainer> getNewUnstableTestContainers(GradleEnterpriseApi api, Set<String> unstableTestContainersFromLastWeek, OffsetDateTime now) throws ApiException {
        OffsetDateTime oneDayAgo = now.minusDays(1);

        List<TestOrContainer> unstableTestContainersFromYesterday = api.getTestContainers(new TestContainersQuery()
            .testOutcomes(UNSTABLE_OUTCOMES)
            .include(INCLUDE_BUILD_SCAN_IDS_AND_WORK_UNITS)
            .query(buildsSince(oneDayAgo))
        ).getContent();

        List<TestOrContainer> newUnstableTestContainers = unstableTestContainersFromYesterday.stream()
            .filter(container -> !unstableTestContainersFromLastWeek.contains(container.getName()))
            .sorted(UNSTABLE_TEST_COMPARATOR)
            .collect(Collectors.toList());

        System.out.printf("Found %d test classes that became since %s.%n", newUnstableTestContainers.size(), oneDayAgo);

        return newUnstableTestContainers;
    }

    private List<TestContainerWithCases> getUnstableTestCases(GradleEnterpriseApi api, List<TestOrContainer> newUnstableTestContainers, OffsetDateTime now) {
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

    private List<TestOrContainer> getUnstableTestCases(GradleEnterpriseApi api, TestOrContainer testContainer, OffsetDateTime now) throws ApiException {
        return api.getTestCases(new TestCasesQuery()
                .container(testContainer.getName())
                .testOutcomes(UNSTABLE_OUTCOMES)
                .query(buildsSince(now.minusDays(1)))
            ).getContent()
            .stream()
            .sorted(UNSTABLE_TEST_COMPARATOR)
            .collect(Collectors.toList());
    }

}
