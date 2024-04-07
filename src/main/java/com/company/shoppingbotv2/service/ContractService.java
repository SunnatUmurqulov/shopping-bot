package com.company.shoppingbotv2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.payload.ContractResponse;
import com.company.shoppingbotv2.payload.DebtCheckResponse;
import com.company.shoppingbotv2.repository.ContractRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;

    public List<Contract> saveUserContracts(User user, DebtCheckResponse response) {
        List<Contract> contracts = new ArrayList<>();
        contractRepository.deleteAll(user.getContracts());
        for (ContractResponse contractResponse : response.contractResponses()) {
            Contract contract = contractRepository.findByRemoteId(contractResponse.id())
                    .orElse(new Contract());
            updateContractAttributes(contract, user, contractResponse, response.currency());
            contracts.add(contract);
        }
        List<Contract> savedContracts = contractRepository.saveAll(contracts);
        user.setContracts(savedContracts);
        return savedContracts;
    }

    private void updateContractAttributes(Contract contract, User user, ContractResponse contractResponse, String ekvivalentCurrency) {
        contract.setUser(user);
        contract.setRemoteId(contractResponse.id());
        contract.setName(contractResponse.name());
        contract.setSum(contractResponse.sum());
        contract.setCurrency(contractResponse.currency());
        contract.setEkvivalent(contractResponse.ekvivalent());
        contract.setEkvivalentCurrency(ekvivalentCurrency);
    }

    public Contract getById(Integer id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new SomethingWentWrongException(
                        "Kontraktlarni saqlagandan so'ng kontrakt id si bo'yicha izlab kontraktni topa olmadi",
                        Map.of("id", id)
                ));
    }
}
