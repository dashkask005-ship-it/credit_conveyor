package com.practika.deal_service.service;


import com.practika.deal_service.entity.Client;
import com.practika.deal_service.entity.Offer;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreditCalculator {

    public CalculationResult calculate(Client client, BigDecimal amount, int term) {
        // 1. Проверка возраста
        int age = getAge(client.getBirthDate());
        if (age < 21) {
            return reject("Возраст меньше 21 лет");
        }
        if (age > 65) {
            return reject("Возраст больше 65 лет");
        }

        // 2. Проверка дохода
        if (client.getMonthlyIncome().intValue() < 20000) {
            return reject("Доход меньше 20 000 ₽");
        }

        // 3. Проверка стажа
        if (client.getWorkExperience() < 3) {
            return reject("Стаж работы меньше 6 месяцев");
        }

        // 4. Расчет ставки
        double rate = 15.0;
        if (age < 25) rate += 2.0;
        else if (age > 55) rate += 1.0;
        else rate -= 1.0;

        if (client.getMonthlyIncome().intValue() < 50000) rate += 3.0;
        else if (client.getMonthlyIncome().intValue() > 150000) rate -= 2.0;

        // 5. Расчет платежа Платеж = Сумма × (ставка × (1 + ставка)^срок) / ((1 + ставка)^срок - 1)
        BigDecimal payment = calculatePayment(amount, rate, term);

        // 6. Проверка платежа
        BigDecimal maxPayment = client.getMonthlyIncome().multiply(BigDecimal.valueOf(0.5));
        if (payment.compareTo(maxPayment) > 0) {
            return reject("Платеж превышает 50% дохода");
        }

        return approve(rate, payment);
    }

    public List<Offer> generateOffers(Client client, BigDecimal amount, int term) {
        List<Offer> offers = new ArrayList<>();

        boolean[][] combos = {
                {false, false},
                {false, true},
                {true, false},
                {true, true}
        };

        int priority = 1;
        for (boolean[] combo : combos) {
            boolean isInsurance = combo[0];
            boolean isSalary = combo[1];

            double rate = 15.0;
            BigDecimal totalAmount = amount;

            if (isInsurance) {
                rate -= 3.0;
                totalAmount = totalAmount.add(new BigDecimal("100000"));
            }
            if (isSalary) {
                rate -= 1.0;
            }

            // Корректировки по возрасту
            int age = getAge(client.getBirthDate());
            if (age < 25) rate += 2.0;
            else if (age > 55) rate += 1.0;
            else rate -= 1.0;

            // Корректировки по доходу
            if (client.getMonthlyIncome().intValue() < 50000) rate += 3.0;
            else if (client.getMonthlyIncome().intValue() > 150000) rate -= 2.0;

            rate = Math.max(5.0, Math.min(30.0, rate));

            BigDecimal payment = calculatePayment(totalAmount, rate, term);

            Offer offer = new Offer();
            offer.setAmount(totalAmount);
            offer.setRate(BigDecimal.valueOf(rate));
            offer.setMonthlyPayment(payment);
            offer.setTerm(term);
            offer.setIsInsuranceEnabled(isInsurance);
            offer.setIsSalaryClient(isSalary);
            offer.setPriority(priority);

            String desc = (isInsurance ? "Со страховкой" : "Без страховки") +
                    ", " + (isSalary ? "зарплатный клиент" : "обычный клиент") +
                    ", ставка " + rate + "%";
            offer.setDescription(desc);

            offers.add(offer);
            priority++;
        }

        return offers;
    }

    private int getAge(LocalDate birthDate) {
        if (birthDate == null) return 30;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Платеж = Сумма × (ставка × (1 + ставка)^срок) / ((1 + ставка)^срок - 1)
    private BigDecimal calculatePayment(BigDecimal amount, double rate, int term) {
        double monthlyRate = rate / 100.0 / 12.0;
        double pow = Math.pow(1 + monthlyRate, term);
        double coefficient = (monthlyRate * pow) / (pow - 1);
        return BigDecimal.valueOf(amount.doubleValue() * coefficient)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private CalculationResult reject(String message) {
        return new CalculationResult(false, message, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private CalculationResult approve(double rate, BigDecimal payment) {
        return new CalculationResult(true, "Кредит одобрен! Ставка: " + rate + "%",
                BigDecimal.valueOf(rate), payment);
    }

    public static class CalculationResult {
        private final boolean approved;
        private final String message;
        private final BigDecimal rate;
        private final BigDecimal monthlyPayment;

        public CalculationResult(boolean approved, String message,
                                 BigDecimal rate, BigDecimal monthlyPayment) {
            this.approved = approved;
            this.message = message;
            this.rate = rate;
            this.monthlyPayment = monthlyPayment;
        }

        public boolean isApproved() { return approved; }
        public String getMessage() { return message; }
        public BigDecimal getRate() { return rate; }
        public BigDecimal getMonthlyPayment() { return monthlyPayment; }
        public String getStatus() { return approved ? "APPROVED" : "REJECTED"; }
    }
}