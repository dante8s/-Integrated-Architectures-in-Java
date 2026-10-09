package org.example.integrated_architectures.sport;

public class SportNameAlreadyUsedException extends RuntimeException {

    public SportNameAlreadyUsedException(String name) {
        super("Sport \"" + name + "\" already exists");
    }
}
