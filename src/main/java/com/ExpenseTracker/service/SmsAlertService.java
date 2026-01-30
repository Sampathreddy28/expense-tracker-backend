package com.ExpenseTracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
@Service
@ConditionalOnProperty(name = "twilio.account.sid")
public class SmsAlertService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String from;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    // ✅ STANDARD METHOD
    public void send(String phoneNumber, String message) {
        Message.creator(
            new PhoneNumber("whatsapp:" + phoneNumber),
            new PhoneNumber(from),
            message
        ).create();
    }
}
