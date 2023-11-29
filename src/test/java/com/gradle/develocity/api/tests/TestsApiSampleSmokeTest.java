package com.gradle.develocity.api.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jackson.JacksonUtils;
import com.gradle.enterprise.api.model.TestOrContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.openapi.OpenAPIConverter;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.matchers.Times.once;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TestsApiSampleSmokeTest {

    private static final String develocityAPIYamlUrl = System.getProperty("develocity.api.url");

    private ClientAndServer mockServer;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalStdOut;

    @TempDir
    private static Path accessKeyFileDir;
    private static Path accessKeyFile;

    @BeforeEach
    public void setup() throws IOException {
        Configuration configuration = Configuration.configuration();
        List<Expectation> openApiExpectations = new OpenAPIConverter(new MockServerLogger()).buildExpectations(develocityAPIYamlUrl, null);
        mockServer = ClientAndServer.startClientAndServer(configuration, singletonList(19235));
        mockServer.upsert(openApiExpectations.toArray(new Expectation[0]));

        accessKeyFile = accessKeyFileDir.resolve("access-key-file.txt");
        Files.write(accessKeyFile, "some-access-key".getBytes());

        originalStdOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void cleanup() {
        System.setOut(originalStdOut);
    }

    @Test
    @DisplayName("smoke test the Tests API sample app")
    public void testSampleApp() throws JsonProcessingException {
        // given
        // unstable containers from last week
        mockServer.when(request(), once()).respond(response()
            .withStatusCode(200)
            .withBody(content(SampleTestData.ANOTHER_UNSTABLE_CONTAINER))
        );

        // new unstable containers from yesterday
        mockServer.when(request(), once()).respond(response()
            .withStatusCode(200)
            .withBody(content(SampleTestData.UNSTABLE_CONTAINER))
        );

        // test cases belonging to new unstable containers
        mockServer.when(request(), once()).respond(response()
            .withStatusCode(200)
            .withBody(content(SampleTestData.UNSTABLE_TEST))
        );

        // when
        String serverUrl = "http://" + mockServer.remoteAddress().getHostName() + ":" + mockServer.remoteAddress().getPort();
        int exitCode = new CommandLine(new TestsApiSample()).execute("--server-url=" + serverUrl, "--access-key-file=" + accessKeyFile.toAbsolutePath());

        // then
        assertEquals(0, exitCode);
        assertTrue(outputStream.toString().contains(
            "\norg.example.TestContainer (🔴 failed: 1, 🟡 flaky: 2, 💯 total: 5)\n" +
            "\tView in Tests dashboard:"
        ));
        assertTrue(outputStream.toString().contains(
            "\tUnstable test cases:\n" +
            "\t\tsomeTest (🔴 failed: 2, 🟡 flaky: 4, 💯 total: 10)\n" +
            "\tWork units:\n" +
            "\t\tproject > :test\n" +
            "\tExample Build Scans:\n" +
            "\t\t" + serverUrl + "/s/123\n" +
            "\t\t" + serverUrl + "/s/456\n" +
            "\t\t" + serverUrl + "/s/789\n"
        ));
    }

    private static String content(TestOrContainer testOrContainer) throws JsonProcessingException {
        return String.format("{ \"content\": %s }", JacksonUtils.newMapper().writeValueAsString(singletonList(testOrContainer)));
    }
}
