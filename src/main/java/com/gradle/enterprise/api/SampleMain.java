package com.gradle.enterprise.api;

import com.gradle.enterprise.api.client.ApiClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

@Command(
    name = "gradle-enterprise-api-samples",
    description = "A sample program that demonstrates using the Gradle Enterprise API to extract build data about build cache performance",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120
)
public final class SampleMain implements Callable<Integer> {

    @Option(
        names = "--server-url",
        description = "The address of the Gradle Enterprise server",
        required = true,
        order = 0
    )
    String serverUrl;

    @Option(
        names = "--access-key-file",
        description = "The path to the file containing the access key",
        required = true,
        order = 1
    )
    String accessKeyFile;

    @Option(
        names = "--project-name",
        description = "The name of the project to show the builds of (if omitted, all builds are shown)",
        defaultValue = Option.NULL_VALUE,
        order = 2
    )
    String projectName;


    @Option(
        names = "--reverse",
        description = "A boolean indicating the time direction of the query. A value of true indicates a backward query, and returned builds will be sorted from most to least recent. A value of false indicates a forward query, and returned builds will be sorted from least to most recent (default: ${DEFAULT-VALUE}).",
        defaultValue = "false",
        order = 3
    )
    boolean reverse;

    @Option(
        names = "--max-builds",
        description = "The maximum number of builds to return by a single query. The number may be lower if --max-wait-secs is reached (default: ${DEFAULT-VALUE})",
        defaultValue = "100",
        order = 4
    )
    int maxBuilds;

    @Option(
        names = "--max-wait-secs",
        description = "The maximum number of seconds to wait until a query returns. If the query returns before --max-builds is reached, it returns with already processed builds (default: ${DEFAULT-VALUE})",
        defaultValue = "3",
        order = 5
    )
    int maxWaitSecs;

    public static void main(final String[] args) {
        System.exit(new CommandLine(new SampleMain()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        String serverUrl = this.serverUrl.endsWith("/")
            ? this.serverUrl.substring(0, this.serverUrl.length() - 1)
            : this.serverUrl;

        BufferedReader reader = new BufferedReader(new FileReader(accessKeyFile));
        String accessKey = reader.readLine();
        reader.close();

        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(serverUrl);
        apiClient.setRequestInterceptor(request -> request.setHeader("Authorization", "Bearer " + accessKey));

        GradleEnterpriseApi api = new GradleEnterpriseApi(apiClient);
        BuildProcessor buildProcessor = new BuildCacheBuildProcessor(api, serverUrl, projectName);
        BuildsProcessor buildsProcessor = new BuildsProcessor(api, buildProcessor, reverse, maxBuilds, maxWaitSecs);

        System.out.println("Processing builds ...");

        Instant startProcessingTime = reverse ? Instant.now() : Instant.now().minus(Duration.ofMinutes(15));
        buildsProcessor.process(startProcessingTime);

        return 0;
    }

}
