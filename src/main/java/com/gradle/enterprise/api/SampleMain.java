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
        order = 2
    )
    String projectName;

    public static void main(String[] args) {
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

        String projectName = this.projectName == null || this.projectName.trim().isEmpty() ? null : this.projectName;

        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(serverUrl);
        apiClient.setRequestInterceptor(request -> request.setHeader("Authorization", "Bearer " + accessKey));

        GradleEnterpriseApi api = new GradleEnterpriseApi(apiClient);
        BuildProcessor buildProcessor = new BuildCacheBuildProcessor(api, serverUrl, projectName);
        BuildsProcessor buildsProcessor = new BuildsProcessor(api, buildProcessor);

        System.out.println("Processing builds ...");

        buildsProcessor.process(Instant.now().minus(Duration.ofMinutes(15)));

        return 0;
    }

}
