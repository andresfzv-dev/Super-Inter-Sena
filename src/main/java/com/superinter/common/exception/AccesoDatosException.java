package com.superinter.common.exception;

/**
 * Envuelve los errores técnicos de acceso a datos (SQLException) para que las capas
 * superiores no dependan de la API de JDBC.
 * El manejador global la traduce a HTTP 500 sin exponer detalles internos.
 */
public class AccesoDatosException extends RuntimeException {

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}