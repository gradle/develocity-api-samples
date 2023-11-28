package com.develocity.api;

import com.develocity.api.builds.BuildsApiSample;
import com.develocity.api.tests.TestsApiSample;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(
    name = "develocity-api-samples",
    description = "A program that demonstrates using the Develocity API to extract build and tests data",
    synopsisHeading = "%n@|bold Usage:|@ ",
    optionListHeading = "%n@|bold Options:|@%n",
    commandListHeading = "%n@|bold Commands:|@%n",
    parameterListHeading = "%n@|bold Parameters:|@%n",
    descriptionHeading = "%n",
    synopsisSubcommandLabel = "COMMAND",
    usageHelpAutoWidth = true,
    usageHelpWidth = 120,
    subcommands = {BuildsApiSample.class, TestsApiSample.class, HelpCommand.class}
)
public final class SampleMain {

    public static void main(final String[] args) {
        //noinspection InstantiationOfUtilityClass
        System.exit(new CommandLine(new SampleMain()).execute(args));
    }

}
