
package com.splitease.model;

public class Settlement {

    private final Member from;
    private final Member to;
    private final double amount;

    public Settlement(Member from, Member to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Member getFrom() {
        return from;
    }

    public Member getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }

    public void display() {
        System.out.println(
            from.getName() + " pays " +
            to.getName() + " Rs. " + amount
        );
    }
}
