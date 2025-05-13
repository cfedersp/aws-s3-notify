package com.epsilon.auto.poc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
@Component
public class S3FeedWithMetadata {
    @Value("${input.bucket.name}")
    protected String bucketName;
    @Value("${input.bucket.keyPrefix}")
    protected String keyPrefix;
    @Value("${input.skipFolders}")
    protected boolean skipFolders;
    // @Value("${input.tagFilter.minus}")
    protected List<String> ignoreTagNames = List.of("SEARCHFORCE_STARTED_PROCESSING");

    @Autowired
    protected S3Client s3Client;

    public List<Pair<String, S3Object>> findMatchingObjects() {
        ListObjectsV2Request.Builder listReqBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        if(!StringUtils.isBlank(keyPrefix)) {
            listReqBuilder = listReqBuilder.prefix(keyPrefix);
        }
        ListObjectsV2Request listReq = listReqBuilder.build();
        ListObjectsV2Response response = s3Client.listObjectsV2(listReq);
        // TODO: add pagination

        Stream<S3Object> s3Objects = response.contents().stream();
        if(skipFolders) {
            s3Objects = s3Objects.filter(o -> !o.key().endsWith("/") && o.size() != 0L);
        }
        List<S3Object> matchingKeys = s3Objects.filter(o -> {
            GetObjectTaggingRequest tagRequest = GetObjectTaggingRequest.builder().bucket(bucketName).key(o.key()).build();
            GetObjectTaggingResponse tagResponse = s3Client.getObjectTagging(tagRequest);
            boolean foundTag = tagResponse.tagSet().stream().anyMatch(t -> {
                String objectName = bucketName + "/" + o.key();
                boolean hasTag = ignoreTagNames.contains(t.key());
                if(hasTag) log.info("object will be ignored: {} already has tag: {}", objectName, t);
                return hasTag;
            });

            return !foundTag;
        }).collect(Collectors.toList());

        return matchingKeys.stream().map(k -> Pair.of(bucketName, k)).collect(Collectors.toList());
    }


}
