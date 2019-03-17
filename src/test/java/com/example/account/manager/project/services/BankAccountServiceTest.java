package com.example.account.manager.project.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.BankAccountStatus;
import com.example.account.manager.project.entities.User;
import com.example.account.manager.project.exceptions.BankAccountIllegalStateException;
import com.example.account.manager.project.exceptions.BankAccountNotFoundException;
import com.example.account.manager.project.exceptions.UserNotFoundException;
import com.example.account.manager.project.repositories.BankAccountRepository;
import com.example.account.manager.project.repositories.UserRepository;
import java.math.BigDecimal;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringRunner.class)
@DataJpaTest
@ComponentScan("com.example.account.manager.project.services")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class BankAccountServiceTest {

  @Autowired
  UserRepository users;

  @Autowired
  BankAccountRepository accounts;

  @Autowired
  BankAccountService bankAccountService;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateBankAccount_validUser_validBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    assertNotNull(bankAccount.getId());
  }

  @Test
  public void testCreateBankAccount_invalidUser_UserNotFoundException() {
    thrown.expect(UserNotFoundException.class);
    thrown.expectMessage("Could not find bank account owner with id:1");
    BankAccount bankAccount = bankAccountService.createBankAccount(1L);
  }

  @Test
  public void testDeleteBankAccount_validBankAccount_void() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    Long bankAccountId = bankAccount.getId();
    bankAccountService.deleteBankAccount(bankAccount);
    bankAccount = null;
    assertEquals(false, accounts.findById(bankAccountId).isPresent());
  }

  @Test
  public void testDepositMoney_validBankAccountAndAmount_validBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    TestTransaction.flagForCommit();
    TestTransaction.end();
    bankAccount = bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("10.5"));
    System.out.println(bankAccount.getBalance().toString());
    assertEquals(true, bankAccount.getBalance().doubleValue() == 10.5);
  }

  @Test
  public void testDepositMoney_invalidBankAccount_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:1");
    bankAccountService.depositMoney(1L, new BigDecimal("10.5"));
  }

  @Test
  public void testDepositMoney_invalidBankAccount_BankAccountIllegalStateException() {
    thrown.expect(BankAccountIllegalStateException.class);
    thrown.expectMessage("Bank account with id:2 blocked");
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccount.setStatus(BankAccountStatus.BLOCKED);
    TestTransaction.flagForCommit();
    TestTransaction.end();
    bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("10.5"));
  }

}
