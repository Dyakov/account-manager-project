package com.example.account.manager.project.exceptions.handlers;

import com.example.account.manager.project.exceptions.BankAccountIllegalStateException;
import com.example.account.manager.project.exceptions.BankAccountNotFoundException;
import com.example.account.manager.project.exceptions.BankAccountWithdrawOperationException;
import com.example.account.manager.project.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Dyakov on 19.03.2019.
 */
@ControllerAdvice
public class ApplicationExceptionAdvice {

  @ResponseBody
  @ExceptionHandler(UserNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String userNotFoundHandler(UserNotFoundException e) {
    return e.getMessage();
  }

  @ResponseBody
  @ExceptionHandler(BankAccountNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  String bankAccountNotFoundHandler(BankAccountNotFoundException e) {
    return e.getMessage();
  }

  @ResponseBody
  @ExceptionHandler(BankAccountWithdrawOperationException.class)
  @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
  String bankAccountWithdrawOperationHandler(BankAccountWithdrawOperationException e) {
    return e.getMessage();
  }

  @ResponseBody
  @ExceptionHandler(BankAccountIllegalStateException.class)
  @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
  String bankAccountIllegalStateHandler(BankAccountIllegalStateException e) {
    return e.getMessage();
  }
}
