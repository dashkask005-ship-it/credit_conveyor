package com.practika.deal_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApplicationResponseDTO {
    private Long id;
    private BigDecimal amount;
    private Integer term;
    private String purpose;
    private String status;
    private BigDecimal rate;
    private BigDecimal monthlyPayment;
    private LocalDateTime createdAt;
    private String clientName;
    private String clientEmail;
    private String message;
}