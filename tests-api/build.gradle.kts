description = "Gradle Enterprise Tests API sample"

plugins {
    id("api-sample-app")
}

application {
    mainClass.set("com.gradle.enterprise.api.tests.TestsApiSampleMain")
    applicationName = "gradle-enterprise-tests-api-sample"
}
