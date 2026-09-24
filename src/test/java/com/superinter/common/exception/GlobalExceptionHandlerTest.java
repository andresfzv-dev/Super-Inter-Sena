package com.superinter.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

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