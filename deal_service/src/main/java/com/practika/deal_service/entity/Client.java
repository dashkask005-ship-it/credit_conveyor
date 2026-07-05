package com.practika.deal_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "work_experience")
    private Integer workExperience;

    @Column(name = "monthly_income")
    private BigDecimal monthlyIncome;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Client(String firstName, String lastName, LocalDate birthDate,
                  String email, String phone, Integer workExperience,
                  BigDecimal monthlyIncome, String employmentType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.email = email;
        this.phone = phone;
        this.workExperience = workExperience;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
    }
}