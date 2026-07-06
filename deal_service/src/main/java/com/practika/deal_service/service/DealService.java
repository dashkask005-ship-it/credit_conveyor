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
    private final NotificationClient notificationClient;  // ← ДОБАВЛЕНО

    @Transactional
    public ApplicationResponseDTO createApplication(ApplicationRequestDTO request) {

        // 1. Сохраняем клиента
        Client client = mapper.toClientEntity(request);
        client = clientRepository.save(client);

        // 2. Сохраняем заявку
        Application application = mapper.toEntity(request);
        application.setClient(client);
        application.setStatus("NEW");
        application.setRate(BigDecimal.ZERO);
        application.setMonthlyPayment(BigDecimal.ZERO);
        application = applicationRepository.save(application);

        // 3. Генерируем 4 предложения
        List<Offer> offers = calculator.generateOffers(client, request.getAmount(), request.getTerm());
        for (Offer offer : offers) {
            offer.setApplication(application);
            offerRepository.save(offer);
        }

        // 4. Калькулятор для основного расчета
        CreditCalculator.CalculationResult result = calculator.calculate(
                client, request.getAmount(), request.getTerm()
        );

        // 5. Обновляем заявку
        application.setRate(result.getRate());
        application.setMonthlyPayment(result.getMonthlyPayment());
        application.setStatus(result.getStatus());
        application = applicationRepository.save(application);

        sendNotification(client, application, result);

        // 7. Формируем ответ
        ApplicationResponseDTO response = mapper.toResponseDTO(application);
        response.setMessage(result.getMessage());

        return response;
    }

    @Transactional
    public Credit selectOffer(SelectOfferDTO request) {

        // 1. Находим заявку
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        // 2. Находим предложение
        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new RuntimeException("Предложение не найдено"));

        // 3. Обновляем заявку выбранным предложением
        application.setAmount(offer.getAmount());
        application.setRate(offer.getRate());
        application.setMonthlyPayment(offer.getMonthlyPayment());
        application.setStatus("APPROVED");
        application = applicationRepository.save(application);

        // 4. Создаем кредит
        Credit credit = new Credit();
        credit.setApplication(application);
        credit.setAmount(offer.getAmount());
        credit.setRate(offer.getRate());
        credit.setMonthlyPayment(offer.getMonthlyPayment());
        credit.setTerm(offer.getTerm());
        credit.setTotalAmount(offer.getAmount().multiply(BigDecimal.valueOf(offer.getTerm())));
        credit.setStatus("ISSUED");
        credit.setIssueDate(LocalDate.now());
        credit = creditRepository.save(credit);

        return credit;
    }

    private void sendNotification(Client client, Application application, CreditCalculator.CalculationResult result) {
        try {
            NotificationRequestDTO notification = new NotificationRequestDTO();
            notification.setEmail(client.getEmail());
            notification.setClientName(client.getFirstName() + " " + client.getLastName());
            notification.setSubject("Статус заявки №" + application.getId());
            notification.setMessage(result.getMessage());
            notification.setStatus(result.getStatus());
            notification.setApplicationId(application.getId());

            notificationClient.sendEmail(notification);

        } catch (Exception e) {
            log.error("Ошибка отправки уведомления: {}", e.getMessage());
        }
    }
}