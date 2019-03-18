package com.example.account.manager.project;

import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.User;
import com.example.account.manager.project.repositories.BankAccountRepository;
import com.example.account.manager.project.repositories.UserRepository;
import com.example.account.manager.project.services.BankAccountService;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(BankAccountService service, UserRepository users, BankAccountRepository accounts) {
    return args -> {
      User user1 = new User("Vladimir", "Dyakov");
      User user2 = new User("Daria", "Vasilueva");
      users.save(user1);
      users.save(user2);

      users.findAll().forEach(user -> {
        log.info("Preloaded " + user);
      });

      BankAccount bankAccount1 = service.createBankAccount(user1.getId());
      service.depositMoney(bankAccount1.getId(), new BigDecimal("20.20"));
      BankAccount bankAccount2 = service.createBankAccount(user2.getId());
      service.depositMoney(bankAccount2.getId(), new BigDecimal("10.10"));
      service.blockBankAccount(bankAccount2.getId());

      accounts.findAll().forEach(bankAccount -> {
        log.info("Preloaded " + bankAccount);
      });

    };
  }

}
