package com.epsilon.auto.poc;

import com.epsilon.auto.poc.process.FileTransformProcessor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;
@Slf4j
@Data
@Configuration
public class CatchUpInitialization implements InitializingBean {
    @Autowired
    public S3FeedWithMetadata initialFeed;
    @Autowired
    FileTransformProcessor processor;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Pair<String, S3Object>> unprocessedObjects = initialFeed.findMatchingObjects();
        unprocessedObjects.stream().forEach(o -> processor.transformFile(o.getLeft(), o.getRight().key()));

    }
}
