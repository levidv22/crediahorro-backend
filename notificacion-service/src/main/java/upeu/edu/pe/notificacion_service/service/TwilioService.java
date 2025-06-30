package upeu.edu.pe.notificacion_service.service;
import upeu.edu.pe.notificacion_service.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
@Service
public class TwilioService {

    private final TwilioConfig config;

    public TwilioService(TwilioConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        Twilio.init(config.getAccountSid(), config.getAuthToken());
    }

    public void enviarMensaje(String numeroDestino, String mensaje) {
        Message.creator(
                new PhoneNumber("whatsapp:" + numeroDestino), // ejemplo: whatsapp:+51906732399
                new PhoneNumber("whatsapp:+14155238886"),     // sandbox
                mensaje
        ).create();

    }

}