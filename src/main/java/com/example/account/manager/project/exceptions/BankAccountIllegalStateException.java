package com.example.account.manager.project.exceptions;

public class BankAccountIllegalStateException extends IllegalStateException {

  public BankAccountIllegalStateException(Long bankAccountId) {
    super("Bank account with id:" + bankAccountId +" blocked");
  }
}
