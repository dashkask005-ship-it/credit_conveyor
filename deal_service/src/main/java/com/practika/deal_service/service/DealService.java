package com.practika.deal_service.service;

import com.practika.deal_service.config.MdcConstants;
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
import com.practika.deal_service.util.MdcUtil;
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

        long startTime = System.currentTimeMillis();

        MdcUtil.setOperation("CREATE_APPLICATION");
        MdcUtil.setClientEmail(request.getEmail());

        log.info("Начало создания заявки: сумма={}, срок={} мес.", request.getAmount(), request.getTerm());

        try {
            // 1. Клиент
            Client client = mapper.toClient(request);
            client = clientRepository.save(client);

            log.debug("Сохранение клиента в БД");
            MdcUtil.setClientId(client.getId());
            log.info("Клиент сохранен: ID:{}", client.getId());

            // 2. Заявка
            Application application = mapper.toApplicationWithClient(client, request);
            application = applicationRepository.save(application);

            MdcUtil.setApplicationId(application.getId());
            log.info("Заявка создана: ID={}, статус=NEW", application.getId());

            // 3. Калькулятор для основного расчета
            log.debug("Вычисление расчета кредита");
            CreditCalculator.CalculationResult result = calculator.calculate(
                    client, request.getAmount(), request.getTerm()
            );

            log.info("✓ Расчет завершен: статус={}, ставка={}%, платеж={}",
                    result.getStatus(), result.getRate(), result.getMonthlyPayment());

            // 4. Обновляем заявку результатами
            mapper.updateApplicationWithResult(application, result);
            application = applicationRepository.save(application);

            if (result.isApproved()) {
                List<Offer> offers = calculator.generateOffers(client, request.getAmount(), request.getTerm());
                mapper.setApplicationForOffers(offers, application);
                offerRepository.saveAll(offers);
                log.info("Создано {} предложений", offers.size());
            } else {
                log.info("Кредит не одобрен. Причина: {}", result.getMessage());
            }

            // 5. Отправляем уведомление
            sendNotification(client, application, result);

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Заявка обработана за {} мс", executionTime);

            // 6. Формируем ответ
            ApplicationResponseDTO response = mapper.toApplicationResponseDTO(application, result.getMessage());

            return response;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Ошибка создания заявки через {} мс: {}", executionTime, e.getMessage());

            throw e;
        }
    }

    @Transactional
    public Credit selectOffer(SelectOfferDTO request) {

        long startTime = System.currentTimeMillis();

        log.info("Выбор предложения: applicationId={}, offerId={}", request.getApplicationId(), request.getOfferId());

        try {
            // 1. Находим заявку
            Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> {
                        log.error("Заявка не найдена: {}", request.getApplicationId());
                        return new RuntimeException("Заявка не найдена");
                    });

            if (application.getClient() != null) {
                MdcUtil.setClientId(application.getClient().getId());
                MdcUtil.setClientEmail(application.getClient().getEmail());
            }

            log.debug("Заявка найдена: клиент={}, сумма={}", application.getClient().getId(), application.getAmount());

            // 2. Находим предложение
            Offer offer = offerRepository.findById(request.getOfferId())
                    .orElseThrow(() -> {
                        log.error("Предложение не найдено: {}", request.getOfferId());
                        return new RuntimeException("Предложение не найдено");
                    });

            log.info("Выбрано предложение: сумма={}, ставка={}%, страховка={}, зарплатный={}",
                    offer.getAmount(),
                    offer.getRate(),
                    offer.getIsInsuranceEnabled(),
                    offer.getIsSalaryClient());

            mapper.updateApplicationWithOffer(application, offer);
            application = applicationRepository.save(application);

            log.info("✓ Заявка обновлена: статус=APPROVED");

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

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Кредит выдан: ID={}, сумма={}, общая сумма={}, срок={} мес., время={} мс",
                    credit.getId(),
                    credit.getAmount(),
                    credit.getTotalAmount(),
                    credit.getTerm(),
                    executionTime);

            return credit;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Ошибка выбора предложения через {} мс: {}", executionTime, e.getMessage());

            throw e;
        }
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