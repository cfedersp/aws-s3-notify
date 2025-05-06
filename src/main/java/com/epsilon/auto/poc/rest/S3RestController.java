package com.epsilon.auto.poc.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
@Slf4j
@RestController
@RequestMapping("/aws")
public class S3RestController {

    @PostMapping("s3event")
    public void handleS3Notification(@RequestBody String event){
        // This class doesn't provide a zero-arg constructor, but it does have jackson
        S3EventNotification notification = S3EventNotification.fromJson(event);

        log.warn("s3 events: \n{}", notification.toJsonPretty());

    }
}
