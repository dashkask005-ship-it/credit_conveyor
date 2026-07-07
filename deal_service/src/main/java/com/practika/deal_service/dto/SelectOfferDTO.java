package com.practika.deal_service.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectOfferDTO {
    private Long applicationId;
    private Long offerId;
}