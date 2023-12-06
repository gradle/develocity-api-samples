package com.gradle.develocity.api.shared;

import com.gradle.enterprise.api.GradleEnterpriseApi;
import com.gradle.enterprise.api.client.ApiClient;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GradleEnterpriseApiProvider {
    @CommandLine.Option(
        names = "--server-url",
        description = "The address of the Develocity server",
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

    public GradleEnterpriseApi create() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(accessKeyFile));
        String accessKey = reader.readLine();
        reader.close();

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(getServerUrl());
        apiClient.setBearerToken(accessKey);

        return new GradleEnterpriseApi(apiClient);
    }

    public String getServerUrl() {
        return this.serverUrl.endsWith("/")
            ? this.serverUrl.substring(0, this.serverUrl.length() - 1)
            : this.serverUrl;
    }
}
