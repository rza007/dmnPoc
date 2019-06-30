package org.camunda.bpm.example.drg;

import java.time.LocalDateTime;
import java.util.List;

public class DelegationRule {
    private List<Rule> rules;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String output;

    public DelegationRule(List<Rule> rules, String userId, LocalDateTime startDate, LocalDateTime endDate, String output) {
        this.rules = rules;
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.output = output;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
