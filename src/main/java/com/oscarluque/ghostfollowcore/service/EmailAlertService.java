package com.oscarluque.ghostfollowcore.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAlertService {

    private static final int MAX_DISPLAY_ROWS = 500;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${MAIL_USERNAME}")
    private String senderEmail;

    @Async
    public void sendSummaryEmail(String to, String accountName, List<String> lostFollowers, List<String> newFollowers) {
        try {
            List<String> lostSafe = lostFollowers != null ? lostFollowers : Collections.emptyList();
            List<String> gainedSafe = newFollowers != null ? newFollowers : Collections.emptyList();

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setFrom(senderEmail);

            String subject = generateSubject(accountName, lostSafe.size(), gainedSafe.size());
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("accountName", accountName);
            context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));


            context.setVariable("lostList", limitList(lostSafe));
            context.setVariable("gainedList", limitList(gainedSafe));

            context.setVariable("lostExtraCount", Math.max(0, lostSafe.size() - MAX_DISPLAY_ROWS));
            context.setVariable("gainedExtraCount", Math.max(0, gainedSafe.size() - MAX_DISPLAY_ROWS));

            context.setVariable("lostTotal", lostSafe.size());
            context.setVariable("gainedTotal", gainedSafe.size());

            String htmlContent = templateEngine.process("summary-alert", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email de RESUMEN enviado a {} (Datos recortados a {} items)", to, MAX_DISPLAY_ROWS);

        } catch (MessagingException e) {
            log.error("Error enviando email de resumen: {}", e.getMessage());
        }
    }

    private List<String> limitList(List<String> list) {
        return list.stream()
                .limit(MAX_DISPLAY_ROWS)
                .toList();
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


    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("password-reset", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Tu código de recuperación - GhostFollow");
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Correo de recuperación enviado exitosamente a: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar el correo de recuperación a {}: {}", toEmail, e.getMessage());
        }
    }


}