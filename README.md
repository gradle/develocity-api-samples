# Develocity API Samples

This repository demonstrates using the Develocity API and generating client code from its OpenAPI specification.

The sample contains two scenarios:
* The `builds` scenario observes builds being published to the given Develocity instance in real-time and prints basic attributes along with build cache performance metrics.

## How to build

Execute:

```
$ ./gradlew install
```

This builds and installs the program into `build/install/develocity-api-samples`.
You can use the `build/install/develocity-api-samples/bin/develocity-api-samples` script to run the sample.

### Note on Java 11

The current version of the OpenAPI generator requires Java 11 to generate the client code. Even though this sample uses Java 11 to generate the client, but the generated **client code is based on Java 8**.
Therefore, the generated client is still compatible with Java 8 based projects.

## How to run

A Develocity access key with the “Export build data via the API” permission is required.

To create an access key:

1. Sign in to Develocity.
2. Access "My settings" from the user menu in the top right-hand corner of the page.
3. Access "Access keys" from the left-hand menu.
4. Click "Generate" on the right-hand side and copy the generated access key.

The access key should be saved to a file, which will be supplied as a parameter to the program.

### Builds API sample

After provisioning the access key, execute:

```
$ build/install/develocity-api-samples/bin/develocity-api-samples builds --server-url=«serverUrl» --access-key-file=«accessKeyFile» --project-name=«projectName»
```

- `«serverUrl»`: The address of your Develocity server (e.g. `https://develocity.example.com`)
- `«accessKeyFile»`: The path to the file containing the access key
- `«projectName»` (optional): The name of the project to limit reporting to (reports all builds when omitted)
- `«reverse»` (optional): A boolean indicating the time direction of the query. A value of true indicates a backward query. A value of false indicates a forward query (default: false).
- `«maxBuilds»` (optional): The maximum number of builds to return by a single query. The number may be lower if --max-wait-secs is reached (default - 100)
- `«maxWaitSecs»` (optional): The maximum number of seconds to wait until a query returns. If the query returns before --max-builds is reached, it returns with already processed builds (default - 3)

The program will print `Processing builds ...`, then:
- when not using `--reverse` or using `--reverse=false`: indefinitely listen for any new builds being published to Develocity and print basic information about each build to the console.
- when using `--reverse` or `--reverse=true`: listen for all builds that were already published to Develocity and print basic information about each build to the console.

To stop the program, use <kbd>Ctrl</kbd> + <kbd>C</kbd>.

## The sample code

The sample code can be found [here](https://github.com/gradle/gradle-enterprise-api-samples/blob/main/src/main/java/com/gradle/enterprise/api).

## Further documentation

The Develocity API manual and reference documentation for each version of the API can be found [here](https://docs.gradle.com/enterprise/api-manual).

## License

The Develocity API Samples project is open-source software released under the [Apache 2.0 License][apache-license].

[apache-license]: https://www.apache.org/licenses/LICENSE-2.0.html
