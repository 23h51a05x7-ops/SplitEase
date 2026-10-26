
package com.splitease.strategy;

import com.splitease.model.Member;
import com.splitease.model.Settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class GreedySettlementStrategy implements SettlementStrategy {

    private static final double EPSILON = 0.005;

    @Override
    public List<Settlement> calculateSettlements(
            Map<Member, Double> balances) {

        List<Settlement> settlements = new ArrayList<>();

        PriorityQueue<Map.Entry<Member, Double>> creditors =
                new PriorityQueue<>(
                    (a, b) -> Double.compare(
                        b.getValue(), a.getValue()
                    )
                );

        PriorityQueue<Map.Entry<Member, Double>> debtors =
                new PriorityQueue<>(
                    (a, b) -> Double.compare(
                        a.getValue(), b.getValue()
                    )
                );

        for (Map.Entry<Member, Double> entry : balances.entrySet()) {
            double balance = entry.getValue();

            if (balance > EPSILON) {
                creditors.offer(entry);
            } else if (balance < -EPSILON) {
                debtors.offer(entry);
            }
        }

        while (!creditors.isEmpty() && !debtors.isEmpty()) {

            Map.Entry<Member, Double> creditor = creditors.poll();
            Map.Entry<Member, Double> debtor = debtors.poll();

            double amount = Math.min(
                creditor.getValue(),
                -debtor.getValue()
            );

            settlements.add(
                new Settlement(
                    debtor.getKey(),
                    creditor.getKey(),
                    amount
                )
            );

            double creditorRemaining =
                    creditor.getValue() - amount;

            double debtorRemaining =
                    debtor.getValue() + amount;

            if (creditorRemaining > EPSILON) {
                creditors.offer(
                    Map.entry(
                        creditor.getKey(),
                        creditorRemaining
                    )
                );
            }

            if (debtorRemaining < -EPSILON) {
                debtors.offer(
                    Map.entry(
                        debtor.getKey(),
                        debtorRemaining
                    )
                );
            }
        }

        return settlements;
    }
}
