package com.gradle.develocity.api.builds;

import com.gradle.develocity.api.shared.GradleEnterpriseApiProvider;
import com.gradle.develocity.api.DevelocityApi;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

@Command(
    name = "builds",
    description = "A sample program that demonstrates using the Develocity API to extract build data about build cache performance",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120
)
public final class BuildsApiSample implements Callable<Integer> {

    @Mixin
    GradleEnterpriseApiProvider apiProvider;

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

    @Override
    public Integer call() throws Exception {
        DevelocityApi api = apiProvider.create();
        BuildProcessor buildProcessor = new BuildCacheBuildProcessor(api, projectName);
        BuildsProcessor buildsProcessor = new BuildsProcessor(api, buildProcessor, reverse, maxBuilds, maxWaitSecs);

        System.out.println("Processing builds ...");

        Instant startProcessingTime = reverse ? Instant.now() : Instant.now().minus(Duration.ofMinutes(15));
        buildsProcessor.process(startProcessingTime);

        return 0;
    }

}
