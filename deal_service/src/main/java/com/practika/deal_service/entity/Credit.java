package com.practika.deal_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "credits")
@Data
@NoArgsConstructor
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
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

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    public Credit(Application application, BigDecimal amount, BigDecimal rate,
                  BigDecimal monthlyPayment, Integer term, BigDecimal totalAmount,
                  String status, LocalDate issueDate) {
        this.application = application;
        this.amount = amount;
        this.rate = rate;
        this.monthlyPayment = monthlyPayment;
        this.term = term;
        this.totalAmount = totalAmount;
        this.status = status;
        this.issueDate = issueDate;
    }

    public Credit(Application application, BigDecimal amount, BigDecimal rate,
                  BigDecimal monthlyPayment, Integer term, BigDecimal totalAmount,
                  String status) {
        this.application = application;
        this.amount = amount;
        this.rate = rate;
        this.monthlyPayment = monthlyPayment;
        this.term = term;
        this.totalAmount = totalAmount;
        this.status = status;
        this.issueDate = LocalDate.now();
    }
}