package com.example.account.manager.project.assemblers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.example.account.manager.project.controllers.BankAccountController;
import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.BankAccountStatus;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class BankAccountResourceAssembler implements ResourceAssembler<BankAccount, Resource<BankAccount>> {

  @Override
  public Resource<BankAccount> toResource(BankAccount bankAccount) {
    Resource<BankAccount> bankAccountResource = new Resource<>(bankAccount,
          linkTo(methodOn(BankAccountController.class).get(bankAccount.getId())).withSelfRel(),
          linkTo(methodOn(BankAccountController.class).all()).withRel("bank/accounts")
        );

    if(bankAccount.getStatus() == BankAccountStatus.ACTIVE) {
      bankAccountResource.add(
          linkTo(methodOn(BankAccountController.class).block(bankAccount.getId())).withRel("block")
      );
    } else {
      bankAccountResource.add(
          linkTo(methodOn(BankAccountController.class).activate(bankAccount.getId())).withRel("activate")
      );
    }

    return bankAccountResource;
  }
}
