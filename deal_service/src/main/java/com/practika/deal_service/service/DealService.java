package com.practika.deal_service.service;


import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.entity.Application;
import com.practika.deal_service.entity.Client;
import com.practika.deal_service.mapper.ApplicationMapper;
import com.practika.deal_service.repository.ApplicationRepository;
import com.practika.deal_service.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class DealService {

    private final ClientRepository clientRepository;
    private final ApplicationRepository applicationRepository;
    private final CreditCalculator calculator;
    private final ApplicationMapper mapper;

    @Transactional
    public ApplicationResponseDTO createApplication(ApplicationRequestDTO request) {

        Client client = mapper.toClientEntity(request);
        client = clientRepository.save(client);


        Application application = mapper.toEntity(request);
        application.setClient(client);
        application.setStatus("NEW");
        application.setRate(BigDecimal.ZERO);
        application.setMonthlyPayment(BigDecimal.ZERO);
        application = applicationRepository.save(application);

        CreditCalculator.CalculationResult result = calculator.calculate(
                client,
                request.getAmount(),
                request.getTerm()
        );


        application.setRate(result.getRate());
        application.setMonthlyPayment(result.getMonthlyPayment());
        application.setStatus(result.getStatus());
        application = applicationRepository.save(application);


        ApplicationResponseDTO response = mapper.toResponseDTO(application);
        response.setMessage(result.getMessage());

        return response;
    }

}