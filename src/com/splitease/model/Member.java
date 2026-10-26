
package com.splitease.model;

public class Member {

    private final String name;

    public Member(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Member name cannot be empty."
            );
        }
        this.name = name.trim();
    }
    
    public String getName() {
        return name;
    }
}
