package com.finetune.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

/**
 * Service for managing object storage with Cloudflare R2.
 * R2 is S3-compatible but requires specific configuration.
 * 
 * DO NOT confuse this with AWS S3 - this is Cloudflare R2.
 * 
 * Configuration is handled by R2Config - this service only contains business logic.
 */
@Service
public class ObjectStorageService {

    @Value("${r2.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * Constructor with dependency injection of R2 clients.
     * Both S3Client and S3Presigner are configured in R2Config.
     *
     * @param s3Client S3Client configured for Cloudflare R2 (injected from R2Config)
     * @param s3Presigner S3Presigner configured for Cloudflare R2 (injected from R2Config)
     */
    public ObjectStorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Upload a PDF file to R2 storage.
     * R2 does NOT support ACLs - they are ignored.
     *
     * @param fileBytes The PDF file content as byte array
     * @param key The object key (path) in the bucket
     * @return The object key of the uploaded file
     * @throws S3Exception if upload fails
     */
    public String uploadPdf(byte[] fileBytes, String key) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/pdf")
                    // NOTE: R2 ignores ACL settings, no need to set them
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));
            
            return key;
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload PDF to R2: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a pre-signed URL for secure access to an object.
     * The URL allows temporary access without authentication.
     * Uses S3Presigner configured for Cloudflare R2.
     *
     * @param key The object key (path) in the bucket
     * @param expiration Duration until the URL expires
     * @return Pre-signed URL as a string
     * @throws RuntimeException if URL generation fails
     */
    public String generateSignedUrl(String key, Duration expiration) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            
            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signed URL for key: " + key + " - " + e.getMessage(), e);
        }
    }

    /**
     * Generate a pre-signed URL with default 15-minute expiration.
     * Used by staff dashboard to view agreements securely.
     *
     * @param key The object key (path) in the bucket
     * @return Pre-signed URL as a string (valid for 15 minutes)
     * @throws RuntimeException if URL generation fails
     */
    public String generateSignedUrl(String key) {
        return generateSignedUrl(key, Duration.ofMinutes(15));
    }

    /**
     * Delete an object from R2 storage.
     *
     * @param key The object key (path) to delete
     * @throws S3Exception if deletion fails
     */
    public void deleteObject(String key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete object from R2: " + e.getMessage(), e);
        }
    }

    /**
     * Check if an object exists in R2 storage.
     *
     * @param key The object key to check
     * @return true if the object exists, false otherwise
     */
    public boolean objectExists(String key) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to check object existence: " + e.getMessage(), e);
        }
    }
}
