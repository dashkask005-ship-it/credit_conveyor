package com.practika.deal_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "offers")
@Data
@NoArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "term")
    private Integer term;

    @Column(name = "is_insurance_enabled")
    private Boolean isInsuranceEnabled;

    @Column(name = "is_salary_client")
    private Boolean isSalaryClient;

    @Column(name = "description")
    private String description;

    @Column(name = "priority")
    private Integer priority;

    public Offer(Application application, BigDecimal amount, BigDecimal rate,
                 BigDecimal monthlyPayment, Integer term, Boolean isInsuranceEnabled,
                 Boolean isSalaryClient, String description, Integer priority) {
        this.application = application;
        this.amount = amount;
        this.rate = rate;
        this.monthlyPayment = monthlyPayment;
        this.term = term;
        this.isInsuranceEnabled = isInsuranceEnabled;
        this.isSalaryClient = isSalaryClient;
        this.description = description;
        this.priority = priority;
    }
}
