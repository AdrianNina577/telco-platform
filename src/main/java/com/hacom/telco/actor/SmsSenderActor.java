package com.hacom.telco.actor;

import akka.actor.AbstractActor;
import com.hacom.telco.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("smsSenderActor")
@Scope("prototype")
public class SmsSenderActor extends AbstractActor {

    private static final Logger logger = LoggerFactory.getLogger(SmsSenderActor.class);

    @Autowired
    private SmsService smsService;

    public static class SmsRequest {
        private final String phoneNumber;
        private final String message;

        public SmsRequest(String phoneNumber, String message) {
            this.phoneNumber = phoneNumber;
            this.message = message;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SmsRequest.class, smsRequest -> {
                    logger.info("Sending SMS to: {}", smsRequest.getPhoneNumber());
                    smsService.sendSms(smsRequest.getPhoneNumber(), smsRequest.getMessage());
                })
                .build();
    }
}
