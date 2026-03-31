package com.pfms.controller;

import com.pfms.dto.AuthDto;
import com.pfms.dto.TransactionDto;
import com.pfms.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for transaction CRUD and analytics.
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<TransactionDto.Response>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getTransactions(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transactionService.getTransactionById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TransactionDto.Request request) {
        try {
            return ResponseEntity.ok(transactionService.createTransaction(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody TransactionDto.Request request) {
        try {
            return ResponseEntity.ok(transactionService.updateTransaction(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.ok(new AuthDto.MessageResponse("Transaction deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<List<TransactionDto.Response>> filter(
            @RequestBody TransactionDto.FilterRequest filter) {
        return ResponseEntity.ok(transactionService.getFilteredTransactions(filter));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<TransactionDto.Response>> getRecent() {
        return ResponseEntity.ok(transactionService.getRecentTransactions());
    }

    @GetMapping("/category-summary")
    public ResponseEntity<List<TransactionDto.CategorySummary>> getCategorySummary(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(transactionService.getCategorySummary(month, year));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<TransactionDto.MonthlySummary>> getMonthlyTrend() {
        return ResponseEntity.ok(transactionService.getMonthlyTrend());
    }
}
