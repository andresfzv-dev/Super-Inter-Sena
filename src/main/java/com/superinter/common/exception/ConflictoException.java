package com.superinter.common.exception;

/**
 * Se lanza cuando una operación entra en conflicto con el estado actual de los datos,
 * por ejemplo un código duplicado o un producto referenciado por ventas.
 * El manejador global la traduce a HTTP 409.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}