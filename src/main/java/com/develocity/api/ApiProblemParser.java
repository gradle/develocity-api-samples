package com.develocity.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.ApiProblem;

import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Optional;

public final class ApiProblemParser {
    private static final String CONTENT_TYPE = "application/problem+json";

    public static Optional<ApiProblem> maybeParse(ApiException apiException, ObjectMapper objectMapper) {
        return Optional.ofNullable(apiException.getResponseHeaders())
            .map(responseHeaders -> responseHeaders.get("content-type")).orElse(Collections.emptyList())
            .stream()
            .findFirst()
            .filter(headerValue -> headerValue.startsWith(CONTENT_TYPE))
            .map(__ -> {
                try {
                    return objectMapper.readValue(apiException.getResponseBody(), ApiProblem.class);
                } catch (JsonProcessingException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }
}
