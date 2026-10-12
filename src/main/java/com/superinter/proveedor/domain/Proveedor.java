package com.superinter.proveedor.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Proveedor que abastece de productos al supermercado (RF-04, RF-15).
 *
 * <p>Es inmutable: los cambios se hacen creando una nueva instancia con
 * {@code toBuilder()}. Los datos de contacto son opcionales y, cuando no
 * existen, se representan con {@code null} y nunca con una cadena vacía.
 */
@Getter
@Builder(toBuilder = true)
public class Proveedor {

    private final Integer idProveedor;
    private final String nombre;
    private final String contacto;
    private final String telefono;
    private final String correo;
}