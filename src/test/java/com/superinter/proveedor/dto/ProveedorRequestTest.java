package com.superinter.proveedor.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ProveedorRequestTest {

    private static final Validator VALIDADOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void solicitudCompletaYValidaNoTieneErrores() {
        ProveedorRequest solicitud = new ProveedorRequest(
                "Postobón", "Laura Gómez", "+57 300 123 4567", "ventas@postobon.com");

        assertThat(VALIDADOR.validate(solicitud)).isEmpty();
    }

    @Test
    void soloElNombreEsObligatorio() {
        ProveedorRequest solicitud = new ProveedorRequest("Postobón", null, null, null);

        assertThat(VALIDADOR.validate(solicitud)).isEmpty();
    }

    @Test
    void nombreEnBlancoEsInvalido() {
        assertThat(camposInvalidos(new ProveedorRequest("   ", null, null, null)))
                .containsOnly("nombre");
    }

    @Test
    void nombreDeMasDeCienCaracteresEsInvalido() {
        assertThat(camposInvalidos(new ProveedorRequest("a".repeat(101), null, null, null)))
                .containsOnly("nombre");
    }

    @Test
    void telefonoConLetrasEsInvalido() {
        assertThat(camposInvalidos(new ProveedorRequest("Postobón", null, "300-ABC-4567", null)))
                .containsOnly("telefono");
    }

    @Test
    void correoSinDominioEsInvalido() {
        assertThat(camposInvalidos(new ProveedorRequest("Postobón", null, null, "ventas@postobon")))
                .containsOnly("correo");
    }

    @Test
    void losCamposOpcionalesVaciosSeConviertenEnNuloYElNombreSeRecorta() {
        ProveedorRequest solicitud = new ProveedorRequest("  Postobón  ", " ", "", "   ");

        assertThat(solicitud.nombre()).isEqualTo("Postobón");
        assertThat(solicitud.contacto()).isNull();
        assertThat(solicitud.telefono()).isNull();
        assertThat(solicitud.correo()).isNull();
        assertThat(VALIDADOR.validate(solicitud)).isEmpty();
    }

    private Set<String> camposInvalidos(ProveedorRequest solicitud) {
        return VALIDADOR.validate(solicitud).stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}