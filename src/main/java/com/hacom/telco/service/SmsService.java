package com.hacom.telco.service;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.type.RecoverablePduException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${smpp.host}")
    private String smppHost;

    @Value("${smpp.port}")
    private int smppPort;

    @Value("${smpp.systemId}")
    private String smppSystemId;

    @Value("${smpp.password}")
    private String smppPassword;

    public void sendSms(String phoneNumber, String message) {
        DefaultSmppClient client = new DefaultSmppClient();
        SmppSession session = null;
        try {
            SmppSessionConfiguration sessionConfig = new SmppSessionConfiguration();
            sessionConfig.setWindowSize(1);
            sessionConfig.setName("telco-platform.smpp.session");
            sessionConfig.setType(SmppBindType.TRANSCEIVER);
            sessionConfig.setHost(smppHost);
            sessionConfig.setPort(smppPort);
            sessionConfig.setSystemId(smppSystemId);
            sessionConfig.setPassword(smppPassword);

            session = client.bind(sessionConfig);

            SubmitSm submit = new SubmitSm();
            submit.setSourceAddress(new Address((byte) 5, (byte) 0, "HACOM"));
            submit.setDestAddress(new Address((byte) 1, (byte) 1, phoneNumber));
            submit.setShortMessage(message.getBytes());

            SubmitSmResp resp = session.submit(submit, 10000);

            if (resp.getCommandStatus() == 0) {
                logger.info("Successfully sent SMS to {}", phoneNumber);
            } else {
                logger.error("Failed to send SMS to {}: {}", phoneNumber, resp.getResultMessage());
            }
        } catch (SmppTimeoutException | SmppChannelException | UnrecoverablePduException | InterruptedException | RecoverablePduException e) {
            logger.error("Error sending SMS", e);
        } finally {
            if (session != null) {
                session.unbind(5000);
            }
            client.destroy();
        }
    }
}