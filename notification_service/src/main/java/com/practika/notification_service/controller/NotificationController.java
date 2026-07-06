package com.practika.notification_service.controller;


import com.practika.notification_service.dto.NotificationRequestDTO;
import com.practika.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody NotificationRequestDTO request) {
        emailService.sendSimpleEmail(request);
        return ResponseEntity.ok("Письмо отправлено на: " + request.getEmail());
    }
}
