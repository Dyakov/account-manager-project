package com.example.account.manager.project.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class BankAccount {

  @Id
  @GeneratedValue
  private Long id;

  private BigDecimal balance;

  @Enumerated(EnumType.STRING)
  @Column(length = 7)
  private BankAccountStatus status;

  @ManyToOne
  @JoinColumn(name = "USER_ID")
  private User user;

  public BankAccount() {
  }

  public BankAccount(BigDecimal balance, BankAccountStatus status, User user) {
    this.balance = balance;
    this.status = status;
    this.user = user;
  }
}
