package com.practika.deal_service.repository;


import com.practika.deal_service.entity.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client,Long> {
}
