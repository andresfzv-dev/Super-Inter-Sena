package com.superinter.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler manejador = new GlobalExceptionHandler();

    @Test
    void recursoNoEncontradoRespondeNotFoundConElMensaje() {
        ProblemDetail problema = manejador.manejarRecursoNoEncontrado(
                new RecursoNoEncontradoException("Producto", 99));

        assertThat(problema.getStatus()).isEqualTo(404);
        assertThat(problema.getDetail()).isEqualTo("Producto con id 99 no existe");
    }

    @Test
    void conflictoRespondeConflictConElMensaje() {
        ProblemDetail problema = manejador.manejarConflicto(
                new ConflictoException("Ya existe un producto con el código 7701234567890"));

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getDetail()).isEqualTo("Ya existe un producto con el código 7701234567890");
    }

    @Test
    void credencialesInvalidasRespondeUnauthorizedConMensajeGenerico() {
        ProblemDetail problema = manejador.manejarCredencialesInvalidas(new CredencialesInvalidasException());

        assertThat(problema.getStatus()).isEqualTo(401);
        assertThat(problema.getTitle()).isEqualTo("Error en la autenticación");
        assertThat(problema.getDetail()).isEqualTo("Correo o contraseña incorrectos");
    }

    @Test
    void peticionSinTokenRespondeUnauthorizedPidiendoElToken() {
        ProblemDetail problema = manejador.manejarNoAutenticado(
                new InsufficientAuthenticationException("Full authentication is required"));

        assertThat(problema.getStatus()).isEqualTo(401);
        assertThat(problema.getTitle()).isEqualTo("No autenticado");
        assertThat(problema.getDetail()).startsWith("Se requiere un token de acceso");
    }

    @Test
    void tokenInvalidoRespondeUnauthorizedSinExponerElMotivoTecnico() {
        ProblemDetail problema = manejador.manejarNoAutenticado(
                new InvalidBearerTokenException("Jwt expired at 2026-01-01T00:00:00Z"));

        assertThat(problema.getStatus()).isEqualTo(401);
        assertThat(problema.getTitle()).isEqualTo("No autenticado");
        assertThat(problema.getDetail())
                .startsWith("El token de acceso no es válido o está vencido")
                .doesNotContain("Jwt expired");
    }

    @Test
    void accesoDenegadoRespondeForbidden() {
        ProblemDetail problema = manejador.manejarAccesoDenegado(new AccessDeniedException("Access Denied"));

        assertThat(problema.getStatus()).isEqualTo(403);
        assertThat(problema.getTitle()).isEqualTo("Acceso denegado");
    }

    @Test
    void accesoDatosRespondeErrorInternoSinExponerDetalles() {
        ProblemDetail problema = manejador.manejarAccesoDatos(
                new AccesoDatosException("Error al consultar productos",
                        new SQLException("relation \"productos\" does not exist")));

        assertThat(problema.getStatus()).isEqualTo(500);
        assertThat(problema.getDetail())
                .doesNotContain("productos")
                .doesNotContain("relation");
    }

    @Test
    void errorInesperadoRespondeErrorInternoSinExponerDetalles() {
        ProblemDetail problema = manejador.manejarErrorInesperado(
                new IllegalStateException("estado interno corrupto"));

        assertThat(problema.getStatus()).isEqualTo(500);
        assertThat(problema.getDetail()).doesNotContain("estado interno");
    }
}