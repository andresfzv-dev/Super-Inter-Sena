package com.superinter.common.exception;

/**
 * Se lanza cuando se busca un recurso por su identificador y no existe.
 * El manejador global la traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object identificador) {
        super(String.format("%s con id %s no existe", recurso, identificador));
    }
}