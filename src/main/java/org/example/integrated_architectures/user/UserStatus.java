package org.example.integrated_architectures.user;

public enum UserStatus {
    /** Can log in. Students get this status right after registration. */
    ACTIVE,
    /** Coach waiting for admin approval, cannot log in yet. */
    PENDING,
    /** Coach rejected by admin. */
    REJECTED
}
