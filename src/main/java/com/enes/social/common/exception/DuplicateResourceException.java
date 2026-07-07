package com.enes.social.common.exception;

/**
 * Benzersiz olması gereken bir kaynak (ör. username/email) zaten mevcut olduğunda fırlatılır.
 * Global handler tarafından 409 Conflict'e çevrilir.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
