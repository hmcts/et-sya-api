# et-sya-api

[![Build Status](https://travis-ci.org/hmcts/et-sya-api.svg?branch=master)](https://travis-ci.org/hmcts/et-sya-api)

## Notes

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.

JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/et-sya-api` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4550` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Running the application in the cftlib environment

To run the application in the cflib local development environment that is provided by
https://github.com/hmcts/et-ccd-callbacks
it is necessary to use the `cftlib` profile.

```bash
  ./gradlew bootRun --args='--spring.profiles.active=cftlib'
```

### Functional API Tests
To run all Functional API tests against AAT instances:
Ensure F5 VPN is on.
These three variables need to be set in your WSL:
```bash
IDAM_API_URL=https://idam-api.aat.platform.hmcts.net
FT_SYA_URL=http://et-sya-api-aat.service.core-compute-aat.internal
```
Then run
```bash
./gradlew functional
```

To run all Functional API tests against local instances (useful for debugging purposes):
Note that some tests may fail as it uses the et.dev@hmcts.net user by default when being run locally,
the workaround is to create a new user for test (need to replace username and password in getLocalAccessToken method).
Ensure your local environment is up and running (see instructions in ecm-ccd-docker), Callback and SYA API instances are started in separate terminals.

Then run
```bash
./gradlew functional
```


### Viewing the API specification and consuming

In order to view API endpoints and consume the API directly, you can use the OpenAPI specification by navigating to the site with the following route appended (swagger-ui/index.html). Swagger UI (https://swagger.io/tools/swagger-ui/) allows anyone — be it your development team or your end consumers — to visualize and interact with the API’s resources without having any of the implementation logic in place. It’s automatically generated from your OpenAPI (formerly known as Swagger) Specification, with the visual documentation making it easy for back end implementation and client side consumption.

Local
```
  http://localhost:4550/swagger-ui/index.html
```

AAT
```
  http://et-sya-api-aat.service.core-compute-aat.internal/swagger-ui/index.html#/
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.


### Health check endpoint
In order to test if the application is up, you can call its health endpoint:

```
  curl http://localhost:4550/health
```

You should get a response similar to this:

```
  {"status":"UP"}


### Running contract or pact tests
You can run contract or pact tests as follows:
```bash
./gradlew contract
```
and then using it to publish your tests:
```bash
./gradlew pactPublish
```
In order to run both contract and publish, use the below command
```bash
./gradlew runAndPublishConsumerPactTests

```

### Other

Hystrix offers much more than Circuit Breaker pattern implementation or command monitoring.
Here are some other functionalities it provides:
 * [Separate, per-dependency thread pools](https://github.com/Netflix/Hystrix/wiki/How-it-Works#isolation)
 * [Semaphores](https://github.com/Netflix/Hystrix/wiki/How-it-Works#semaphores), which you can use to limit
 the number of concurrent calls to any given dependency
 * [Request caching](https://github.com/Netflix/Hystrix/wiki/How-it-Works#request-caching), allowing
 different code paths to execute Hystrix Commands without worrying about duplicating work

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

