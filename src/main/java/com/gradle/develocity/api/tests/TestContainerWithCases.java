package com.gradle.develocity.api.tests;

import com.gradle.develocity.api.model.TestOrContainer;

import java.util.List;

final class TestContainerWithCases {

    private final TestOrContainer container;
    private final List<TestOrContainer> cases;

    TestContainerWithCases(TestOrContainer container, List<TestOrContainer> cases) {
        this.container = container;
        this.cases = cases;
    }

    public TestOrContainer getContainer() {
        return container;
    }

    public List<TestOrContainer> getCases() {
        return cases;
    }
}
