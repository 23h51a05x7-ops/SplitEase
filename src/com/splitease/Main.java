
package com.splitease;

import com.splitease.model.Member;
import com.splitease.model.Expense;
import com.splitease.model.Settlement;
import com.splitease.service.SplitEaseService;
import com.splitease.strategy.GreedySettlementStrategy;
import com.splitease.strategy.SettlementStrategy;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static void clearPreviousData() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement()) {

        stmt.executeUpdate("DELETE FROM settlements");
        stmt.executeUpdate("DELETE FROM expenses");
        stmt.executeUpdate("DELETE FROM members");
        }
    }
    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        SplitEaseService service = new SplitEaseService();
        Map<String, Member> members = new LinkedHashMap<>();

        try {
            clearPreviousData();
            System.out.print("Enter number of members: ");
            int n = Integer.parseInt(sc.nextLine());

            if (n <= 0) {
                System.out.println("Number of members must be positive.");
                return;
            }

            // Enter members and save them to MySQL
            for (int i = 0; i < n; i++) {
                System.out.print("Enter member " + (i + 1) + " name: ");
                String name = sc.nextLine().trim();

                if (name.isBlank() || members.containsKey(name)) {
                    System.out.println("Invalid or duplicate name. Try again.");
                    i--;
                    continue;
                }

                try (Connection con = DatabaseConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO members (name) VALUES (?)")) {

                    ps.setString(1, name);
                    ps.executeUpdate();

                    members.put(name, new Member(name));

                } catch (SQLException e) {
                    System.out.println(
                        "Could not save member. The name may already exist."
                    );
                    i--;
                }
            }

            System.out.print("Enter number of expenses: ");
            int expenseCount = Integer.parseInt(sc.nextLine());

            if (expenseCount < 0) {
                System.out.println("Expense count cannot be negative.");
                return;
            }

            // Enter expenses and save them to MySQL
            for (int i = 0; i < expenseCount; i++) {
                System.out.print("Enter expense description: ");
                String description = sc.nextLine().trim();

                System.out.print("Who paid? Enter member name: ");
                String payerName = sc.nextLine().trim();

                Member payer = members.get(payerName);

                if (payer == null) {
                    System.out.println("Unknown member. Try again.");
                    i--;
                    continue;
                }

                System.out.print("Enter amount: ");
                double amount = Double.parseDouble(sc.nextLine());

                if (!Double.isFinite(amount) || amount <= 0) {
                    System.out.println("Enter a valid positive amount.");
                    i--;
                    continue;
                }

                Expense expense = new Expense(description, payer, amount);

                try (Connection con = DatabaseConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO expenses (description, amount, paid_by) "
                         + "VALUES (?, ?, (SELECT member_id FROM members "
                         + "WHERE name = ?))")) {

                    ps.setString(1, description);
                    ps.setBigDecimal(2, BigDecimal.valueOf(amount));
                    ps.setString(3, payerName);
                    ps.executeUpdate();

                    service.addExpense(expense);

                } catch (SQLException e) {
                    System.out.println("Could not save expense: "
                        + e.getMessage());
                    i--;
                }
            }

            // Calculate balances
            double totalExpense = service.getTotalExpense();
            List<Member> memberList =
                new ArrayList<>(members.values());

            Map<Member, Double> balances =
                service.calculateBalances(memberList);

            double share = totalExpense / memberList.size();

            System.out.printf("%nTotal group expense: Rs. %.2f%n",
                totalExpense);
            System.out.printf("Each member's share: Rs. %.2f%n", share);

            System.out.println("\n--- Member Balances ---");

            for (Map.Entry<Member, Double> entry : balances.entrySet()) {
                System.out.printf("%s: Rs. %.2f%n",
                    entry.getKey().getName(), entry.getValue());
            }

            if (!service.areBalancesValid(balances)) {
                System.out.println("Error: Balances do not add up to zero.");
                return;
            }

            // Calculate and save settlements
            SettlementStrategy strategy = new GreedySettlementStrategy();
            List<Settlement> settlements =
                strategy.calculateSettlements(balances);

            System.out.println("\n--- Suggested Settlements ---");

            if (settlements.isEmpty()) {
                System.out.println("Everyone is settled. No payments needed.");
            } else {
                for (Settlement settlement : settlements) {

                    try (Connection con = DatabaseConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(
                             "INSERT INTO settlements "
                             + "(from_member_id, to_member_id, amount) "
                             + "VALUES ((SELECT member_id FROM members "
                             + "WHERE name = ?), "
                             + "(SELECT member_id FROM members "
                             + "WHERE name = ?), ?)")) {

                        ps.setString(1, settlement.getFrom().getName());
                        ps.setString(2, settlement.getTo().getName());
                        ps.setBigDecimal(3,
                            BigDecimal.valueOf(settlement.getAmount()));

                        ps.executeUpdate();
                        settlement.display();
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}
// to connect sql to java command:
//javac -cp ".;mysql-connector-j-26.10.0.jar" -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
//java -cp ".;mysql-connector-j-26.10.0.jar;out" com.splitease.Main
