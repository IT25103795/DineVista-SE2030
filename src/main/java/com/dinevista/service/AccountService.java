package com.dinevista.service;

import com.dinevista.model.UserAccountRecord;
import com.dinevista.repository.AccountRepository;
import com.dinevista.repository.DuplicateEmailException;
import com.dinevista.util.ManagerTokenVerifier;
import com.dinevista.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class AccountService {
    public static final String CUSTOMER = "CUSTOMER";
    public static final String MANAGER = "MANAGER";

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PHONE = Pattern.compile("^(?:\\+94|0)7\\d{8}$");
    private static final String DUMMY_PASSWORD_HASH =
            PasswordUtil.hash("DineVista authentication timing placeholder");
    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public OperationResult<UserAccountRecord> authenticate(String email, String password, String requiredRole) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || password == null || password.isEmpty()) {
            return OperationResult.failure("Enter your email and password.");
        }
        Optional<UserAccountRecord> account = repository.findByEmailAndRole(normalizedEmail, requiredRole);
        String storedHash = account.map(UserAccountRecord::getPasswordHash).orElse(DUMMY_PASSWORD_HASH);
        boolean passwordMatches = PasswordUtil.verify(password, storedHash);
        if (account.isEmpty() || !"ACTIVE".equals(account.get().getAccountStatus()) || !passwordMatches) {
            return OperationResult.failure("Invalid email or password for this portal.");
        }
        return OperationResult.success(account.get());
    }

    public OperationResult<UserAccountRecord> register(String role, String firstName, String lastName,
                                                        String email, String phone, String password,
                                                        String confirmPassword, String managerToken) {
        List<String> errors = validate(firstName, lastName, email, phone, password, confirmPassword);
        if (MANAGER.equals(role) && !ManagerTokenVerifier.isValid(managerToken)) {
            errors.add("The manager registration token is invalid.");
        }
        if (!CUSTOMER.equals(role) && !MANAGER.equals(role)) {
            errors.add("Unsupported account type.");
        }
        if (!errors.isEmpty()) return OperationResult.failure(errors);

        String normalizedEmail = normalizeEmail(email);
        if (repository.emailExists(normalizedEmail)) {
            return OperationResult.failure("An account already uses that email address.");
        }
        try {
            UserAccountRecord account = repository.create(
                    role, clean(firstName), clean(lastName), normalizedEmail, clean(phone),
                    PasswordUtil.hash(password));
            return OperationResult.success(account);
        } catch (DuplicateEmailException ex) {
            return OperationResult.failure(ex.getMessage());
        }
    }

    private List<String> validate(String firstName, String lastName, String email, String phone,
                                  String password, String confirmPassword) {
        List<String> errors = new ArrayList<>();
        validateName("First name", firstName, errors);
        validateName("Last name", lastName, errors);
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.length() > 160 || !EMAIL.matcher(normalizedEmail).matches()) {
            errors.add("Enter a valid email address.");
        }
        String cleanPhone = clean(phone);
        if (!PHONE.matcher(cleanPhone).matches()) {
            errors.add("Enter a valid Sri Lankan mobile number.");
        }
        if (password == null || password.length() < 8 || password.length() > 128) {
            errors.add("Password must contain between 8 and 128 characters.");
        }
        if (password == null || !password.equals(confirmPassword)) {
            errors.add("Passwords do not match.");
        }
        return errors;
    }

    private void validateName(String label, String name, List<String> errors) {
        int length = clean(name).length();
        if (length < 2 || length > 80) {
            errors.add(label + " must contain between 2 and 80 characters.");
        }
    }

    private String normalizeEmail(String email) {
        return clean(email).toLowerCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
