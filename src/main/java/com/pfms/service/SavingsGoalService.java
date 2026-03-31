package com.pfms.service;

import com.pfms.dto.SavingsGoalDto;
import com.pfms.entity.SavingsGoal;
import com.pfms.entity.User;
import com.pfms.repository.SavingsGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsGoalService {

    @Autowired private SavingsGoalRepository goalRepository;
    @Autowired private AuthService authService;

    @Transactional
    public SavingsGoalDto.Response createGoal(SavingsGoalDto.Request request) {
        User user = authService.getCurrentUser();
        SavingsGoal goal = SavingsGoal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null ? request.getCurrentAmount() : BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .icon(request.getIcon())
                .color(request.getColor())
                .user(user)
                .build();
        return mapToResponse(goalRepository.save(goal));
    }

    @Transactional
    public SavingsGoalDto.Response updateGoal(Long id, SavingsGoalDto.Request request) {
        User user = authService.getCurrentUser();
        SavingsGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        if (request.getIcon() != null) goal.setIcon(request.getIcon());
        if (request.getColor() != null) goal.setColor(request.getColor());
        return mapToResponse(goalRepository.save(goal));
    }

    @Transactional
    public SavingsGoalDto.Response addContribution(Long id, BigDecimal amount) {
        User user = authService.getCurrentUser();
        SavingsGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoal.GoalStatus.COMPLETED);
        }
        return mapToResponse(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(Long id) {
        User user = authService.getCurrentUser();
        SavingsGoal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalRepository.delete(goal);
    }

    public List<SavingsGoalDto.Response> getAllGoals() {
        User user = authService.getCurrentUser();
        return goalRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private SavingsGoalDto.Response mapToResponse(SavingsGoal g) {
        SavingsGoalDto.Response r = new SavingsGoalDto.Response();
        r.setId(g.getId());
        r.setTitle(g.getTitle());
        r.setDescription(g.getDescription());
        r.setTargetAmount(g.getTargetAmount());
        r.setCurrentAmount(g.getCurrentAmount());
        r.setTargetDate(g.getTargetDate());
        r.setStatus(g.getStatus());
        r.setProgressPercentage(g.getProgressPercentage());
        r.setIcon(g.getIcon());
        r.setColor(g.getColor());
        if (g.getCreatedAt() != null)
            r.setCreatedAt(g.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return r;
    }
}
