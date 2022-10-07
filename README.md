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
- `«reverse»` (optional): A boolean indicating the time direction of the query. A value of true indicates a backward query. A value of false indicates a forward query (default: false).
- `«maxBuilds»` (optional): The maximum number of builds to return by a single query. The number may be lower if --max-wait-secs is reached (default - 100)
- `«maxWaitSecs»` (optional): The maximum number of seconds to wait until a query returns. If the query returns before --max-builds is reached, it returns with already processed builds (default - 3)

The program will print `Processing builds ...`, then:
- when not using `--reverse` or using `--reverse=false`: indefinitely listen for any new builds being published to Gradle Enterprise and print basic information about each build to the console.
- when using `--reverse` or `--reverse=true`: listen for all builds that were already published to Gradle Enterprise and print basic information about each build to the console.

To stop the program, use <kbd>Ctrl</kbd> + <kbd>C</kbd>.

## The sample code

The sample code can be found [here](https://github.com/gradle/gradle-enterprise-api-samples/blob/main/src/main/java/com/gradle/enterprise/api).

## Further documentation

The Gradle Enterprise API manual and reference documentation for each version of the API can be found [here](https://docs.gradle.com/enterprise/api-manual).


## License

The Gradle Enterprise API Samples project is open-source software released under the [Apache 2.0 License][apache-license].

[apache-license]: https://www.apache.org/licenses/LICENSE-2.0.html
