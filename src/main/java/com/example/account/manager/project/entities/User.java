package com.example.account.manager.project.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
public class User {

  @Id
  @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="USER_SEQ")
  @SequenceGenerator(name="USER_SEQ", sequenceName="USER_SEQ", allocationSize=1)
  private Long id;

  private String name;

  private String surname;

  @JsonIgnore
  @ToString.Exclude
  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private List<BankAccount> bankAccounts = new ArrayList<>();

  public User() {
  }

  public User(Long id, String name, String surname) {
    this.id = id;
    this.name = name;
    this.surname = surname;
  }

  public User(String name, String surname) {
    this.name = name;
    this.surname = surname;
  }

  public User(String name, String surname, List<BankAccount> bankAccounts) {
    this.name = name;
    this.surname = surname;
    this.bankAccounts = bankAccounts;
  }

}
