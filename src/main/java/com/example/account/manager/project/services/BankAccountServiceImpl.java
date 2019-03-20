package com.example.account.manager.project.services;

import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.BankAccountStatus;
import com.example.account.manager.project.entities.User;
import com.example.account.manager.project.exceptions.BankAccountIllegalStateException;
import com.example.account.manager.project.exceptions.BankAccountNotFoundException;
import com.example.account.manager.project.exceptions.BankAccountWithdrawOperationException;
import com.example.account.manager.project.exceptions.UserNotFoundException;
import com.example.account.manager.project.repositories.BankAccountRepository;
import com.example.account.manager.project.repositories.UserRepository;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class BankAccountServiceImpl implements BankAccountService{

  private final UserRepository users;
  private final BankAccountRepository bankAccounts;

  public BankAccountServiceImpl(UserRepository users, BankAccountRepository bankAccounts) {
    this.users = users;
    this.bankAccounts = bankAccounts;
  }

  @Override
  public BankAccount createBankAccount(Long ownerID) {
    User user = users.findById(ownerID).orElseThrow(() -> new UserNotFoundException(ownerID));
    BankAccount bankAccount = new BankAccount(new BigDecimal("0.00"), BankAccountStatus.ACTIVE, user);
    user.getBankAccounts().add(bankAccount);
    bankAccount = bankAccounts.save(bankAccount);
    log.info("Create bank account: " + bankAccount);
    return bankAccount;
  }

  @Override
  public void deleteBankAccount(Long bankAccountId) {
    BankAccount bankAccount = bankAccounts.findById(bankAccountId).orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
    bankAccounts.delete(bankAccount);
    log.info("Delete bank account: " + bankAccount);
  }

  @Override
  public BankAccount depositMoney(Long bankAccountId, BigDecimal amount) {
    BankAccount bankAccount = bankAccounts.findById(bankAccountId).orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
    checkAccountStatus(bankAccount);
    bankAccount.setBalance(bankAccount.getBalance().add(amount));
    log.info("Deposit money to bank account with id:" + bankAccountId + "; amount:" + amount);
    return bankAccount;
  }

  @Override
  public BankAccount withdrawMoney(Long bankAccountId, BigDecimal amount) {
    BankAccount bankAccount = bankAccounts.findById(bankAccountId).orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
    checkAccountStatus(bankAccount);
    //Счёт не может быть отрицательным
    if (bankAccount.getBalance().compareTo(amount) < 0) {
      log.info("Could not withdraw money from bank account with id:" + bankAccountId + "; amount:" + amount);
      throw new BankAccountWithdrawOperationException(bankAccountId, amount);
    }
    bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
    log.info("Withdraw money from bank account with id:" + bankAccountId + "; amount:" + amount);
    return bankAccount;
  }

  @Transactional(timeout = 10)
  @Override
  public void transferMoney(Long bankAccountIdFrom, Long bankAccountIdTo, BigDecimal amount) {
    //упорядочивание счетов по идентификатору для предотвращения взаимной блокировки
    BankAccount bankAccountFrom;
    BankAccount bankAccountTo;
    if(bankAccountIdTo.compareTo(bankAccountIdFrom) > 0) {
      bankAccountFrom = bankAccounts.findById(bankAccountIdFrom).orElseThrow(() -> new BankAccountNotFoundException(bankAccountIdFrom));
      bankAccountTo = bankAccounts.findById(bankAccountIdTo).orElseThrow(() -> new BankAccountNotFoundException(bankAccountIdTo));
    } else {
      bankAccountTo = bankAccounts.findById(bankAccountIdTo).orElseThrow(() -> new BankAccountNotFoundException(bankAccountIdTo));
      bankAccountFrom = bankAccounts.findById(bankAccountIdFrom).orElseThrow(() -> new BankAccountNotFoundException(bankAccountIdFrom));
    }
    checkAccountStatus(bankAccountFrom);
    checkAccountStatus(bankAccountTo);
    //Счёт не может быть отрицательным
    if (bankAccountFrom.getBalance().compareTo(amount) < 0) {
      log.info("Could not transfer money from bank account with id:" + bankAccountIdFrom + "; amount:" + amount);
      throw new BankAccountWithdrawOperationException(bankAccountIdFrom, amount);
    }
    bankAccountFrom.setBalance(bankAccountFrom.getBalance().subtract(amount));
    bankAccountTo.setBalance(bankAccountTo.getBalance().add(amount));
    log.info("Money was transferred from bank account id:" + bankAccountIdFrom + " to bank account id:" + bankAccountIdTo + " amount:" + amount);
  }

  @Override
  public BankAccount activateBankAccount(Long bankAccountId) {
    BankAccount bankAccount = bankAccounts.findById(bankAccountId).orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
    bankAccount.setStatus(BankAccountStatus.ACTIVE);
    log.info("Bank account was activated");
    return bankAccount;
  }

  @Override
  public BankAccount blockBankAccount(Long bankAccountId) {
    BankAccount bankAccount = bankAccounts.findById(bankAccountId).orElseThrow(() -> new BankAccountNotFoundException(bankAccountId));
    bankAccount.setStatus(BankAccountStatus.BLOCKED);
    log.info("Bank account was blocked");
    return bankAccount;
  }

  private void checkAccountStatus(BankAccount bankAccount) {
    if (bankAccount.getStatus().equals(BankAccountStatus.BLOCKED)) {
      log.info("Bank account with id:" + bankAccount.getId() +" blocked");
      throw new BankAccountIllegalStateException(bankAccount.getId());
    }
  }

}
