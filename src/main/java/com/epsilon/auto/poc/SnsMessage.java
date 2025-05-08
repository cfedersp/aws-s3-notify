package com.epsilon.auto.poc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@Data
@ToString
public class SnsMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -1005909983115719782L;
    @JsonProperty("Type")
    protected String Type;
    @JsonProperty("MessageId")
    protected String MessageId;
    @JsonProperty("Token")
    protected String Token;
    @JsonProperty("TopicArn")
    protected String TopicArn;
    @JsonProperty("Message")
    protected String Message;
    @JsonProperty("SubscribeURL")
    protected String SubscribeURL;
    @JsonProperty("UnsubscribeURL")
    protected String UnsubscribeURL;
    @JsonProperty("SignatureVersion")
    protected String SignatureVersion;
    @JsonProperty("SigningCertURL")
    protected String SigningCertURL;
    @JsonProperty("Timestamp")
    protected String Timestamp;
}
