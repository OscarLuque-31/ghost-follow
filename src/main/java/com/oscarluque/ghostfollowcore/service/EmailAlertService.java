package com.oscarluque.ghostfollowcore.service;

import com.oscarluque.ghostfollowcore.dto.follower.FollowerChangeEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
public class EmailAlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailAlertService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    public void sendUnFollowAlert(FollowerChangeEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(event.getUserEmail());
            helper.setSubject("👻 Alerta GhostFollow: " + event.getTargetUser() + " te ha dejado de seguir");

            Context context = new Context();
            context.setVariable("accountName", event.getAccountName());
            context.setVariable("targetUser", event.getTargetUser());
            context.setVariable("date", event.getLocalDateTime().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));

            String htmlContent = templateEngine.process("unfollow-alert", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            LOGGER.info("Email HTML enviado a {}",  event.getUserEmail());

        } catch (MessagingException e) {
            LOGGER.error("Error enviando email: {}", e.getMessage());
        }
    }
}
