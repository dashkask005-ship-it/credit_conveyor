package com.practika.deal_service.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "term")
    private Integer term;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "status")
    private String status;

    @Column(name = "rate")
    private BigDecimal rate;

    @Column(name = "monthly_payment")
    private BigDecimal monthlyPayment;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Application(Client client, BigDecimal amount, Integer term, String purpose) {
        this.client = client;
        this.amount = amount;
        this.term = term;
        this.purpose = purpose;
        this.status = "NEW";
        this.rate = BigDecimal.ZERO;
        this.monthlyPayment = BigDecimal.ZERO;
    }

    public Application(Client client, BigDecimal amount, Integer term, String purpose,
                       BigDecimal rate, BigDecimal monthlyPayment) {
        this.client = client;
        this.amount = amount;
        this.term = term;
        this.purpose = purpose;
        this.rate = rate;
        this.monthlyPayment = monthlyPayment;
        this.status = "APPROVED";
    }
}