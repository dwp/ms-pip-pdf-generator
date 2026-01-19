package uk.gov.dwp.health.pip.pdf.generator.component.utils;

import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.net.URI;
import java.net.URISyntaxException;

public class S3Util {

  private final S3Client amazonS3;
  private final String bucketName;

  public S3Util(String serviceEndpoint, String awsRegion, String bucketName) throws URISyntaxException {

    this.amazonS3 =
        S3Client.builder()
            .endpointOverride(new URI(serviceEndpoint))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .region(Region.of(awsRegion))
            .build();
    this.bucketName = bucketName;
  }

  public String getObjectAsString(String objectName) {
    String objectAsString = amazonS3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(objectName).build()).asUtf8String();

    return objectAsString.substring(objectAsString.indexOf('{'), objectAsString.indexOf('}') + 1);
  }
}
