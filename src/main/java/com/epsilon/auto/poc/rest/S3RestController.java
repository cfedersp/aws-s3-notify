package com.epsilon.auto.poc.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
@Slf4j
@RestController
@RequestMapping("/aws")
public class S3RestController {
    @PostMapping("s3notification/")
    public void handleS3Notification(S3EventNotification event){
        log.warn("Got notification from S3", event);
    }
}
