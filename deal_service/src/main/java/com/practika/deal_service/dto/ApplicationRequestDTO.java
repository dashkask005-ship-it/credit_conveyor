package com.practika.deal_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApplicationRequestDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer workExperience;
    private BigDecimal monthlyIncome;
    private LocalDate birthDate;
    private String employmentType;
    private BigDecimal amount;
    private Integer term;
    private String purpose;
}