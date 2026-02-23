package com.finetune.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuration for Cloudflare R2 object storage.
 * R2 is S3-compatible but NOT AWS S3.
 * 
 * This class is responsible for ALL R2 configuration and SDK object creation.
 * Services should inject these beans, not create their own clients.
 */
@Configuration
public class R2Config {

    @Value("${r2.account-id}")
    private String accountId;

    @Value("${r2.access-key}")
    private String accessKey;

    @Value("${r2.secret-key}")
    private String secretKey;

    /**
     * Create S3Client bean configured for Cloudflare R2.
     * 
     * IMPORTANT: This is for Cloudflare R2, not AWS S3.
     * - Custom endpoint: https://{accountId}.r2.cloudflarestorage.com
     * - Region: "auto" (required by R2)
     * - Path-style access: enabled (required by R2)
     * - ACLs: not supported by R2
     *
     * @return Configured S3Client for Cloudflare R2
     */
    @Bean
    public S3Client r2Client() {
        // Cloudflare R2 endpoint format
        String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
        
        // Create credentials from environment variables
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // Configure S3 client for Cloudflare R2
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(credentialsProvider)
                .region(Region.of("auto")) // R2 uses "auto" region
                .forcePathStyle(true) // Required for R2
                .httpClient(UrlConnectionHttpClient.builder().build()) // Explicitly set HTTP client
                .build();
    }

    /**
     * Create S3Presigner bean configured for Cloudflare R2.
     * Used for generating pre-signed URLs for secure temporary access.
     * 
     * IMPORTANT: Must use same endpoint and credentials as S3Client.
     *
     * @return Configured S3Presigner for Cloudflare R2
     */
    @Bean
    public S3Presigner r2Presigner() {
        // Cloudflare R2 endpoint format (same as S3Client)
        String endpoint = String.format("https://%s.r2.cloudflarestorage.com", accountId);
        
        // Create credentials (same as S3Client)
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // Configure presigner for Cloudflare R2
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(credentialsProvider)
                .region(Region.of("auto")) // R2 uses "auto" region
                .build();
    }
}
