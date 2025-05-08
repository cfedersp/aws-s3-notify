package com.epsilon.auto.poc.rest;

import com.epsilon.auto.poc.SnsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(value = "/aws/s3event")
    public void handleS3Notification(HttpServletRequest request) throws IOException {
        SnsMessage msg = mapper.readValue(request.getInputStream(), SnsMessage.class);
        log.info("Event type: {}", msg.getType());
        if(msg.getType().equals("SubscriptionConfirmation")) {
            handleSNSConfirmation(msg);
        }
        // This class doesn't provide a zero-arg constructor, but it does have jackson
        S3EventNotification notification = S3EventNotification.fromJson(msg.getMessage());

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
