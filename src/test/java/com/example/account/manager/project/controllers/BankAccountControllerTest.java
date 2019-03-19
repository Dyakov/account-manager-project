package com.example.account.manager.project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.BankAccountStatus;
import com.example.account.manager.project.entities.User;
import com.example.account.manager.project.exceptions.BankAccountIllegalStateException;
import com.example.account.manager.project.exceptions.BankAccountNotFoundException;
import com.example.account.manager.project.exceptions.BankAccountWithdrawOperationException;
import com.example.account.manager.project.exceptions.UserNotFoundException;
import com.example.account.manager.project.repositories.BankAccountRepository;
import com.example.account.manager.project.repositories.UserRepository;
import com.example.account.manager.project.requests.DepositRequest;
import com.example.account.manager.project.requests.TransferRequest;
import com.example.account.manager.project.requests.WithdrawRequest;
import com.example.account.manager.project.services.BankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Created by Dyakov on 19.03.2019.
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@WebMvcTest(BankAccountController.class)
@ComponentScan(basePackages = {
    "com.example.account.manager.project.assemblers",
    "com.example.account.manager.project.services",
    "com.example.account.manager.project.exceptions",
    "com.example.account.manager.project.requests",
    "com.example.account.manager.project.entities"
})
public class BankAccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  UserRepository userRepository;

  @MockBean
  BankAccountRepository bankAccountRepository;

  @MockBean
  private BankAccountService service;

  @Before
  public void setUp() {
    User user1 = new User(1L,"Vladidmir", "Dyakov");
    User user2 = new User(2L, "Daria", "Vasilueva");
    List<BankAccount> bankAccounts = new ArrayList<>();
    BankAccount bankAccount1 = new BankAccount(1L, new BigDecimal("0.0"), BankAccountStatus.ACTIVE, user1);
    bankAccounts.add(bankAccount1);
    BankAccount bankAccount2 = new BankAccount(2L, new BigDecimal("10.01"), BankAccountStatus.BLOCKED, user1);
    bankAccounts.add(bankAccount2);
    BankAccount bankAccount3 = new BankAccount(3L, new BigDecimal("11.12"), BankAccountStatus.ACTIVE, user2);
    bankAccounts.add(bankAccount3);
    BankAccount bankAccount4 = new BankAccount(4L, new BigDecimal("15.06"), BankAccountStatus.BLOCKED, user2);
    bankAccounts.add(bankAccount4);
    Mockito.when(bankAccountRepository.findAll()).thenReturn(bankAccounts);
    Mockito.when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(bankAccount1));
    Mockito.when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(bankAccount2));
    Mockito.when(bankAccountRepository.findById(3L)).thenReturn(Optional.of(bankAccount3));
    Mockito.when(service.depositMoney(eq(3L), any(BigDecimal.class))).thenReturn(bankAccount3);
    Mockito.when(service.depositMoney(eq(5L), any(BigDecimal.class))).thenThrow(new BankAccountNotFoundException(5L));
    Mockito.when(service.depositMoney(eq(4L), any(BigDecimal.class))).thenThrow(new BankAccountIllegalStateException(4L));
    Mockito.when(service.withdrawMoney(eq(1L), any(BigDecimal.class))).thenThrow(new BankAccountWithdrawOperationException(1L, new BigDecimal("11.05")));
    Mockito.when(service.withdrawMoney(eq(3L), any(BigDecimal.class))).thenReturn(bankAccount3);
    Mockito.when(service.withdrawMoney(eq(5L), any(BigDecimal.class))).thenThrow(new BankAccountNotFoundException(5L));
    Mockito.when(service.withdrawMoney(eq(4L), any(BigDecimal.class))).thenThrow(new BankAccountIllegalStateException(4L));
    Mockito.doThrow(new BankAccountNotFoundException(5L)).when(service).transferMoney(eq(5L), eq(1L), any(BigDecimal.class));
    Mockito.doThrow(new BankAccountNotFoundException(5L)).when(service).transferMoney(eq(3L), eq(5L), any(BigDecimal.class));
    Mockito.doThrow(new BankAccountIllegalStateException(2L)).when(service).transferMoney(eq(2L), eq(1L), any(BigDecimal.class));
    Mockito.doThrow(new BankAccountIllegalStateException(4L)).when(service).transferMoney(eq(3L), eq(4L), any(BigDecimal.class));
    Mockito.doThrow(new BankAccountWithdrawOperationException(1L, new BigDecimal("11.05"))).when(service).transferMoney(eq(1L), eq(3L), any(BigDecimal.class));
    Mockito.when(service.createBankAccount(eq(1L))).thenReturn(bankAccount1);
    Mockito.when(service.createBankAccount(eq(3L))).thenThrow(new UserNotFoundException(3L));
    BankAccount blockedBankAccount1 = new BankAccount(1L, new BigDecimal("0.0"), BankAccountStatus.BLOCKED, user1);
    Mockito.when(service.blockBankAccount(1L)).thenReturn(blockedBankAccount1);
    Mockito.when(service.blockBankAccount(2L)).thenReturn(bankAccount2);
    Mockito.when(bankAccountRepository.findById(5L)).thenThrow(new BankAccountNotFoundException(5L));
    BankAccount activeBankAccount2 = new BankAccount(2L, new BigDecimal("10.01"), BankAccountStatus.ACTIVE, user1);
    Mockito.when(service.activateBankAccount(2L)).thenReturn(activeBankAccount2);
    Mockito.when(service.activateBankAccount(1L)).thenReturn(bankAccount1);
    Mockito.doThrow(new BankAccountNotFoundException(5L)).when(service).deleteBankAccount(5L);
  }

  @Test
  public void testGetAll_void_AllBankAccountsWithLinks() throws Exception {
    mockMvc.perform(get("/bank/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.bankAccountList[0].id").value("1"))
        .andExpect(jsonPath("$._embedded.bankAccountList[1].id").value("2"))
        .andExpect(jsonPath("$._embedded.bankAccountList[2].id").value("3"))
        .andExpect(jsonPath("$._embedded.bankAccountList[3].id").value("4"))

        .andExpect(jsonPath("$._embedded.bankAccountList[0].balance").value("0.0"))
        .andExpect(jsonPath("$._embedded.bankAccountList[1].balance").value("10.01"))
        .andExpect(jsonPath("$._embedded.bankAccountList[2].balance").value("11.12"))
        .andExpect(jsonPath("$._embedded.bankAccountList[3].balance").value("15.06"))

        .andExpect(jsonPath("$._embedded.bankAccountList[0].status").value("ACTIVE"))
        .andExpect(jsonPath("$._embedded.bankAccountList[1].status").value("BLOCKED"))
        .andExpect(jsonPath("$._embedded.bankAccountList[2].status").value("ACTIVE"))
        .andExpect(jsonPath("$._embedded.bankAccountList[3].status").value("BLOCKED"))

        .andExpect(jsonPath("$._links.self.href").value("http://localhost/bank/accounts"));
  }

  @Test
  public void testGet_activeBankAccountId_bankAccountWithLinks() throws Exception {
    mockMvc.perform(get("/bank/accounts/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$._links.bank/accounts.href").value("http://localhost/bank/accounts"))
        .andExpect(jsonPath("$._links.self.href").value("http://localhost/bank/accounts/1"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/bank/accounts/1/delete"))
        .andExpect(jsonPath("$._links.deposit.href").value("http://localhost/bank/accounts/1/deposit/money"))
        .andExpect(jsonPath("$._links.withdraw.href").value("http://localhost/bank/accounts/1/withdraw/money"))
        .andExpect(jsonPath("$._links.block.href").value("http://localhost/bank/accounts/1/block"))
        .andExpect(jsonPath("$._links.activate.href").doesNotExist());
  }

  @Test
  public void testGet_blockedBankAccountId_bankAccountWithLinks() throws Exception {
    mockMvc.perform(get("/bank/accounts/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("2"))
        .andExpect(jsonPath("$._links.bank/accounts.href").value("http://localhost/bank/accounts"))
        .andExpect(jsonPath("$._links.self.href").value("http://localhost/bank/accounts/2"))
        .andExpect(jsonPath("$._links.delete.href").value("http://localhost/bank/accounts/2/delete"))
        .andExpect(jsonPath("$._links.activate.href").value("http://localhost/bank/accounts/2/activate"))
        .andExpect(jsonPath("$._links.deposit.href").doesNotExist())
        .andExpect(jsonPath("$._links.withdraw.href").doesNotExist())
        .andExpect(jsonPath("$._links.block.href").doesNotExist());
  }

  @Test
  public void testGet_notExistedBankAccountId_HttpResponseWithStatusNotFound() throws Exception {
    mockMvc.perform(get("/bank/accounts/5"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testDepositMoney_validBankAccountIdAndValidResponse_validBankAccount() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new DepositRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/3/deposit/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("3"));
  }

  @Test
  public void testDepositMoney_notExistedBankAccountIdAndValidResponse_BankAccountNotFoundException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new DepositRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/5/deposit/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testDepositMoney_blockedBankAccountIdAndValidResponse_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new DepositRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/4/deposit/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:4 blocked"));
  }

  @Test
  public void testWithdrawMoney_validBankAccountIdAndValidResponse_validBankAccount() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/3/withdraw/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("3"));
  }

  @Test
  public void testWithdrawMoney_notExistedBankAccountIdAndValidResponse_BankAccountNotFoundException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/5/withdraw/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testWithdrawMoney_blockedBankAccountIdAndValidResponse_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/4/withdraw/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:4 blocked"));
  }

  @Test
  public void testWithdrawMoney_bankAccountWithZeroBalance_BankAccountWithdrawOperationException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/1/withdraw/money").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isPreconditionFailed())
        .andExpect(content().string("Could not withdraw money from bank account with id:1; amount:11.05"));
  }

  @Test
  public void testTransferMoney_validBankAccounts_AllBankAccountsWithLink() throws Exception{
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(3L, 1L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.bankAccountList[0].id").value("1"))
        .andExpect(jsonPath("$._embedded.bankAccountList[1].id").value("2"))
        .andExpect(jsonPath("$._embedded.bankAccountList[2].id").value("3"))
        .andExpect(jsonPath("$._embedded.bankAccountList[3].id").value("4"))
        .andExpect(jsonPath("$._links.self.href").value("http://localhost/bank/accounts"));
  }

  @Test
  public void testTransferMoney_notExistedBankAccountFromId_BankAccountNotFoundException() throws  Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(5L, 1L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testTransferMoney_notExistedBankAccountToId_BankAccountNotFoundException() throws  Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(3L, 5L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testTransferMoney_blockedBankAccountFrom_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(2L, 1L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:2 blocked"));
  }

  @Test
  public void testTransferMoney_blockedBankAccountTo_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(3L, 4L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:4 blocked"));
  }

  @Test
  public void testTransferMoney_bankAccountFromWithZeroBalance_BankAccountWithdrawOperationException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(1L, 3L, "11.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isPreconditionFailed())
        .andExpect(content().string("Could not withdraw money from bank account with id:1; amount:11.05"));
  }

  @Test
  public void testNewBankAccount_validUser_validBankAccount() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    User user = new User(1L,"Vladidmir", "Dyakov");
    String request = objectMapper.writeValueAsString(user);
    mockMvc.perform(post("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("1"));
  }

  @Test
  public void testNewBankAccount_invalidUser_UserNotFoundException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    User user = new User(3L,"Ivan", "Ivanov");
    String request = objectMapper.writeValueAsString(user);
    mockMvc.perform(post("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account owner with id:3"));
  }

  @Test
  public void testBlock_activeBankAccount_blockedBankAccount() throws Exception {
    mockMvc.perform(delete("/bank/accounts/1/block"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("1"))
        .andExpect(jsonPath("$.status").value("BLOCKED"));
  }

  @Test
  public void testBlock_blockedBankAccount_MethodNotAllowedResponse() throws Exception {
    mockMvc.perform(delete("/bank/accounts/2/block"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.logref").value("Method not allowed"))
        .andExpect(jsonPath("$.message").value("You can't block bank account that is in the BLOCKED status"));
  }

  @Test
  public void testBlock_notExistedBankAccountId_BankAccountNotFoundException() throws Exception {
    mockMvc.perform(delete("/bank/accounts/5/block"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testActivate_blockedBankAccount_activeBankAccount() throws Exception {
    mockMvc.perform(put("/bank/accounts/2/activate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("2"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  public void testActivate_activeBankAccount_MethodNotAllowedResponse() throws Exception {
    mockMvc.perform(put("/bank/accounts/1/activate"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.logref").value("Method not allowed"))
        .andExpect(jsonPath("$.message").value("You can't activate bank account that is in the ACTIVE status"));
  }

  @Test
  public void testActivate_notExistedBankAccountId_BankAccountNotFoundException() throws Exception {
    mockMvc.perform(put("/bank/accounts/5/activate"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testDeleteBankAccount_validBankAccountId_AllBankAccountsWithLinks() throws Exception {
    mockMvc.perform(delete("/bank/accounts/1/delete"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.bankAccountList[0].id").value("1"))
        .andExpect(jsonPath("$._embedded.bankAccountList[1].id").value("2"))
        .andExpect(jsonPath("$._embedded.bankAccountList[2].id").value("3"))
        .andExpect(jsonPath("$._embedded.bankAccountList[3].id").value("4"))
        .andExpect(jsonPath("$._links.self.href").value("http://localhost/bank/accounts"));
  }

  @Test
  public void testDeleteBankAccount_notExistedBankAccountId_BankAccountNotFoundException() throws Exception {
    mockMvc.perform(delete("/bank/accounts/5/delete"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));

  }
}
