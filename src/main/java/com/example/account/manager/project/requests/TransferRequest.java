package com.example.account.manager.project.requests;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Created by Dyakov on 19.03.2019.
 */
@Data
public class TransferRequest {

  Long bankAccountIdFrom;
  Long bankAccountIdTo;
  String amount;

  public TransferRequest() {
  }

  public TransferRequest(Long bankAccountIdFrom, Long bankAccountIdTo, String amount) {
    this.bankAccountIdFrom = bankAccountIdFrom;
    this.bankAccountIdTo = bankAccountIdTo;
    this.amount = amount;
  }
}
