package com.practika.deal_service.mapper;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.entity.Application;
import com.practika.deal_service.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Client toClientEntity(ApplicationRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rate", ignore = true)
    @Mapping(target = "monthlyPayment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Application toEntity(ApplicationRequestDTO dto);

    @Mapping(target = "clientName", expression = "java(app.getClient().getFirstName() + \" \" + app.getClient().getLastName())")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "message", ignore = true)
    ApplicationResponseDTO toResponseDTO(Application app);
}