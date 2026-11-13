package com.superinter.usuario.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import com.superinter.common.security.JwtProperties;
import com.superinter.common.security.SeguridadConfig;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import com.superinter.usuario.dto.UsuarioResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * Verifica los tokens con el mismo JwtEncoder y JwtDecoder que configura la aplicación.
 * Cada prueba usa un secreto aleatorio generado en el momento, no el de .env.
 */
class TokenServiceTest {

    private static final Duration EXPIRACION = Duration.ofMinutes(60);

    private final SeguridadConfig configuracion = new SeguridadConfig();
    private final JwtProperties propiedades = propiedadesConSecretoAleatorio();
    private final TokenService tokenService =
            new TokenService(configuracion.jwtEncoder(propiedades), propiedades);
    private final JwtDecoder jwtDecoder = configuracion.jwtDecoder(propiedades);

    private final Usuario usuario = Usuario.builder()
            .idUsuario(7)
            .nombre("Ana Pérez")
            .correo("ana.perez@ejemplo.com")
            .contrasenaCifrada("$2a$10$hash-que-no-debe-aparecer-en-el-token")
            .rol(Rol.CAJERO)
            .build();

    @Test
    void elTokenSeDecodificaYTieneLosClaimsEsperados() {
        Jwt token = jwtDecoder.decode(tokenService.generar(usuario));

        // getIssuer() intenta convertir el claim en URL; el emisor de esta aplicación es un nombre.
        assertThat(token.getClaimAsString("iss")).isEqualTo("super-inter");
        assertThat(token.getSubject()).isEqualTo("7");
        assertThat(token.getClaimAsString("nombre")).isEqualTo("Ana Pérez");
        assertThat(token.getClaimAsString("correo")).isEqualTo("ana.perez@ejemplo.com");
        assertThat(token.getClaimAsString("rol")).isEqualTo("CAJERO");
        assertThat(Duration.between(token.getIssuedAt(), token.getExpiresAt())).isEqualTo(EXPIRACION);
        assertThat(token.getHeaders()).containsEntry("alg", "HS256");
    }

    @Test
    void elTokenNoLlevaLaContrasenaNiClaimsAdicionales() {
        Jwt token = jwtDecoder.decode(tokenService.generar(usuario));

        assertThat(token.getClaims().keySet())
                .containsExactlyInAnyOrder("iss", "sub", "iat", "exp", "nombre", "correo", "rol");
        assertThat(token.getTokenValue()).doesNotContain("hash-que-no-debe-aparecer");
    }

    @Test
    void leerUsuarioReconstruyeLosDatosDesdeElToken() {
        Jwt token = jwtDecoder.decode(tokenService.generar(usuario));

        assertThat(tokenService.leerUsuario(token))
                .isEqualTo(new UsuarioResponse(7, "Ana Pérez", "ana.perez@ejemplo.com", Rol.CAJERO));
    }

    @Test
    void unTokenFirmadoConOtroSecretoEsRechazado() {
        JwtProperties otrasPropiedades = propiedadesConSecretoAleatorio();
        TokenService otroEmisor = new TokenService(configuracion.jwtEncoder(otrasPropiedades), otrasPropiedades);
        String tokenAjeno = otroEmisor.generar(usuario);

        assertThatThrownBy(() -> jwtDecoder.decode(tokenAjeno))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenVencidoEsRechazado() {
        // Emitido hace dos horas con vigencia de una: venció hace una hora, más que la
        // tolerancia de 60 segundos que aplica el validador por diferencias de reloj.
        Clock haceDosHoras = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        TokenService emisorEnElPasado =
                new TokenService(configuracion.jwtEncoder(propiedades), propiedades, haceDosHoras);
        String tokenVencido = emisorEnElPasado.generar(usuario);

        assertThatThrownBy(() -> jwtDecoder.decode(tokenVencido))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void segundosDeExpiracionCorrespondenALaConfiguracion() {
        assertThat(tokenService.segundosDeExpiracion()).isEqualTo(3600);
    }

    private static JwtProperties propiedadesConSecretoAleatorio() {
        byte[] secreto = new byte[32];
        new SecureRandom().nextBytes(secreto);
        return new JwtProperties(Base64.getEncoder().encodeToString(secreto), "super-inter", EXPIRACION);
    }
}
