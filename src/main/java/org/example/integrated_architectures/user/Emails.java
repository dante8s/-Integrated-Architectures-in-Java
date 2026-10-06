package org.example.integrated_architectures.user;

import java.util.Locale;

/**
 * Email helpers shared by registration and login.
 */
public final class Emails {

    private Emails() {
    }

    // "  Ivan@Mail.COM " and "ivan@mail.com" must be the same account.
    public static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
