package com.example.account.manager.project.repositories;

import com.example.account.manager.project.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

}
