group = "com.gradle.enterprise.api"
description = "Gradle Enterprise API sample"

plugins {
    id("org.hidetake.swagger.generator") version "2.19.2"
    kotlin("jvm") version embeddedKotlinVersion apply false
    `java-library`
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val gradleEnterpriseVersion = "2022.1" // Must be later than 2022.1
val baseApiUrl = "https://docs.gradle.com/enterprise/api-manual/ref/"

val apiSpecificationFileGradleProperty = providers.gradleProperty("apiSpecificationFile")
val apiSpecificationFile = apiSpecificationFileGradleProperty
    .map { s -> file(s) }
    .orElse(objects.property(File::class)
        .convention(provider { resources.text.fromUri("${baseApiUrl}gradle-enterprise-${gradleEnterpriseVersion}-api.yaml").asFile() }))

application {
    mainClass.set("com.gradle.enterprise.api.SampleMain")
}

swaggerSources.configureEach {
    code.apply {
        inputs.file("openapi/api.mustache")
            .withPropertyName("template")
            .withPathSensitivity(PathSensitivity.NONE)

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
        add(sourceSet.implementationConfigurationName, "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.2")
        add(sourceSet.implementationConfigurationName, "javax.annotation:javax.annotation-api:1.3.2")
        add(sourceSet.implementationConfigurationName, "com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
        add(sourceSet.implementationConfigurationName, "javax.validation:validation-api:2.0.1.Final")
        add(sourceSet.implementationConfigurationName, "com.google.code.findbugs:jsr305:3.0.2")
    }
}

swaggerSources.register("model") {
    code.components = listOf("models", "modelTests=false", "modelDocs=false", "log.level=error")
}

val client = swaggerSources.register("client") {
    code.components = mapOf(
        "supportingFiles" to listOf("ApiClient.java", "ApiException.java", "ApiResponse.java", "Pair.java"),
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
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2")
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

    add("swaggerCodegen", "org.openapitools:openapi-generator-cli:5.4.0")

    add(sourceSets[client.get().name].implementationConfigurationName, project(":") as ModuleDependency) {
        capabilities {
            requireCapability("com.gradle.enterprise.api:${project.name}-model")
        }
    }
}
