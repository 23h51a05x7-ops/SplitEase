
package com.splitease.service;

import com.splitease.model.Expense;
import com.splitease.model.Member;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SplitEaseService {

    private final List<Expense> expenses = new ArrayList<>();

    public void addExpense(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException(
                "Expense cannot be null."
            );
        }

        expenses.add(expense);
    }

    public double getTotalExpense() {
        double total = 0;

        for (Expense expense : expenses) {
            total += expense.getAmount();
        }

        return total;
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public Map<Member, Double> calculateBalances(
            List<Member> members) {

        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one member is required."
            );
        }

        double share = getTotalExpense() / members.size();

        Map<Member, Double> balances = new LinkedHashMap<>();

        for (Member member : members) {
            balances.put(member, -share);
        }

        for (Expense expense : expenses) {
            Member payer = expense.getPaidBy();

            if (!balances.containsKey(payer)) {
                throw new IllegalArgumentException(
                    "An expense payer is not part of the group."
                );
            }

            balances.put(
                payer,
                balances.get(payer) + expense.getAmount()
            );
        }

        return balances;
    }
    
public boolean areBalancesValid(Map<Member, Double> balances) {
    double total = 0;

    for (double balance : balances.values()) {
        total += balance;
    }

    return Math.abs(total) < 0.005;
}

}
