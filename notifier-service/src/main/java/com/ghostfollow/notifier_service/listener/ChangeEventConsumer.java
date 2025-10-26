package com.ghostfollow.notifier_service.listener;

import com.ghostfollow.notifier_service.utils.Constants;
import com.ghostfollow.processor_service.model.FollowerChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class ChangeEventConsumer {

    @Autowired
    private JavaMailSender mailSender;

    private static final String EVENTS_TOPIC = "follower-change-events";
    private static final String GROUP_ID = "notifier-group";

    @KafkaListener(topics = EVENTS_TOPIC, groupId = GROUP_ID)
    public void consumeChangeEvent(FollowerChangeEvent event) {
        System.out.println("--- NOTIFIER RECIBIÓ EVENTO ---");
        System.out.println("Tipo: " + event.getEventType() + " | Usuario: " + event.getTargetUser());

        if(Constants.UNFOLLOW_EVENT.equals(event.getEventType())){
            sendUnFollowMail(event);
        }
    }

    private void sendUnFollowMail(FollowerChangeEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(Constants.GHOST_EMAIL);
        message.setTo("oscar15luquexd@gmail.com");
        message.setSubject("🚨 ¡ALERTA! Unfollow Detectado en Instagram");
        message.setText(
                "El usuario @" + event.getTargetUser() + " te ha dejado de seguir en Instagram.\n" +
                        "Cuenta monitoreada: @" + event.getAccountId() + "\n" +
                        "Hora del evento: " + event.getLocalDateTime()
        );

        try {
            mailSender.send(message);
            System.out.println("Correo de notificación enviado con éxito.");
        } catch (Exception e) {
            System.err.println("ERROR al enviar correo: " + e.getMessage());
        }
    }
}
