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

        // Устанавливаем контекст операции
        MdcUtil.setOperation("CREATE_APPLICATION");
        MdcUtil.setClientEmail(request.getEmail());

        log.info("Начало создания заявки: сумма={}, срок={} мес.", request.getAmount(), request.getTerm());

        try {
            // 1. Сохраняем клиента
            log.debug("Сохранение клиента в БД");
            Client client = mapper.toClientEntity(request);
            client = clientRepository.save(client);
            MdcUtil.setClientId(client.getId());
            log.info("Клиент сохранен: ID:{}", client.getId());

            // 2. Сохраняем заявку
            log.debug("Создание заявки в БД");
            Application application = mapper.toEntity(request);
            application.setClient(client);
            application.setStatus("NEW");
            application.setRate(BigDecimal.ZERO);
            application.setMonthlyPayment(BigDecimal.ZERO);
            application = applicationRepository.save(application);

            MdcUtil.setApplicationId(application.getId());
            log.info("Заявка создана: ID={}, статус=NEW", application.getId());

            // 3. Генерируем 4 предложения
            log.debug("Генерация кредитных предложений");
            List<Offer> offers = calculator.generateOffers(client, request.getAmount(), request.getTerm());
            for (Offer offer : offers) {
                offer.setApplication(application);
                Offer savedOffer = offerRepository.save(offer);

                log.debug("Предложение {}: ставка={}%, платеж={}, страховка={}",
                        savedOffer.getId(),
                        offer.getRate(),
                        offer.getMonthlyPayment(),
                        offer.getIsInsuranceEnabled());
            }
            log.info("Сгенерировано {} предложений", offers.size());

            // 4. Калькулятор для основного расчета
            log.debug("Вычисление расчета кредита");
            CreditCalculator.CalculationResult result = calculator.calculate(
                    client, request.getAmount(), request.getTerm()
            );

            // 5. Обновляем заявку
            application.setRate(result.getRate());
            application.setMonthlyPayment(result.getMonthlyPayment());
            application.setStatus(result.getStatus());
            application = applicationRepository.save(application);

            log.info("✓ Расчет завершен: статус={}, ставка={}%, платеж={}",
                    result.getStatus(), result.getRate(), result.getMonthlyPayment());

            sendNotification(client, application, result);

            // 7. Формируем ответ
            ApplicationResponseDTO response = mapper.toResponseDTO(application);
            response.setMessage(result.getMessage());

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Заявка обработана за {} мс", executionTime);

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

        MdcUtil.setOperation("SELECT_OFFER");
        MdcUtil.setApplicationId(request.getApplicationId());

        log.info("Выбор предложения: applicationId={}, offerId={}", request.getApplicationId(), request.getOfferId());

        try {
            // 1. Находим заявку
            Application application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> {
                        log.error("Заявка не найдена: {}", request.getApplicationId());
                        return new RuntimeException("Заявка не найдена");
                    });

            if (application.getClient() != null){
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

            // 3. Обновляем заявку выбранным предложением
            application.setAmount(offer.getAmount());
            application.setRate(offer.getRate());
            application.setMonthlyPayment(offer.getMonthlyPayment());
            application.setStatus("APPROVED");
            application = applicationRepository.save(application);

            log.info("✓ Заявка обновлена: статус=APPROVED");

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

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Кредит выдан: ID={}, сумма={}, общая сумма={}, срок={} мес., время={} мс",
                    credit.getId(),
                    credit.getAmount(),
                    credit.getTotalAmount(),
                    credit.getTerm(),
                    executionTime);

            return credit;

        } catch (Exception e)
        {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Ошибка выбора предложения через {} мс: {}", executionTime, e.getMessage());

            throw e;
        }
    }

    private void sendNotification(Client client, Application application, CreditCalculator.CalculationResult result) {

        MdcUtil.setOperation("SEND_NOTIFICATION");

        try {

            NotificationRequestDTO notification = new NotificationRequestDTO();
            notification.setEmail(client.getEmail());
            notification.setClientName(client.getFirstName() + " " + client.getLastName());
            notification.setSubject("Статус заявки №" + application.getId());
            notification.setMessage(result.getMessage());
            notification.setStatus(result.getStatus());
            notification.setApplicationId(application.getId());

            notificationClient.sendEmail(notification);
            log.info("Уведомление отправлено на {}", MdcUtil.getClientEmail());

        } catch (Exception e) {
            log.error("Ошибка отправки уведомления: {}", e.getMessage());
        }
    }
}