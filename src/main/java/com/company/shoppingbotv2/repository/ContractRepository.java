package com.company.shoppingbotv2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.company.shoppingbotv2.entity.Contract;

import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    boolean existsByRemoteId(String remoteId);

    Optional<Contract> findByRemoteId(String remoteId);
}
