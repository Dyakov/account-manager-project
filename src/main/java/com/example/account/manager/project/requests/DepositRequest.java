package com.example.account.manager.project.requests;

import lombok.Data;

/**
 * Created by Dyakov on 19.03.2019.
 */
@Data
public class DepositRequest {

  String amount;

  public DepositRequest() {
  }

  public DepositRequest(String amount) {
    this.amount = amount;
  }
}
