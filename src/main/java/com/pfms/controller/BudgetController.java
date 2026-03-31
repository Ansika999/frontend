package com.pfms.controller;

import com.pfms.dto.AuthDto;
import com.pfms.dto.BudgetDto;
import com.pfms.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    @Autowired private BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDto.Response>> getAll() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/month")
    public ResponseEntity<List<BudgetDto.Response>> getByMonth(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(budgetService.getBudgetsByMonth(month, year));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody BudgetDto.Request request) {
        try {
            return ResponseEntity.ok(budgetService.createBudget(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BudgetDto.Request request) {
        try {
            return ResponseEntity.ok(budgetService.updateBudget(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            budgetService.deleteBudget(id);
            return ResponseEntity.ok(new AuthDto.MessageResponse("Budget deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }
}
