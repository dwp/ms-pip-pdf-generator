# ms-pip2-pdf-generator

A Micro-service generates a PDF document from data (in JSON format) captured by PIP2 HTML online form. API spec can be found [here](api-spec/openapi-spec-v1.yaml)

# Example PIP application data in JSON 

```json

```
## Dependency

* Common PIP data model [library](https://gitlab.com/health-pdu/save-and-resume/pip2-html-commons:+v1.0.0)

* HTML-TO-PDF_A micro-service [ms-html-to-pdfa](https://gitlab.com/health/shared-components/components/ms-html-to-pdfa)

## Build and run the application locally

This is a standard SpringBoot application with all the configuration items held in `src/main/resources/application.yml` and bundled 
into the project at build.

```bash
mvn clean verify
```
to build and vulnerability check
```bash
mvn spring-boot:run

or

java -jar target/ms-pip2-pdf-generator-<artifactId>.jar
```
to run

## Run in a docker container

```bash

docker-compose up --scale api-test=0

```

will build and run the application with other dependent services stubbed

## Running the Component Tests

### Running Locally
Run the following command to spin up the service in docker
```bash 
docker-compose up --scale api-test=0
```
Open another terminal window and run the following maven command to execute the tests locally
```bash 
mvn clean verify -Papi-component-tests
```

## Configuration

All configuration is listed in `src/main/resources/application.yml` and follows the standard spring convention for yml file notation.  
The custom setup is configured with the following section and can be overridden (either on the command line or by environment variables).
The main configuration is serialised into handler classes 
`uk.gov.dwp.health.pdf.generator.config.PdfGeneratorConfig.java`

```yaml
AWS_ENCRYPTION_DATA_KEY=kms_key
AWS_S3_AWS_REGION=eu-west-2
AWS_S3_BUCKET=S3_bucket_name
HTML_PDF_GENERATOR_BASE_URL=ms-html-pdf-url **(note 1)**
HTML_PDF_GENERATOR_ENDPOINT_PATH=generatePdf **(note 2)**
HTML_PDF_GENERATOR_HTML_TO_PDF_CONFORMANCE_LEVEL=PDFA_1_A **(note 3)**
```

* `HTML_PDF_GENERATOR_BASE_URL`  URL to ms-html-to-pdfa micro-service **(note 1)**
* `HTML_PDF_GENERATOR_ENDPOINT_PATH` Endpoint path to ms-html-to-pdfa microservice **(note 2)**  
* `HTML_PDF_GENERATOR_HTML_TO_PDF_CONFORMANCE_LEVEL` = pdf conformance level **(note 3)**


## docker
The docker image is built on the distroless base image
