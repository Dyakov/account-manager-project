package com.example.account.manager.project.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@DataJpaTest
@ComponentScan("com.example.account.manager.project.services")
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
  @DirtiesContext
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
    bankAccountService.createBankAccount(1L);
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testDeleteBankAccount_validBankAccount_void() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    Long bankAccountId = bankAccount.getId();
    bankAccountService.deleteBankAccount(bankAccountId);
    assertEquals(false, accounts.findById(bankAccountId).isPresent());
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testDepositMoney_validBankAccountAndAmount_validBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccount = bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("10.5"));
    assertEquals(true, bankAccount.getBalance().doubleValue() == 10.5);
  }

  @Test
  public void testDepositMoney_invalidBankAccount_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:1");
    bankAccountService.depositMoney(1L, new BigDecimal("10.5"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testDepositMoney_invalidBankAccount_BankAccountIllegalStateException() {
    thrown.expect(BankAccountIllegalStateException.class);
    thrown.expectMessage("Bank account with id:1 blocked");
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccountService.blockBankAccount(bankAccount.getId());
    bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("10.5"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testWithdrawMoney_validBankAccountAndAmount_validBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("50.0"));
    bankAccount = bankAccountService.withdrawMoney(bankAccount.getId(), new BigDecimal("10.5"));
    assertTrue(bankAccount.getBalance().doubleValue() == 39.5);
  }

  @Test
  public void testWithdrawMoney_invalidBankAccount_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:1");
    bankAccountService.withdrawMoney(1L, new BigDecimal("10.5"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testWithdrawMoney_invalidBankAccount_BankAccountIllegalStateException() {
    thrown.expect(BankAccountIllegalStateException.class);
    thrown.expectMessage("Bank account with id:1 blocked");
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccountService.blockBankAccount(bankAccount.getId());
    bankAccountService.withdrawMoney(bankAccount.getId(), new BigDecimal("10.5"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testWithdrawMoney_validBankAccount_BankAccountWithdrawOperationException() {
    thrown.expect(BankAccountWithdrawOperationException.class);
    thrown.expectMessage("Could not withdraw money from bank account with id:1; amount:10.5");
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccountService.withdrawMoney(bankAccount.getId(), new BigDecimal("10.5"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testBlockBankAccount_validActiveBankAccount_vakidBlockedBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccount = bankAccountService.blockBankAccount(bankAccount.getId());
    assertEquals(BankAccountStatus.BLOCKED, bankAccount.getStatus());
  }

  @Test
  public void testBlockBankAccount_invalidBankAccount_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:1");
    bankAccountService.blockBankAccount(1L);
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testActivateBankAccount_validBlockedBankAccount_validActiveBankAccount() {
    User user = new User("Vladimir", "Dyakov");
    user = users.save(user);
    BankAccount bankAccount = bankAccountService.createBankAccount(user.getId());
    bankAccount = bankAccountService.blockBankAccount(bankAccount.getId());
    bankAccount = bankAccountService.activateBankAccount(bankAccount.getId());
    assertEquals(BankAccountStatus.ACTIVE, bankAccount.getStatus());
  }

  @Test
  public void testActivateBankAccount_invalidBankAccount_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:1");
    bankAccountService.activateBankAccount(1L);
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validBankAccountFromAndValidBankAccountTo_void() {
    User user1 = new User("Vladimir", "Dyakov");
    user1 = users.save(user1);
    User user2 = new User("Daria", "Vasilueva");
    user2 = users.save(user2);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(user1.getId());
    firstUserBankAccount = bankAccountService.depositMoney(firstUserBankAccount.getId(), new BigDecimal("20.10"));
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(user2.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), secondUserBankAccount.getId(), new BigDecimal("10.3"));
    firstUserBankAccount = accounts.findById(firstUserBankAccount.getId()).orElseThrow(IllegalStateException::new);
    secondUserBankAccount = accounts.findById(secondUserBankAccount.getId()).orElseThrow(IllegalStateException::new);
    assertTrue(firstUserBankAccount.getBalance().doubleValue() == 9.8);
    assertTrue(secondUserBankAccount.getBalance().doubleValue() == 10.3);
  }

  @Test
  public void testTransferMoney_invalidBankAccountFromAndValidBankAccountTo_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:2");
    User user2 = new User("Daria", "Vasilueva");
    user2 = users.save(user2);
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(user2.getId());
    bankAccountService.transferMoney(2L, secondUserBankAccount.getId(), new BigDecimal("10.3"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validBankAccountFromAndInvalidBankAccountTo_BankAccountNotFoundException() {
    thrown.expect(BankAccountNotFoundException.class);
    thrown.expectMessage("Could not find bank account with id:2");
    User user1 = new User("Vladimir", "Dyakov");
    user1 = users.save(user1);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(user1.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), 2L, new BigDecimal("10.3"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validBlockedBankAccountFromAndValidActiveBankAccountTo_BankAccountIllegalStateException() {
    thrown.expect(BankAccountIllegalStateException.class);
    thrown.expectMessage("Bank account with id:1 blocked");
    User user1 = new User("Vladimir", "Dyakov");
    user1 = users.save(user1);
    User user2 = new User("Daria", "Vasilueva");
    user2 = users.save(user2);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(user1.getId());
    firstUserBankAccount = bankAccountService.blockBankAccount(firstUserBankAccount.getId());
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(user2.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), secondUserBankAccount.getId(), new BigDecimal("10.3"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validActiveBankAccountFromAndValidBlockedBankAccountTo_BankAccountIllegalStateException() {
    thrown.expect(BankAccountIllegalStateException.class);
    thrown.expectMessage("Bank account with id:2 blocked");
    User user1 = new User("Vladimir", "Dyakov");
    user1 = users.save(user1);
    User user2 = new User("Daria", "Vasilueva");
    user2 = users.save(user2);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(user1.getId());
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(user2.getId());
    secondUserBankAccount = bankAccountService.blockBankAccount(secondUserBankAccount.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), secondUserBankAccount.getId(), new BigDecimal("10.3"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validBankAccountFromAndValidBankAccountTo_BankAccountWithdrawOperationException() {
    thrown.expect(BankAccountWithdrawOperationException.class);
    thrown.expectMessage("Could not withdraw money from bank account with id:1; amount:10.3");
    User user1 = new User("Vladimir", "Dyakov");
    user1 = users.save(user1);
    User user2 = new User("Daria", "Vasilueva");
    user2 = users.save(user2);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(user1.getId());
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(user2.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), secondUserBankAccount.getId(), new BigDecimal("10.3"));
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validBankAccountFromAndValidBankAccountToWithTheSameOwner_void() {
    User owner = new User("Vladimir", "Dyakov");
    owner = users.save(owner);
    BankAccount firstUserBankAccount = bankAccountService.createBankAccount(owner.getId());
    firstUserBankAccount = bankAccountService.depositMoney(firstUserBankAccount.getId(), new BigDecimal("20.10"));
    BankAccount secondUserBankAccount = bankAccountService.createBankAccount(owner.getId());
    bankAccountService.transferMoney(firstUserBankAccount.getId(), secondUserBankAccount.getId(), new BigDecimal("10.3"));
    firstUserBankAccount = accounts.findById(firstUserBankAccount.getId()).orElseThrow(IllegalStateException::new);
    secondUserBankAccount = accounts.findById(secondUserBankAccount.getId()).orElseThrow(IllegalStateException::new);
    assertTrue(firstUserBankAccount.getBalance().doubleValue() == 9.8);
    assertTrue(secondUserBankAccount.getBalance().doubleValue() == 10.3);
  }

  @Test
  @Transactional(propagation = Propagation.SUPPORTS)
  @DirtiesContext
  public void testTransferMoney_validSameBankAccounts_void() {
    User owner = new User("Vladimir", "Dyakov");
    owner = users.save(owner);
    BankAccount bankAccount = bankAccountService.createBankAccount(owner.getId());
    bankAccount = bankAccountService.depositMoney(bankAccount.getId(), new BigDecimal("20.10"));
    bankAccountService.transferMoney(bankAccount.getId(), bankAccount.getId(), new BigDecimal("10.3"));
    bankAccount = accounts.findById(bankAccount.getId()).orElseThrow(IllegalStateException::new);
    assertTrue(bankAccount.getBalance().doubleValue() == 20.1);
  }

}
