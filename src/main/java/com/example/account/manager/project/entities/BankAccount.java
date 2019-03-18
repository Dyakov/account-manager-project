package com.example.account.manager.project.entities;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import lombok.Data;

@Data
@Entity
public class BankAccount {

  @Id
  @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="BANK_ACCOUNT_SEQ")
  @SequenceGenerator(name="BANK_ACCOUNT_SEQ", sequenceName="BANK_ACCOUNT_SEQ", allocationSize=1)
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
