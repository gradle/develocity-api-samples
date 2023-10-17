import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.distsDirectory

group = "com.gradle.enterprise.api"
description = "Gradle Enterprise API sample"

plugins {
    id("org.openapi.generator") version "7.0.1"
    kotlin("jvm") version embeddedKotlinVersion apply false
    `java-library`
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.gradle.enterprise.api.SampleMain")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation("info.picocli:picocli:4.7.5")

    // Required for OpenAPI Generator
    implementation("io.swagger:swagger-annotations:1.6.12")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.15.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")

    testImplementation("org.mock-server:mockserver-netty:5.15.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

val gradleEnterpriseVersion = "2023.3" // Must be later than 2022.1
val baseApiUrl = providers.gradleProperty("apiManualUrl").orElse("https://docs.gradle.com/enterprise/api-manual/ref/")

val apiSpecificationFileGradleProperty = providers.gradleProperty("apiSpecificationFile")
val apiSpecificationURL = baseApiUrl.map { "${it}gradle-enterprise-${gradleEnterpriseVersion}-api.yaml" }
val apiSpecificationFile = apiSpecificationFileGradleProperty
    .map { s -> file(s) }
    .orElse(
        objects.property(File::class)
            .convention(provider {
                resources.text.fromUri(apiSpecificationURL).asFile()
            })
    ).map { file -> file.absolutePath }

val basePackageName = "com.gradle.enterprise.api"
val modelPackageName = "$basePackageName.model"
val invokerPackageName = "$basePackageName.client"
openApiGenerate {
    generatorName.set("java")
    inputSpec.set(apiSpecificationFile)
    outputDir.set(project.layout.buildDirectory.file("generated/$name").map { it.asFile.absolutePath })
    ignoreFileOverride.set(project.layout.projectDirectory.file(".openapi-generator-ignore").asFile.absolutePath)
    modelPackage.set(modelPackageName)
    apiPackage.set(basePackageName)
    invokerPackage.set(invokerPackageName)
    cleanupOutput.set(true)
    openapiNormalizer.set(mapOf("REF_AS_PARENT_IN_ALLOF" to "true"))
    // see https://github.com/OpenAPITools/openapi-generator/blob/master/docs/generators/java.md for a description of each configuration option
    configOptions.set(mapOf(
        "library" to "apache-httpclient",
        "dateLibrary" to "java8",
        "hideGenerationTimestamp" to "true",
        "openApiNullable" to "false",
        "useBeanValidation" to "false",
        "disallowAdditionalPropertiesIfNotPresent" to "false",
        "additionalModelTypeAnnotations" to  "@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)",
        "sourceFolder" to "",  // makes IDEs like IntelliJ more reliably interpret the class packages.
        "containerDefaultToNull" to "true"
    ))
}

tasks.test {
    useJUnitPlatform()

    apiSpecificationURL.orNull.let { systemProperties["ge.api.url"] = it }


    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

sourceSets {
    main {
        java {
            srcDir(tasks.openApiGenerate)
        }
    }
}
