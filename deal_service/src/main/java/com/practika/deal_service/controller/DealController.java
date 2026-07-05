package com.practika.deal_service.controller;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.dto.SelectOfferDTO;
import com.practika.deal_service.entity.Credit;
import com.practika.deal_service.service.DealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @PostMapping("/application")
    public ResponseEntity<ApplicationResponseDTO> createApplication(@RequestBody ApplicationRequestDTO request) {
        return ResponseEntity.ok(dealService.createApplication(request));
    }

    @PostMapping("/offer/select")
    public ResponseEntity<Credit> selectOffer(@RequestBody SelectOfferDTO request) {
        Credit credit = dealService.selectOffer(request);
        return ResponseEntity.ok(credit);
    }
}