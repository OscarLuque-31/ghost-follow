package com.oscarluque.ghostfollowcore.service;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailAlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailAlertService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    public void sendSummaryEmail(String to, String accountName, List<String> lostFollowers, List<String> newFollowers) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(senderEmail);

            String subject = generateSubject(accountName, lostFollowers.size(), newFollowers.size());
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("accountName", accountName);
            context.setVariable("lostList", lostFollowers);
            context.setVariable("gainedList", newFollowers);
            context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            String htmlContent = templateEngine.process("summary-alert", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            LOGGER.info("Email de RESUMEN enviado a {}", to);

        } catch (MessagingException e) {
            LOGGER.error("Error enviando email de resumen: {}", e.getMessage());
        }
    }

    private String generateSubject(String account, int lost, int gained) {
        if (lost > 0 && gained > 0) {
            return String.format("📊 Resumen GhostFollow: -%d perdidos y +%d nuevos en %s", lost, gained, account);
        } else if (lost > 0) {
            return String.format("📉 Alerta: %d personas han dejado de seguir a %s", lost, account);
        } else {
            return String.format("✨ Buenas noticias: %d nuevos seguidores en %s", gained, account);
        }
    }
}
