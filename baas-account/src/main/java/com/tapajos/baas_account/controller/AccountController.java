package com.tapajos.baas_account.controller;

import com.tapajos.baas_account.domain.Account;
import com.tapajos.baas_account.domain.Transaction;
import com.tapajos.baas_account.exception.AccountBlockedException;
import com.tapajos.baas_account.exception.AccountNotFoundException;
import com.tapajos.baas_account.exception.ConcurrentModificationException;
import com.tapajos.baas_account.exception.CurrencyMismatchException;
import com.tapajos.baas_account.exception.InsufficientBalanceException;
import com.tapajos.baas_account.usecase.BlockAccount;
import com.tapajos.baas_account.usecase.Checkin;
import com.tapajos.baas_account.usecase.Checkout;
import com.tapajos.baas_account.usecase.CreateAccount;
import com.tapajos.baas_account.usecase.GetAccount;
import com.tapajos.baas_account.usecase.GetTransactions;
import com.tapajos.baas_account.usecase.UnblockAccount;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccount createAccount;
    private final GetAccount getAccount;
    private final Checkin checkin;
    private final Checkout checkout;
    private final BlockAccount blockAccount;
    private final UnblockAccount unblockAccount;
    private final GetTransactions getTransactions;

    public AccountController(CreateAccount createAccount, GetAccount getAccount,
                             Checkin checkin, Checkout checkout,
                             BlockAccount blockAccount, UnblockAccount unblockAccount,
                             GetTransactions getTransactions) {
        this.createAccount = createAccount;
        this.getAccount = getAccount;
        this.checkin = checkin;
        this.checkout = checkout;
        this.blockAccount = blockAccount;
        this.unblockAccount = unblockAccount;
        this.getTransactions = getTransactions;
    }

    @PostMapping
    public ResponseEntity<Account> create(@RequestBody CreateAccountRequest request) {
        Account account = createAccount.execute(request.onboardingId(), request.currency());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> get(@PathVariable String id) {
        return ResponseEntity.ok(getAccount.execute(id));
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<Transaction> checkin(
            @PathVariable String id,
            @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Transaction tx = checkin.execute(id, request.amount(), request.currency(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<Transaction> checkout(
            @PathVariable String id,
            @RequestBody TransactionRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Transaction tx = checkout.execute(id, request.amount(), request.currency(), idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(tx);
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<Account> block(@PathVariable String id) {
        return ResponseEntity.ok(blockAccount.execute(id));
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<Account> unblock(@PathVariable String id) {
        return ResponseEntity.ok(unblockAccount.execute(id));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> transactions(@PathVariable String id) {
        return ResponseEntity.ok(getTransactions.execute(id));
    }

    // ── exception handlers ─────────────────────────────────────────────────────

    @org.springframework.web.bind.annotation.ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
            AccountBlockedException.class,
            InsufficientBalanceException.class,
            CurrencyMismatchException.class
    })
    public ResponseEntity<Map<String, String>> handleUnprocessable(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConcurrentModificationException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConcurrentModificationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }
}
