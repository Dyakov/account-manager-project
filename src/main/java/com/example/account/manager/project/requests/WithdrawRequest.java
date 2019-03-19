package com.example.account.manager.project.requests;

import lombok.Data;

/**
 * Created by Dyakov on 19.03.2019.
 */
@Data
public class WithdrawRequest {

  String amount;

  public WithdrawRequest() {
  }

  public WithdrawRequest(String amount) {
    this.amount = amount;
  }

}
