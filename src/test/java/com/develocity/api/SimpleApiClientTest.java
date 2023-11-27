package com.develocity.api;

import com.gradle.enterprise.api.BuildsApi;
import com.gradle.enterprise.api.client.ApiClient;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.client.ServerConfiguration;
import com.gradle.enterprise.api.model.Build;
import com.gradle.enterprise.api.model.BuildsQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.openapi.OpenAPIConverter;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class SimpleApiClientTest {

    private static final String develocityAPIYamlUrl = System.getProperty("develocity.api.url");

    private ClientAndServer mockServer;

    @BeforeEach
    public void setup() {
        Configuration configuration = Configuration.configuration();
        List<Expectation> openApiExpectations = new OpenAPIConverter(new MockServerLogger()).buildExpectations(develocityAPIYamlUrl, null);
        mockServer = ClientAndServer.startClientAndServer(configuration, Collections.singletonList(19234));
        mockServer.upsert(openApiExpectations.toArray(new Expectation[0]));
    }

    @Test
    public void testSimpleAPICall() throws ApiException {
        InetSocketAddress remoteAddress = mockServer.remoteAddress();

        ApiClient apiClient = new ApiClient();
        apiClient.setServers(Collections.singletonList(new ServerConfiguration(
            "http://" + remoteAddress.getHostName() + ":" + remoteAddress.getPort(), "mockServer", Collections.emptyMap()
        )));
        BuildsApi buildsApi = new BuildsApi(apiClient);
        apiClient.addDefaultHeader("Authorization", "Bearer XYZ");

        List<Build> builds = buildsApi.getBuilds(new BuildsQuery());

        assertFalse(builds.isEmpty());
    }
}
