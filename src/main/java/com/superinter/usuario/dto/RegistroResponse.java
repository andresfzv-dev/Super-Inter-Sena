package com.superinter.usuario.dto;

/**
 * Respuesta del registro: un mensaje de confirmación y los datos públicos del usuario creado.
 */
public record RegistroResponse(String mensaje, UsuarioResponse usuario) {

    public static final String MENSAJE_EXITO = "Usuario registrado correctamente";

    public static RegistroResponse exitoso(UsuarioResponse usuario) {
        return new RegistroResponse(MENSAJE_EXITO, usuario);
    }
}
