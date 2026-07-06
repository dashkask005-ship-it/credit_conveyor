package com.practika.notification_service.service;

import com.practika.notification_service.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendSimpleEmail(NotificationRequestDTO request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getEmail());
            message.setSubject(request.getSubject());
            message.setText(request.getMessage());

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка отправки письма: " + e.getMessage());
        }
    }
}