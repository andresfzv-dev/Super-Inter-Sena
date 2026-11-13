package com.superinter.common.exception;

/**
 * Se lanza cuando el inicio de sesión falla, sea porque el correo no está registrado o
 * porque la contraseña es incorrecta. Tiene un único mensaje para los dos casos, para que
 * la respuesta no revele qué correos existen en el sistema.
 * El manejador global la traduce a HTTP 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public static final String MENSAJE = "Correo o contraseña incorrectos";

    public CredencialesInvalidasException() {
        super(MENSAJE);
    }
}
