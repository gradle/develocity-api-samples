# Gradle Enterprise API Samples

This repository demonstrates using the Gradle Enterprise API and generating client code from its OpenAPI specification.

The sample observes builds being published to the given Gradle Enterprise instance in real-time and prints basic attributes along with build cache performance metrics.

## How to build

Execute:

```
$ ./gradlew install
```

This builds and installs the program into `build/install/gradle-enterprise-api-samples`.
You can use the `build/install/gradle-enterprise-api-samples/bin/gradle-enterprise-api-samples` script to run the sample.

## How to run

A Gradle Enterprise access key with the “Export build data via the API” permission is required.

To create an access key:

1. Sign in to Gradle Enterprise.
2. Access "My settings" from the user menu in the top right-hand corner of the page.
3. Access "Access keys" from the left-hand menu.
4. Click "Generate" on the right-hand side and copy the generated access key.

The access key should be saved to a file, which will be supplied as a parameter to the program.

Next, execute:

```
$ build/install/gradle-enterprise-api-samples/bin/gradle-enterprise-api-samples --server-url=«serverUrl» --access-key-file=«accessKeyFile» --project-name=«projectName»
```

- `«serverUrl»`: The address of your Gradle Enterprise server (e.g. `https://ge.example.com`)
- `«accessKeyFile»`: The path to the file containing the access key
- `«projectName»` (optional): The name of the project to limit reporting to (reports all builds when omitted)

The program will print `Processing builds ...`, then indefinitely listen for any new builds being published to Gradle Enterprise and print basic information about each build to the console.
To stop the program, use <kbd>Ctrl</kbd> + <kbd>C</kbd>.

## The sample code

The sample code can be found [here](https://github.com/gradle/gradle-enterprise-api-samples/blob/main/src/main/java/com/gradle/enterprise/api).

## About the code generation

This sample uses [`openapi-generator`](https://openapi-generator.tech) `5.4.0` to generate client code from the Gradle Enterprise API specification.
This version has [a bug that causes incorrect client code to be generated](https://github.com/OpenAPITools/openapi-generator/issues/4808).

To work around this issue, this sample [customizes the generator](openApi/openapi-generator-config.json) to use a [custom template](openApi/api.mustache) for code generation.

This [has been fixed](https://github.com/OpenAPITools/openapi-generator/pull/11682) for an upcoming release.
Once the fix is released, this sample will be updated and the workaround removed.

## Further documentation

The Gradle Enterprise API manual and reference documentation for each version of the API can be found [here](https://docs.gradle.com/enterprise/api-manual).


## License

The Gradle Enterprise API Samples project is open-source software released under the [Apache 2.0 License][apache-license].

[apache-license]: https://www.apache.org/licenses/LICENSE-2.0.html
