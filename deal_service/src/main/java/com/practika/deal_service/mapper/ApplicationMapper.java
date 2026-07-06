package com.practika.deal_service.mapper;

import com.practika.deal_service.dto.ApplicationRequestDTO;
import com.practika.deal_service.dto.ApplicationResponseDTO;
import com.practika.deal_service.entity.Application;
import com.practika.deal_service.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class ApplicationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Client toClient(ApplicationRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "rate", ignore = true)
    @Mapping(target = "monthlyPayment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract Application toApplicationWithClient(Client client, ApplicationRequestDTO dto);

    @Mapping(target = "clientName", expression = "java(app.getClient().getFirstName() + \" \" + app.getClient().getLastName())")
    @Mapping(target = "clientEmail", source = "client.email")
    @Mapping(target = "message", ignore = true)
    public abstract ApplicationResponseDTO toApplicationResponseDTO(Application app);

}