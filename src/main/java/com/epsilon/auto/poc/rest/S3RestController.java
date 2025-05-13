package com.epsilon.auto.poc.rest;

import com.epsilon.auto.poc.SnsMessage;
import com.epsilon.auto.poc.process.FileTransformProcessor;
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
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
public class S3RestController {
    private static Boolean confirmedSubscription = false;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    RestClient client;

    @Value("${input.bucket.name}")
    String bucketName;

    @Autowired
    FileTransformProcessor processor;

    @PostMapping(value = "/aws/s3event")
    public void handleS3Notification(HttpServletRequest request) throws IOException {
        String event = new String(request.getInputStream().readAllBytes(), Charset.forName("UTF-8"));
        try {
            SnsMessage msg = mapper.readValue(event, SnsMessage.class);
            log.info("Event type: {}", msg.getType());
            if (msg.getType().equals("SubscriptionConfirmation")) {
                handleSNSConfirmation(msg);
            } else {
                // This class doesn't provide a zero-arg constructor, but it does have jackson
                S3EventNotification notification = S3EventNotification.fromJson(msg.getMessage());
                // log.warn("s3 event: \n{}", notification);
                notification.getRecords().stream().forEach(r -> {
                    log.info("s3 file changed. Operation: {}. Object: {}/{}", r.getEventName(), r.getS3().getBucket(), r.getS3().getObject());
                    processor.transformFile(r.getS3().getBucket().getName(), r.getS3().getObject().getKey());
                });
            }
        } catch (Exception e) {
            log.error("Could not handle event: {}.\n{}", event, e.toString());
        }
    }
    private void handleSNSConfirmation(SnsMessage confirmReq) {
        synchronized (confirmedSubscription) {
            if (!confirmedSubscription) {
                log.warn("SNS confirmation: \n{}", confirmReq.toString());
                log.info("Sending confirmation to: {}", confirmReq.getSubscribeURL());
                //TODO: verify signature
                String result = client.get().uri(confirmReq.getSubscribeURL()).retrieve()
                        .body(String.class);
                log.info("Successfully subscribed: {}", result);
                confirmedSubscription = true;
            }
        }
    }
}
