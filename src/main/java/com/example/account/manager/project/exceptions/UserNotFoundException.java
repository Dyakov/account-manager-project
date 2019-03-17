package com.example.account.manager.project.exceptions;

public class UserNotFoundException extends RuntimeException{

  public UserNotFoundException(Long ownerId) {
    super("Could not find bank account owner with id:" + ownerId);
  }
}
