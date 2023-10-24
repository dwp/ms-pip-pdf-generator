# ms-pip2-pdf-generator

A microservice that maps a PIP2 Versioned application received
from [ms-health-capture-manager](https://gitlab.com/dwp/health/pip-apply/components/ms-pip2-health-capture-manager)
into HTML format to then generate a PDF using
the HTML-TO-PDF_A
microservice [ms-html-to-pdfa](https://gitlab.com/health/shared-components/components/ms-html-to-pdfa)

## API SPEC

The API spec can be found at [here](schemas/api-spec/ms-pip2-pdf-generator/openapi-spec-v2.yaml)

**NOTE**: *The V1 api spec is deprecated alongside any associated methods that are not related to
versioning*

# Example PIP2 application data in JSON

An example of the application data which is sent across from **ms-health-capture-manager** is
available [here](src/test/resources/v2TestData/testSubmissionDto.json)

## Dependency

* [Form specification model]
  (https://gitlab.com/dwp/health/pip-apply/libraries/form-specification-model)

* HTML-TO-PDF_A
  micro-service [ms-html-to-pdfa](https://gitlab.com/health/shared-components/components/ms-html-to-pdfa)

## Build and run the application locally

To run the application locally you need to have a locally built version
of [pip-apply-mocks](https://gitlab.com/dwp/health/pip-apply/components/pip-apply-mocks)

1. Clone repo above and then build using below command

```zsh
docker build -t pipcs-api-registration .
```

1. Run the docker-compose-local.yml file to spin up the application and dependencies locally

```zsh
docker-compose -f docker-compose-local.yml up -d
```

## Unit Testing

To run unit tests

```zsh
mvn clean verify
```

to build and vulnerability check

```zsh
mvn spring-boot:run

or

java -jar target/ms-pip2-pdf-generator-<artifactId>.jar
```

## Running the Component Tests

Run the following command to spin up the service in docker

#### With Docker

```zsh 
docker-compose up
```

#### With IntelliJ/Maven

Ensure the services are running in the background using docker

```zsh
docker-compose -f docker-compose-local.yml up -d
```

Open another terminal window and run the following maven command to execute the tests locally

```bash 
mvn clean verify -Papi-component-tests
```

## Configuration

The following are required config properties

| Property                                           | Example value                              | Description                                                                                                           | 
|:---------------------------------------------------|:-------------------------------------------|:----------------------------------------------------------------------------------------------------------------------|
| `HEALTH_CAPTURE_MANAGER_BASE_URL`                  | `ms-health-capture-manager-url  `          | URL to [ms-health-capture-manager](https://gitlab.com/dwp/health/pip-apply/components/ms-pip2-health-capture-manager) |
| `HEALTH_CAPTURE_MANAGER_ENDPOINT_PATH`             | `form-specification/{formSpecificationId}` | Endpoint path within health-capture-manager to retrieve the formSpecification                                         |
| `AWS_ENCRYPTION_DATA_KEY`                          | `ksm-key`                                  |                                                                                                                       |
| `AWS_S3_AWS_REGION`                                | `eu-west-2  `                              |                                                                                                                       |
| `AWS_S3_BUCKET`                                    | `bucket-name`                              | Name of location in S3 to store generated PDF                                                                         |
| `HTML_PDF_GENERATOR_BASE_URL`                      | `ms-html-pdf-url`                          | URL to ms-html-to-pdfa micro-service                                                                                  |
| `HTML_PDF_GENERATOR_ENDPOINT_PATH`                 | `generatePdf`                              | Endpoint path to ms-html-to-pdfa microservice                                                                         |
| `HTML_PDF_GENERATOR_HTML_TO_PDF_CONFORMANCE_LEVEL` | `PDFA_1_A`                                 | pdf conformance level                                                                                                 |

## docker

The docker image is built on the distroless base image

## Troubleshooting

This repository often fails in pipeline with errors such as

```html
  cannot find docker...
```

To resolve this ensure that:

1. Shared runners are **enabled** in the gitlab CI/CD settings
2. Group runners are **disabled** in the gitlab CI/CD settings
