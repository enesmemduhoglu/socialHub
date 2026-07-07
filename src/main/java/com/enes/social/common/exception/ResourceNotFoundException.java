package com.enes.social.common.exception;

/**
 * İstenen kaynak bulunamadığında fırlatılır. Global handler tarafından 404'e çevrilir.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
