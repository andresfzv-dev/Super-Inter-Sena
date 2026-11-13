package com.superinter.common.security;

import java.time.Duration;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración de los tokens JWT, leída de las propiedades {@code superinter.jwt.*}.
 *
 * <p>Se valida al arrancar: si {@code JWT_SECRET} falta, no está en Base64 o es demasiado
 * corto, la aplicación no inicia. Así un secreto débil se detecta en el despliegue y no
 * cuando ya se emitieron tokens que cualquiera podría falsificar.
 *
 * @param secreto    clave compartida en Base64 para firmar y verificar los tokens (HS256)
 * @param emisor     valor del claim {@code iss}; el decodificador rechaza tokens de otro emisor
 * @param expiracion tiempo de vida de cada token
 */
@Validated
@ConfigurationProperties("superinter.jwt")
public record JwtProperties(
        @NotBlank(message = "Defina JWT_SECRET (genérelo con: openssl rand -base64 32)")
        String secreto,

        @NotBlank(message = "Defina superinter.jwt.emisor")
        String emisor,

        @NotNull(message = "Defina JWT_EXPIRACION, por ejemplo 60m")
        Duration expiracion) {

    /**
     * HS256 exige una clave de al menos 256 bits (RFC 7518, sección 3.2); con menos,
     * Nimbus se niega a firmar y el secreto sería vulnerable a fuerza bruta.
     */
    static final int BYTES_MINIMOS_SECRETO = 32;

    private static final String ALGORITMO_HMAC = "HmacSHA256";

    /**
     * Regla de validación del secreto. Se expresa como getter booleano para que Bean
     * Validation la reporte junto con las demás, con un mensaje que indica cómo corregirla.
     * Un secreto vacío se deja a {@code @NotBlank} para no repetir el error.
     */
    @AssertTrue(message = "JWT_SECRET debe estar en Base64 y tener al menos 32 bytes "
            + "(genérelo con: openssl rand -base64 32)")
    public boolean isSecretoValido() {
        if (secreto == null || secreto.isBlank()) {
            return true;
        }
        try {
            return decodificarSecreto().length >= BYTES_MINIMOS_SECRETO;
        } catch (IllegalArgumentException excepcion) {
            return false;
        }
    }

    @AssertTrue(message = "JWT_EXPIRACION debe ser mayor que cero")
    public boolean isExpiracionPositiva() {
        return expiracion == null || expiracion.isPositive();
    }

    /**
     * Clave HMAC a partir del secreto. Se construye cada vez en lugar de guardarla en el
     * record porque un arreglo de bytes rompería la igualdad e inmutabilidad del record.
     */
    public SecretKey claveSecreta() {
        return new SecretKeySpec(decodificarSecreto(), ALGORITMO_HMAC);
    }

    // strip(): un espacio o retorno de carro copiado al final de la línea del .env
    // haría fallar la decodificación aunque el secreto sea correcto.
    private byte[] decodificarSecreto() {
        return Base64.getDecoder().decode(secreto.strip());
    }
}
