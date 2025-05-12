package com.epsilon.auto.poc;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Data
@Configuration
public class CatchUpInitialization implements InitializingBean {
    @Autowired
    public S3FeedWithMetadata initialFeed;
    protected List<String> unprocessedObjects;
    @Override
    public void afterPropertiesSet() throws Exception {
        unprocessedObjects = initialFeed.findMatchingObjects();
        log.warn("Unprocessed Objects: {}", unprocessedObjects);
    }
}
