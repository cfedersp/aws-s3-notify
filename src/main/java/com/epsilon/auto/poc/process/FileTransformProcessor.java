package com.epsilon.auto.poc.process;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Component
public class FileTransformProcessor {
    public void transformFile(String bucketName, String key) {
        log.warn("Processing Object from Bucket: {} with Key: {}", bucketName, key);
    }
}
