package com.epsilon.auto.poc.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
public class FileTransformProcessor {

    @Autowired
    protected S3TransferManager transferManager;
    @Autowired
    S3Client s3Client;

    @Autowired
    @Qualifier("localDir")
    Path localDir;

    protected enum TAG_SEQUENCE { SEARCHFORCE_DOWNLOADING, SEARCHFORCE_STARTED_PROCESSING, SEARCHFORCE_COMPLETED_PROCESSING, SEARCHFORCE_COMPLETION_STATUS};

    public void transformFile(String bucketName, String key) {
        String fileName = key.substring(key.lastIndexOf("/"));
        Path localDirAndFile = Path.of(localDir.toString(), fileName);

        CompletableFuture<CompletedFileDownload> downloadTask = downloadFileAsync(bucketName, key, localDirAndFile);
        Map<String, Tag> transformTags = new HashMap<>();
        transformTags.put(TAG_SEQUENCE.SEARCHFORCE_DOWNLOADING.name(), Tag.builder().key(TAG_SEQUENCE.SEARCHFORCE_DOWNLOADING.name()).value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
        PutObjectTaggingRequest downloadTagReq = PutObjectTaggingRequest.builder().bucket(bucketName).key(key).tagging(Tagging.builder().tagSet(transformTags.values()).build()).build();
        s3Client.putObjectTagging(downloadTagReq);
        CompletedFileDownload downloadResult = downloadTask.join();

        try {
            transformTags.put(TAG_SEQUENCE.SEARCHFORCE_STARTED_PROCESSING.name(), Tag.builder().key(TAG_SEQUENCE.SEARCHFORCE_STARTED_PROCESSING.name()).value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
            PutObjectTaggingRequest startedTagReq = PutObjectTaggingRequest.builder().bucket(bucketName).key(key).tagging(Tagging.builder().tagSet(transformTags.values()).build()).build();
            s3Client.putObjectTagging(startedTagReq);
            processFile(localDirAndFile, downloadResult);

            transformTags.put(TAG_SEQUENCE.SEARCHFORCE_COMPLETION_STATUS.name(), Tag.builder().key(
                    TAG_SEQUENCE.SEARCHFORCE_COMPLETION_STATUS.name()
            ).value("SUCCESS").build());
            PutObjectTaggingRequest completedTagReq = PutObjectTaggingRequest.builder().bucket(bucketName).key(key).tagging(
                            Tagging.builder().tagSet(
                                            transformTags.values())
                                    .build())
                    .build();
            s3Client.putObjectTagging(completedTagReq);

        } catch(Exception e) {
            transformTags.put(TAG_SEQUENCE.SEARCHFORCE_COMPLETION_STATUS.name(), Tag.builder().key(TAG_SEQUENCE.SEARCHFORCE_COMPLETION_STATUS.name()).value("FAILED").build());
            PutObjectTaggingRequest statusTagReq = PutObjectTaggingRequest.builder().bucket(bucketName).key(key).tagging(
                            Tagging.builder().tagSet(
                                            transformTags.values())
                                    .build())
                    .build();
            s3Client.putObjectTagging(statusTagReq);
        } finally {
            transformTags.put(TAG_SEQUENCE.SEARCHFORCE_COMPLETED_PROCESSING.name(), Tag.builder().key(String.valueOf(TAG_SEQUENCE.SEARCHFORCE_COMPLETED_PROCESSING)).value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).build());
            updateTags(bucketName, key, transformTags);
        }
    }
    public CompletableFuture<CompletedFileDownload> downloadFileAsync(String bucketName, String key, Path localDirAndFile) {

        log.info("Creating Download Request from bucket {}/{} to file: {}", bucketName, key, localDirAndFile);
        GetObjectRequest objReq = GetObjectRequest.builder().bucket(bucketName).key(key).build();

        DownloadFileRequest downloadRequest = DownloadFileRequest.builder().getObjectRequest(objReq).destination(localDirAndFile).build();
        log.info("Downloading Object from Bucket: {} with Key: {}", bucketName, key);

        FileDownload downloadFile = transferManager.downloadFile(downloadRequest);
        return downloadFile.completionFuture();
    }
    protected void updateTags(String bucketName, String objectKey, Map<String, Tag> objectTags) {
        PutObjectTaggingRequest completedTagReq = PutObjectTaggingRequest.builder().bucket(bucketName).key(objectKey).tagging(
                Tagging.builder().tagSet(
                        objectTags.values()
                ).build()
        ).build();
        s3Client.putObjectTagging(completedTagReq);
    }
    protected void processFile(Path localDirAndFile, CompletedFileDownload dataFile) {
        log.info("Content length [{}]", dataFile.response().contentLength());
        log.info("Processed file {}", localDirAndFile);
    }


}
