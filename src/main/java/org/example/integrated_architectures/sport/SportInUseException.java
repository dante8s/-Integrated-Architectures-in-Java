package org.example.integrated_architectures.sport;

public class SportInUseException extends RuntimeException {

    public SportInUseException(String name) {
        super("Sport \"" + name + "\" cannot be deleted: some coaches teach it");
    }
}
