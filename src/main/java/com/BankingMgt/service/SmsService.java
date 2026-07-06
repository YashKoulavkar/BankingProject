package com.BankingMgt.service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    // प्रोजेक्ट चालू झाल्यावर Twilio ला ऑटोमॅटिकली इनिशियलाइज करण्यासाठी
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }


    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message.creator(
                    new PhoneNumber(toPhoneNumber), // कोणाला पाठवायचा (युझरचा नंबर)
                    new PhoneNumber(fromPhoneNumber), // कोणाकडून जाणार (Twilio नंबर)
                    messageBody // मेसेज काय पाठवायचा
            ).create();
            System.out.println("SMS Sent Successfully to " + toPhoneNumber);
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
}