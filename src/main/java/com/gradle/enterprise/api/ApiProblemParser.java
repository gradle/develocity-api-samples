package com.gradle.enterprise.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.ApiProblem;

import java.util.Optional;

public final class ApiProblemParser {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CONTENT_TYPE = "application/problem+json";

    public static Optional<ApiProblem> maybeParse(ApiException apiException) {
        return apiException.getResponseHeaders()
            .firstValue("content-type")
            .filter(v -> v.startsWith(CONTENT_TYPE))
            .map(__ -> {
                try {
                    return OBJECT_MAPPER.readValue(apiException.getResponseBody(), ApiProblem.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
    }

}
