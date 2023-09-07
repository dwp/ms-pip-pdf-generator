package uk.gov.dwp.health.pip.pdf.generator.component.utils;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3Util {

  private final AmazonS3 amazonS3;
  private final String bucketName;

  public S3Util(String serviceEndpoint, String awsRegion, String bucketName) {
    var endpointConfiguration =
        new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, awsRegion);

    this.amazonS3 =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .build();
    this.bucketName = bucketName;
  }

  public String getObjectAsString(String objectName) {
    return amazonS3.getObjectAsString(bucketName, objectName);
  }
}
