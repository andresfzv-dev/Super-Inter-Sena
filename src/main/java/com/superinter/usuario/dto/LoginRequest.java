package com.superinter.usuario.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales del inicio de sesión: el correo es el nombre de usuario (RNF-01).
 *
 * <p>Solo se exige que los campos no estén vacíos. Las reglas de formato y longitud son del
 * registro; aquí no se aplican para no responder 400 con detalles que ayuden a adivinar
 * credenciales: cualquier dato incorrecto termina en el mismo 401.
 */
public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        String contrasena) {

    /** Normaliza el correo con la misma regla del registro para encontrar al usuario. */
    public LoginRequest {
        correo = RegistroRequest.normalizarCorreo(correo);
    }
}
