package com.dinevista.model;

public class UserAccountRecord {
    private final long userId;
    private final String role;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String passwordHash;
    private final String accountStatus;

    public UserAccountRecord(long userId, String role, String firstName, String lastName,
                             String email, String passwordHash, String accountStatus) {
        this.userId = userId;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountStatus = accountStatus;
    }

    public long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getAccountStatus() { return accountStatus; }

    public String getDisplayName() {
        return (firstName + " " + lastName).trim();
    }
}
