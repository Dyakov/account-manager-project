package com.example.account.manager.project.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import com.example.account.manager.project.assemblers.BankAccountResourceAssembler;
import com.example.account.manager.project.entities.BankAccount;
import com.example.account.manager.project.entities.BankAccountStatus;
import com.example.account.manager.project.exceptions.BankAccountNotFoundException;
import com.example.account.manager.project.repositories.BankAccountRepository;
import com.example.account.manager.project.services.BankAccountService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BankAccountController {

  private final BankAccountService service;
  private final BankAccountRepository repository;
  private final BankAccountResourceAssembler assembler;

  BankAccountController(BankAccountService service, BankAccountRepository repository, BankAccountResourceAssembler assembler) {
    this.service = service;
    this.repository = repository;
    this.assembler = assembler;
  }

  @GetMapping(value = "/bank/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
  public Resources<Resource<BankAccount>> all() {
    List<Resource<BankAccount>> accounts = repository.findAll().stream()
        .map(assembler::toResource)
        .collect(Collectors.toList());
    return new Resources<>(accounts, linkTo(methodOn(BankAccountController.class).all()).withSelfRel());
  }

  @GetMapping(value = "/bank/accounts/{bankAccountId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Resource<BankAccount> get(@PathVariable Long bankAccountId) {
    return assembler.toResource(repository.findById(bankAccountId).orElseThrow(()->new BankAccountNotFoundException(bankAccountId)));
  }

  @DeleteMapping("/bank/accounts/{bankAccountId}/block")
  public ResponseEntity<ResourceSupport> block(@PathVariable Long bankAccountId) {
    BankAccount bankAccount = repository.findById(bankAccountId).orElseThrow(()->new BankAccountNotFoundException(bankAccountId));
    if(bankAccount.getStatus() == BankAccountStatus.ACTIVE) {
      bankAccount = service.blockBankAccount(bankAccountId);
      return ResponseEntity.ok(assembler.toResource(bankAccount));
    }

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new VndErrors.VndError("Method not allowed", "You can't block bank account that is in the " + bankAccount.getStatus() + " status"));
  }

  @PutMapping("bank/accounts/{bankAccountId}/activate")
  public ResponseEntity<ResourceSupport> activate(@PathVariable Long bankAccountId) {
    BankAccount bankAccount = repository.findById(bankAccountId).orElseThrow(()->new BankAccountNotFoundException(bankAccountId));
    if(bankAccount.getStatus() == BankAccountStatus.BLOCKED) {
      bankAccount = service.activateBankAccount(bankAccountId);
      return ResponseEntity.ok(assembler.toResource(bankAccount));
    }

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new VndErrors.VndError("Method not allowed", "You can't activate bank account that is in the " + bankAccount.getStatus() + " status"));
  }

}
