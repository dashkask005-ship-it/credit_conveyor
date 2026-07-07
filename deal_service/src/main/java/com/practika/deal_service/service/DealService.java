package com.practika.deal_service.service;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.dto.NotificationRequestDTO;
import com.practika.deal_service.dto.SelectOfferDTO;
import com.practika.deal_service.entity.Application;
import com.practika.deal_service.entity.Client;
import com.practika.deal_service.entity.Credit;
import com.practika.deal_service.entity.Offer;
import com.practika.deal_service.integration.NotificationClient;
import com.practika.deal_service.mapper.ApplicationMapper;
import com.practika.deal_service.repository.ApplicationRepository;
import com.practika.deal_service.repository.ClientRepository;
import com.practika.deal_service.repository.CreditRepository;
import com.practika.deal_service.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealService {

    private final ClientRepository clientRepository;
    private final ApplicationRepository applicationRepository;
    private final OfferRepository offerRepository;
    private final CreditRepository creditRepository;
    private final CreditCalculator calculator;
    private final ApplicationMapper mapper;
    private final NotificationClient notificationClient;

    @Transactional
    public ApplicationResponseDTO createApplication(ApplicationRequestDTO request) {

        // 1. Клиент
        Client client = mapper.toClient(request);
        client = clientRepository.save(client);

        // 2. Заявка
        Application application = mapper.toApplicationWithClient(client, request);
        application = applicationRepository.save(application);

        // 3. Калькулятор для основного расчета
        CreditCalculator.CalculationResult result = calculator.calculate(
                client, request.getAmount(), request.getTerm()
        );

        // 4. Обновляем заявку результатами
        mapper.updateApplicationWithResult(application, result);
        application = applicationRepository.save(application);

        if (result.isApproved()) {
            List<Offer> offers = calculator.generateOffers(client, request.getAmount(), request.getTerm());
            mapper.setApplicationForOffers(offers, application);
            offerRepository.saveAll(offers);
            log.info("Создано {} предложений", offers.size());
            }
         else {
            log.info("Кредит не одобрен. Причина: {}", result.getMessage());
        }

        // 5. Отправляем уведомление
        sendNotification(client, application, result);

        // 6. Формируем ответ
        ApplicationResponseDTO response = mapper.toApplicationResponseDTO(application, result.getMessage());

        return response;
    }

    @Transactional
    public Credit selectOffer(SelectOfferDTO request) {

        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new RuntimeException("Предложение не найдено"));

        mapper.updateApplicationWithOffer(application, offer);
        application = applicationRepository.save(application);

        Credit credit = Credit.builder()
                .application(application)
                .amount(offer.getAmount())
                .rate(offer.getRate())
                .monthlyPayment(offer.getMonthlyPayment())
                .term(offer.getTerm())
                .totalAmount(offer.getAmount().multiply(BigDecimal.valueOf(offer.getTerm())))
                .status("ISSUED")
                .issueDate(LocalDate.now())
                .build();
        credit = creditRepository.save(credit);

        return credit;
    }

    private void sendNotification(Client client, Application application, CreditCalculator.CalculationResult result) {
        try {
            String subject;
            String message;

            if (result.isApproved()) {
                subject = "Кредит одобрен!";
                message = String.format(
                        "Уважаемый(ая) %s %s!\n\n" +
                                "Ваша заявка №%d ОДОБРЕНА!\n\n" +
                                "Условия кредита:\n" +
                                "• Сумма: %.2f ₽\n" +
                                "• Ставка: %.1f%%\n" +
                                "• Ежемесячный платеж: %.2f ₽\n" +
                                "• Срок: %d месяцев\n\n" +
                                "С уважением,\nКоманда Кредитного конвейера",
                        client.getFirstName(),
                        client.getLastName(),
                        application.getId(),
                        application.getAmount(),
                        application.getRate(),
                        application.getMonthlyPayment(),
                        application.getTerm()
                );
            } else {
                subject = "Кредит не одобрен";
                message = String.format(
                        "Уважаемый(ая) %s %s!\n\n" +
                                "К сожалению, Ваша заявка №%d НЕ ОДОБРЕНА.\n\n" +
                                "Причина: %s\n\n" +
                                "С уважением,\nКоманда Кредитного конвейера",
                        client.getFirstName(),
                        client.getLastName(),
                        application.getId(),
                        result.getMessage()
                );
            }

            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .email(client.getEmail())
                    .clientName(client.getFirstName() + " " + client.getLastName())
                    .subject(subject)
                    .message(message)
                    .status(result.getStatus())
                    .applicationId(application.getId())
                    .build();

            notificationClient.sendEmail(notification);

        } catch (Exception e) {
            log.error("⚠️ Ошибка отправки уведомления: {}", e.getMessage());
        }
    }
}