description = "Develocity Tests API sample"

plugins {
    id("api-sample-app")
}

application {
    mainClass.set("com.gradle.develocity.api.tests.TestsApiSampleMain")
    applicationName = "develocity-tests-api-sample"
}
