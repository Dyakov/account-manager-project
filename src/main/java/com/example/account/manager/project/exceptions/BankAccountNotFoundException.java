package com.example.account.manager.project.exceptions;

public class BankAccountNotFoundException extends RuntimeException {

  public BankAccountNotFoundException(Long bankAccountId) {
    super("Could not find bank account with id:" + bankAccountId);
  }
}
