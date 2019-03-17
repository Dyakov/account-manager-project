package com.example.account.manager.project.repositories;

import com.example.account.manager.project.entities.BankAccount;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

  @Override
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<BankAccount> findById(Long aLong);
}
