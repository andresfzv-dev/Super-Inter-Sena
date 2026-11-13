package com.superinter.usuario.dto;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada del registro público de usuarios (RF-17, HU4).
 *
 * <p>No tiene campo {@code rol}: por mínimo privilegio y por la regla de negocio 3 (solo el
 * administrador modifica parámetros), quien se registra no elige su rol. Si el JSON trae un
 * campo {@code rol} u otro desconocido, se ignora de forma explícita con
 * {@code @JsonIgnoreProperties}, en lugar de depender de la configuración global de Jackson.
 *
 * <p>Las longitudes máximas coinciden con las columnas de la tabla usuarios.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegistroRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Size(max = 150, message = "El correo no puede superar 150 caracteres")
        @Email(regexp = ".+@.+\\..+", message = "El correo no tiene un formato válido")
        String correo,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @MaximoBytesUtf8(value = 72,
                message = "La contraseña no puede superar 72 bytes (las letras con tilde y la ñ ocupan 2)")
        String contrasena) {

    /**
     * Constructor compacto: normaliza antes de validar.
     * El correo es el nombre de usuario: sin espacios y en minúsculas, para que
     * "Ana@Correo.com " y "ana@correo.com" sean el mismo usuario. La contraseña no se
     * modifica, porque los espacios y las mayúsculas son parte de ella.
     */
    public RegistroRequest {
        nombre = nombre == null ? null : nombre.strip();
        correo = normalizarCorreo(correo);
    }

    /**
     * Regla única de normalización del correo, compartida con el inicio de sesión para que
     * un usuario se busque exactamente como se guardó. Locale.ROOT evita conversiones que
     * dependan del idioma del servidor (por ejemplo, la "I" en turco).
     */
    public static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.strip().toLowerCase(Locale.ROOT);
    }
}
