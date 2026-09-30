package com.superinter.common.web;

/**
 * Conversión segura de los parámetros de texto que llegan en las peticiones HTTP.
 */
public final class ParametrosWeb {

    private ParametrosWeb() {
    }

    /**
     * Convierte un parámetro a entero. Devuelve null si está vacío o no es un número válido.
     */
    public static Integer enteroOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(valor.strip());
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }
}