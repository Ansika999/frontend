package com.pfms.controller;

import com.pfms.dto.AuthDto;
import com.pfms.dto.SavingsGoalDto;
import com.pfms.service.SavingsGoalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/savings-goals")
public class SavingsGoalController {

    @Autowired private SavingsGoalService savingsGoalService;

    @GetMapping
    public ResponseEntity<List<SavingsGoalDto.Response>> getAll() {
        return ResponseEntity.ok(savingsGoalService.getAllGoals());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SavingsGoalDto.Request request) {
        try {
            return ResponseEntity.ok(savingsGoalService.createGoal(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody SavingsGoalDto.Request request) {
        try {
            return ResponseEntity.ok(savingsGoalService.updateGoal(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<?> contribute(@PathVariable Long id,
                                         @Valid @RequestBody SavingsGoalDto.ContributionRequest request) {
        try {
            return ResponseEntity.ok(savingsGoalService.addContribution(id, request.getAmount()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            savingsGoalService.deleteGoal(id);
            return ResponseEntity.ok(new AuthDto.MessageResponse("Goal deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse(e.getMessage()));
        }
    }
}
