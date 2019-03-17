package com.example.account.manager.project.exceptions;

import java.math.BigDecimal;

public class BankAccountWithdrawOperationException extends UnsupportedOperationException {

  public BankAccountWithdrawOperationException(Long bankAccountId, BigDecimal amount) {
    super("Could not withdraw money from bank account with id:" + bankAccountId + "; amount:" + amount);
  }
}
