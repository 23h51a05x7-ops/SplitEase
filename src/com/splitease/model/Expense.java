
package com.splitease.model;

public class Expense {

    private final String description;
    private final Member paidBy;
    private final double amount;

    public Expense(String description, Member paidBy, double amount) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                "Expense description cannot be empty."
            );
        }

        if (paidBy == null) {
            throw new IllegalArgumentException(
                "Payer cannot be null."
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Expense amount must be positive."
            );
        }

        this.description = description.trim();
        this.paidBy = paidBy;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public Member getPaidBy() {
        return paidBy;
    }

    public double getAmount() {
        return amount;
    }
}
