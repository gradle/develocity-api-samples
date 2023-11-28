package com.develocity.api;

import com.develocity.api.builds.BuildsApiSample;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "develocity-api-samples",
    description = "A sample program that demonstrates using the Develocity API to extract build data about build cache performance",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120,
    subcommands = {BuildsApiSample.class}
)
public final class SampleMain {

    public static void main(final String[] args) {
        //noinspection InstantiationOfUtilityClass
        System.exit(new CommandLine(new SampleMain()).execute(args));
    }

}
