package com.practika.deal_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String email;
    private String subject;
    private String message;
    private String clientName;
    private String status;
    private Long applicationId;
}