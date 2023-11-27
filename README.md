# Gradle Enterprise API Samples

This repository demonstrates using the Gradle Enterprise API and generating client code from its OpenAPI specification.

* The `builds-api` sample observes builds being published to the given Gradle Enterprise instance in real-time and prints basic attributes along with build cache performance metrics.
* The `tests-api` sample determines previously stable test classes that have recently become unstable, and creates a report pointing to example builds published to the given Gradle Enterprise instance.

## How to build

Execute:

```
$ ./gradlew install
```

This builds and installs the programs into `builds-api/build/install/gradle-enterprise-builds-api-sample` and `tests-api/build/install/develocity-tests-api-sample`.
You can use either `builds-api/build/install/gradle-enterprise-builds-api-sample/bin/gradle-enterprise-builds-api-sample` or `tests-api/build/install/develocity-tests-api-sample/bin/develocity-tests-api-sample` scripts to run the sample of interest.

### Note on Java 11

The current version of the OpenAPI generator requires Java 11 to generate the client code. Even though this sample uses Java 11 to generate the client, but the generated **client code is based on Java 8**.
Therefore, the generated client is still compatible with Java 8 based projects.

## How to run

A Gradle Enterprise access key with the “Export build data via the API” permission is required.

To create an access key:

1. Sign in to Gradle Enterprise.
2. Access "My settings" from the user menu in the top right-hand corner of the page.
3. Access "Access keys" from the left-hand menu.
4. Click "Generate" on the right-hand side and copy the generated access key.

The access key should be saved to a file, which will be supplied as a parameter to the program.

### Builds API sample

After provisioning the access key, execute:

```
$ builds-api/build/install/gradle-enterprise-builds-api-sample/bin/gradle-enterprise-builds-api-sample --server-url=«serverUrl» --access-key-file=«accessKeyFile» --project-name=«projectName»
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

### Tests API sample

After provisioning the access key, execute:

```
$ tests-api/build/install/develocity-tests-api-sample/bin/develocity-tests-api-sample --server-url=«serverUrl» --access-key-file=«accessKeyFile» [--project-name=«projectName»] [--reporter-type=<<reporterType>>] [--github-repo=<<githubRepo>>]
```

- `«serverUrl»`: The address of your Gradle Enterprise server (e.g. `https://ge.example.com`)
- `«accessKeyFile»`: The path to the file containing the access key
- `«projectName»` (optional): The name of the project to limit reporting to (reports unstable containers from all projects when omitted)
- `«reporterType»` (optional): The type of the report to be generated for discovered unstable containers (possible values: `STANDARD_OUTPUT` or `GITHUB_CLI`). The `GITHUB_CLI` type requires the [GitHub CLI](https://cli.github.com/) to be installed on your machine. 
- `«githubRepo»` (optional): The URL of the GitHub repo to create issues in. Required if the reporter type is set to `GITHUB_CLI`.

The program will:
1. Determine a set of test containers which were unstable (i.e. failed or flaky) in the past 7 days.
2. Determine a set of test containers which became unstable just yesterday.
3. Fetch additional data for such containers like builds and test tasks/goals where the container was unstable, as well as the list of unstable cases.
4. Report the summary of findings to the standard output or create issues in the GitHub repository of your choice.

To stop the program, use <kbd>Ctrl</kbd> + <kbd>C</kbd>.

## Further documentation

The Gradle Enterprise API manual and reference documentation for each version of the API can be found [here](https://docs.gradle.com/enterprise/api-manual).

## License

The Gradle Enterprise API Samples project is open-source software released under the [Apache 2.0 License][apache-license].

[apache-license]: https://www.apache.org/licenses/LICENSE-2.0.html
