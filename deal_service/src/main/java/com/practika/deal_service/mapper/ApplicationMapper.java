package com.practika.deal_service.mapper;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.entity.Application;
import com.practika.deal_service.entity.Client;
import com.practika.deal_service.entity.Offer;
import com.practika.deal_service.service.CreditCalculator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    public abstract Client toClient(ApplicationRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", source = "client")
    @Mapping(target = "amount", source = "dto.amount")
    @Mapping(target = "term", source = "dto.term")
    @Mapping(target = "purpose", source = "dto.purpose")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "rate", constant = "0")
    @Mapping(target = "monthlyPayment", constant = "0")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    public abstract Application toApplicationWithClient(Client client, ApplicationRequestDTO dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "term", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "rate", source = "result.rate")
    @Mapping(target = "monthlyPayment", source = "result.monthlyPayment")
    @Mapping(target = "status", source = "result.status")
    public abstract void updateApplicationWithResult(@MappingTarget Application application, CreditCalculator.CalculationResult result);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    @Mapping(target = "term", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "status", constant = "APPROVED")
    @Mapping(target = "amount", source = "offer.amount")
    @Mapping(target = "rate", source = "offer.rate")
    @Mapping(target = "monthlyPayment", source = "offer.monthlyPayment")
    public abstract void updateApplicationWithOffer(@MappingTarget Application application, Offer offer);


    @Mapping(target = "clientName", expression = "java(app.getClient().getFirstName() + \" \" + app.getClient().getLastName())")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "message", ignore = true)
    public abstract ApplicationResponseDTO toApplicationResponseDTO(Application app);

    public ApplicationResponseDTO toApplicationResponseDTO(Application app, String message) {
        ApplicationResponseDTO dto = toApplicationResponseDTO(app);
        dto.setMessage(message);
        return dto;
    }

    public void setApplicationForOffers(List<Offer> offers, Application application) {
        if (offers != null && application != null) {
            for (Offer offer : offers) {
                offer.setApplication(application);
            }
        }
    }
}