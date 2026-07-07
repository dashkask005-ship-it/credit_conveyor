package com.practika.deal_service.controller;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.dto.SelectOfferDTO;
import com.practika.deal_service.entity.Credit;
import com.practika.deal_service.service.DealService;
import com.practika.deal_service.util.MdcUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DealController {

    private final DealService dealService;

    @PostMapping("/application")
    public ResponseEntity<ApplicationResponseDTO> createApplication(@RequestBody ApplicationRequestDTO request) {

        MdcUtil.setOperation("CONTROLLER_CREATE_APPLICATION");

        log.info("Получен запрос на создание заявки {} {}", request.getFirstName(), request.getLastName());
        log.debug("Детали запроса: сумма={}, срок={}, цель={}, доход={}", request.getAmount(), request.getTerm(), request.getPurpose(), request.getMonthlyIncome());

        ApplicationResponseDTO response = dealService.createApplication(request);

        log.info("Отправлен ответ: ID={}, статус={}", response.getId(), response.getStatus());

        return ResponseEntity.ok(dealService.createApplication(request));
    }

    @PostMapping("/offer/select")
    public ResponseEntity<Credit> selectOffer(@RequestBody SelectOfferDTO request) {
        MdcUtil.setOperation("CONTROLLER_SELECT_OFFER");

        log.info("Получен запрос на выбор предложения: appId={}, offerId={}", request.getApplicationId(), request.getOfferId());

        Credit credit = dealService.selectOffer(request);

        log.info("Отправлен ответ: creditId={}, сумма={}", credit.getId(), credit.getAmount());

        return ResponseEntity.ok(credit);
    }
}