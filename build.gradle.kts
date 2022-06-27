group = "com.gradle.enterprise.api"
description = "Gradle Enterprise API sample"

plugins {
    id("org.hidetake.swagger.generator") version "2.19.2"
    kotlin("jvm") version embeddedKotlinVersion apply false
    `java-library`
    application
}

val gradleEnterpriseVersion = "2022.2.4" // Must be later than 2022.1
val baseApiUrl = providers.gradleProperty("apiManualUrl").orElse("https://docs.gradle.com/enterprise/api-manual/ref/")

val apiSpecificationFileGradleProperty = providers.gradleProperty("apiSpecificationFile")
val apiSpecificationFile = apiSpecificationFileGradleProperty
    .map { s -> file(s) }
    .orElse(objects.property(File::class)
        .convention(provider {
            resources.text.fromUri("${baseApiUrl.get()}gradle-enterprise-${gradleEnterpriseVersion}-api.yaml").asFile()
        })
    )

application {
    mainClass.set("com.gradle.enterprise.api.SampleMain")
}

swaggerSources.configureEach {
    code.apply {
        language = "java"
        inputFile = apiSpecificationFile.get()
        configFile = file("openapi/openapi-generator-config.json")
        outputDir = file("${buildDir}/generated/$name")
    }

    val generationDir = file("${code.outputDir}/src/main/java")
    val sourceSet = sourceSets.create(name) {
        java { srcDir(files(generationDir).builtBy(code)) }
    }

    java {
        registerFeature(name) {
            usingSourceSet(sourceSet)
        }
    }

    dependencies {
        add(sourceSet.apiConfigurationName, "io.swagger:swagger-annotations:1.6.6")
        add(sourceSet.implementationConfigurationName, "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")
        add(sourceSet.implementationConfigurationName, "com.fasterxml.jackson.core:jackson-databind:2.13.3")
        add(sourceSet.implementationConfigurationName, "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.13.3")
        add(sourceSet.implementationConfigurationName, "com.google.code.findbugs:jsr305:3.0.2")
        add(sourceSet.implementationConfigurationName, "org.apache.httpcomponents:httpclient:4.5.13")
        add(sourceSet.implementationConfigurationName, "org.apache.httpcomponents:httpcore:4.4.15")
        add(sourceSet.implementationConfigurationName, "org.apache.httpcomponents:httpmime:4.5.13")
    }
}

swaggerSources.register("model") {
    code.components = listOf("models", "modelTests=false", "modelDocs=false", "log.level=error")
}

val client = swaggerSources.register("client") {
    code.components = mapOf(
        "supportingFiles" to listOf(
            "ApiClient.java",
            "ApiException.java",
            "ApiResponse.java",
            "Pair.java",
            "Configuration.java",
            "Authentication.java",
            "HttpBearerAuth.java",
            "ServerConfiguration.java",
            "ServerVariable.java",
            "JavaTimeFormatter.java",
            "StringUtil.java",
            "RFC3339DateFormat.java"
        ),
        "apis" to true,
        "apiTests" to false,
        "modelTests" to false,
        "apiDocs" to false,
        "modelDocs" to false,
        "models" to false,
        "log.level" to "error"
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("info.picocli:picocli:4.6.3")
    implementation(project(project.path)) {
        capabilities {
            requireCapability("com.gradle.enterprise.api:${project.name}-model")
        }
    }
    implementation(project(project.path)) {
        capabilities {
            requireCapability("com.gradle.enterprise.api:${project.name}-client")
        }
    }

    add("swaggerCodegen", "org.openapitools:openapi-generator-cli:6.0.0")

    add(sourceSets[client.get().name].implementationConfigurationName, project(":") as ModuleDependency) {
        capabilities {
            requireCapability("com.gradle.enterprise.api:${project.name}-model")
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
