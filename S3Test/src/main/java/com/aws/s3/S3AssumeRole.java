package com.aws.s3;

import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

public class S3AssumeRole {
	public static void main(String[] args) {
		String roleArn = "arn:aws:iam::<account_id>:role/<rolename_with_minimum_permissions>";
		String roleSessionName = "abcd";
		Region region = Region.AP_SOUTHEAST_1;

		/*
		 * Uses ProfileCredentialsProvider class. Will directly look up to
		 * ~/.aws/credentials file skipping chain
		 */
		StsClient stsClient = StsClient.builder().region(region)
				.credentialsProvider(ProfileCredentialsProvider.create()).build();

		/* Bad Code Alert! Below line will call STS every time you run. */
		Credentials credentials = getTemporaryCredentials(stsClient, roleArn, roleSessionName);
		printS3Buckets(credentials.accessKeyId(), credentials.secretAccessKey(), credentials.sessionToken(), region);
		stsClient.close();
	}

	private static void printS3Buckets(String accessKey, String secretKey, String sessionToken, Region region) {
		AwsSessionCredentials awscCredentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
		S3Client s3Client = S3Client.builder().region(region)
				.credentialsProvider(StaticCredentialsProvider.create(awscCredentials)).build();
		List<Bucket> listOfBuckets = s3Client.listBuckets().buckets();
		System.out.println("=================" + listOfBuckets);
		s3Client.close();
	}

	private static Credentials getTemporaryCredentials(StsClient stsClient, String roleArn, String roleSessionName) {
		AssumeRoleRequest roleRequest = AssumeRoleRequest.builder().roleArn(roleArn).roleSessionName(roleSessionName)
				.build();
		AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);
		return roleResponse.credentials();
	}
}
