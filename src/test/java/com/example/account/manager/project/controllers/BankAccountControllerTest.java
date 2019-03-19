package com.example.account.manager.project.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import javax.security.auth.login.AccountNotFoundException;
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
import org.springframework.ui.ModelExtensionsKt;

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
    User user1 = new User("Vladidmir", "Dyakov");
    user1.setId(1L);
    User user2 = new User("Daria", "Vasilueva");
    user2.setId(2L);
    List<BankAccount> bankAccounts = new ArrayList<>();
    BankAccount bankAccount1 = new BankAccount(new BigDecimal("0.0"), BankAccountStatus.ACTIVE, user1);
    bankAccount1.setId(1L);
    bankAccounts.add(bankAccount1);
    BankAccount bankAccount2 = new BankAccount(new BigDecimal("10.01"), BankAccountStatus.BLOCKED, user1);
    bankAccount2.setId(2L);
    bankAccounts.add(bankAccount2);
    BankAccount bankAccount3 = new BankAccount(new BigDecimal("11.12"), BankAccountStatus.ACTIVE, user2);
    bankAccount3.setId(3L);
    bankAccounts.add(bankAccount3);
    BankAccount bankAccount4 = new BankAccount(new BigDecimal("15.06"), BankAccountStatus.BLOCKED, user2);
    bankAccount4.setId(4L);
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
    mockMvc.perform(put("/bank/accounts/3/deposit/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("3"));
  }

  @Test
  public void testDepositMoney_notExistedBankAccountIdAndValidResponse_BankAccountNotFoundException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new DepositRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/5/deposit/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testDepositMoney_blockedBankAccountIdAndValidResponse_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new DepositRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/4/deposit/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:4 blocked"));
  }

  @Test
  public void testWithdrawMoney_validBankAccountIdAndValidResponse_validBankAccount() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/3/withdraw/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("3"));
  }

  @Test
  public void testWithdrawMoney_notExistedBankAccountIdAndValidResponse_BankAccountNotFoundException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/5/withdraw/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testWithdrawMoney_blockedBankAccountIdAndValidResponse_BankAccountIllegalStateException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/4/withdraw/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
        .andExpect(status().isNotAcceptable())
        .andExpect(content().string("Bank account with id:4 blocked"));
  }

  @Test
  public void testWithdrawMoney_bankAccountWithZeroBalance_BankAccountWithdrawOperationException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new WithdrawRequest("11.05"));
    mockMvc.perform(put("/bank/accounts/1/withdraw/money").contentType(MediaType.APPLICATION_JSON_VALUE).content(request).characterEncoding("utf-8"))
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
  public void testTransferMoney_invalidBankAccountFromId_BankAccountNotFoundException() throws  Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(5L, 1L, "5.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Could not find bank account with id:5"));
  }

  @Test
  public void testTransferMoney_invalidBankAccountToId_BankAccountNotFoundException() throws  Exception {
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
  public void testTransferMoneybankAccountFromWithZeroBalance_BankAccountWithdrawOperationException() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String request = objectMapper.writeValueAsString(new TransferRequest(1L, 3L, "11.05"));
    mockMvc.perform(put("/bank/accounts").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(request))
        .andExpect(status().isPreconditionFailed())
        .andExpect(content().string("Could not withdraw money from bank account with id:1; amount:11.05"));
  }
}
