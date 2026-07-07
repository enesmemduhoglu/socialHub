package com.enes.social.common.exception;

/**
 * Kullanıcı kimliği doğrulanmış ancak işlem için yetkisiz olduğunda fırlatılır
 * (ör. başkasının gönderisini silmeye çalışmak). Global handler 403'e çevirir.
 *
 * <p>Not: Spring'in {@code AccessDeniedException}'ı yerine bilerek özel bir tip
 * kullanıyoruz; böylece hata, güvenlik filtresine takılmadan {@code @RestControllerAdvice}
 * tarafından tutarlı bir {@link ApiError} gövdesine dönüştürülür.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
