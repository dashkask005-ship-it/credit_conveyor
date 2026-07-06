package com.practika.notification_service.dto;


import lombok.Data;

@Data
public class NotificationRequestDTO {
    private String email;
    private String subject;
    private String message;
    private String clientName;
    private String status;
    private Long applicationId;
}
