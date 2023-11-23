description = "Gradle Enterprise Builds API sample"

plugins {
    id("api-sample-app")
}

application {
    mainClass.set("com.gradle.enterprise.api.SampleMain")
    applicationName = "gradle-enterprise-builds-api-sample"
}
