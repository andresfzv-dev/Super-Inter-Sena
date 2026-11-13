package com.superinter.usuario.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles del sistema, tomados de "Clases y características de usuarios" del documento de
 * especificación de requerimientos (GA1-220501093-AA4-EV02). Atiende RF-17 y RNF-02.
 *
 * <p>En la columna {@code usuarios.rol} se guarda el código ({@link #name()}), no el nombre
 * visible: el código no cambia si se corrige la redacción del nombre y coincide con la
 * restricción {@code chk_usuarios_rol} de la migración 003.
 */
@Getter
@RequiredArgsConstructor
public enum Rol {

    ADMINISTRADOR("Administrador"),
    GERENTE("Gerente"),
    AUXILIAR_INVENTARIOS("Auxiliar de inventarios"),
    CAJERO("Cajero"),
    COMPRAS("Compras");

    private final String nombreVisible;

    /**
     * Obtiene el rol a partir del código guardado en la base de datos.
     *
     * @throws IllegalArgumentException si el código no corresponde a ningún rol; no debería
     *                                  ocurrir porque la base de datos restringe los valores
     */
    public static Rol desdeCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(rol -> rol.name().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol desconocido: " + codigo));
    }
}
