package com.example.account.manager.project.services;

import com.example.account.manager.project.entities.BankAccount;
import java.math.BigDecimal;

public interface BankAccountService {
  BankAccount createBankAccount(Long ownerID);
  void deleteBankAccount(Long bankAccountId);
  BankAccount depositMoney(Long bankAccountId, BigDecimal amount);
  BankAccount withdrawMoney(Long bankAccountId, BigDecimal amount);
  void transferMoney(Long bankAccountIdFrom, Long bankAccountIdTo, BigDecimal amount);
  BankAccount activateBankAccount(Long bankAccountId);
  BankAccount blockBankAccount(Long bankAccountId);
}
