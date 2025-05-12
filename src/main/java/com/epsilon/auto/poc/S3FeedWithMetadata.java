package com.epsilon.auto.poc;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
public class S3FeedWithMetadata {
    @Value("${input.bucket.name}")
    protected String bucketName;
    @Value("${input.bucket.keyPrefix}")
    protected String keyPrefix;
    //@Value("${input.filter}")
    protected List<String> tagFilters;

    @Autowired
    protected S3Client s3Client;

    public List<String> findMatchingObjects() {
        ListObjectsV2Request.Builder listReqBuilder = ListObjectsV2Request.builder()
                .bucket(bucketName);
        if(!StringUtils.isBlank(keyPrefix)) {
            listReqBuilder = listReqBuilder.prefix(keyPrefix);
        }
        ListObjectsV2Request listReq = listReqBuilder.build();
        ListObjectsV2Response response = s3Client.listObjectsV2(listReq);
        return response.contents().stream().map(c -> c.key()).collect(Collectors.toList());
        // TODO: add pagination

    }


}
