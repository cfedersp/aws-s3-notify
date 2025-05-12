package com.epsilon.auto.poc.rest;

import com.epsilon.auto.poc.SnsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;

import java.io.IOException;

@Slf4j
@RestController
public class S3RestController {

    @Autowired
    ObjectMapper mapper;

    @Autowired
    RestClient client;

    @Value("${input.bucket.name}")
    String bucketName;

    @PostConstruct
    public void findUnTaggedFiles() {
        log.warn("looking for files in bucket: {}", bucketName);
    }

    @PostMapping(value = "/aws/s3event")
    public void handleS3Notification(HttpServletRequest request) throws IOException {
        SnsMessage msg = mapper.readValue(request.getInputStream(), SnsMessage.class);
        log.info("Event type: {}", msg.getType());
        if(msg.getType().equals("SubscriptionConfirmation")) {
            handleSNSConfirmation(msg);
        }
        // This class doesn't provide a zero-arg constructor, but it does have jackson
        S3EventNotification notification = S3EventNotification.fromJson(msg.getMessage());
        notification.getRecords().stream().forEach(r -> log.info("s3 file changed. Operation: {}. Object: {}/{}",r.getEventName(), r.getS3().getBucket(), r.getS3().getObject()));
        log.warn("s3 event: \n{}", notification);
    }
    protected void handleSNSConfirmation(SnsMessage confirmReq) {
        log.warn("SNS confirmation: \n{}", confirmReq.toString());
        log.info("Sending confirmation to: {}", confirmReq.getSubscribeURL());
        //TODO: verify signature
        String result = client.get().uri(confirmReq.getSubscribeURL()).retrieve()
                .body(String.class);
        log.info("Successfully subscribed: {}", result);
    }
}
