package com.superinter.usuario.dto;

/**
 * Respuesta de un inicio de sesión correcto.
 *
 * @param mensaje   confirmación pedida por la evidencia: "Autenticación satisfactoria"
 * @param token     JWT que el cliente envía en la cabecera {@code Authorization}
 * @param tipoToken esquema de la cabecera: {@code Authorization: Bearer <token>}
 * @param expiraEn  segundos de vida del token desde su emisión
 * @param usuario   datos públicos del usuario autenticado
 */
public record LoginResponse(
        String mensaje,
        String token,
        String tipoToken,
        long expiraEn,
        UsuarioResponse usuario) {

    public static final String MENSAJE_EXITO = "Autenticación satisfactoria";
    public static final String TIPO_TOKEN = "Bearer";

    public static LoginResponse exitoso(String token, long expiraEn, UsuarioResponse usuario) {
        return new LoginResponse(MENSAJE_EXITO, token, TIPO_TOKEN, expiraEn, usuario);
    }
}
