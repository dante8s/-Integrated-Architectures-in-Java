package org.example.integrated_architectures.user;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Email " + email + " is already registered");
    }
}
