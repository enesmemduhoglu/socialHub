package com.enes.social.common.exception;

/**
 * İstek anlamsal olarak geçersiz olduğunda fırlatılır (ör. kendini takip etme).
 * Global handler tarafından 400'e çevrilir.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
