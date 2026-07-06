package com.practika.deal_service.integration;



import com.practika.deal_service.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "notification-service",
        url = "${services.notification.url:http://localhost:8081}"
)
public interface NotificationClient {

    @PostMapping("/api/notification/send")
    void sendEmail(@RequestBody NotificationRequestDTO request);
}