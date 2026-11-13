package com.superinter.usuario.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class RegistroRequestTest {

    private static final Validator VALIDADOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static final String NOMBRE = "Ana Pérez";
    private static final String CORREO = "ana.perez@ejemplo.com";
    private static final String CONTRASENA = "Clave-Segura-2026";

    @Test
    void solicitudCompletaYValidaNoTieneErrores() {
        assertThat(VALIDADOR.validate(new RegistroRequest(NOMBRE, CORREO, CONTRASENA))).isEmpty();
    }

    @Test
    void losTresCamposSonObligatorios() {
        assertThat(camposInvalidos(new RegistroRequest(null, null, null)))
                .containsExactlyInAnyOrder("nombre", "correo", "contrasena");
    }

    @Test
    void nombreYCorreoEnBlancoSonInvalidos() {
        assertThat(camposInvalidos(new RegistroRequest("   ", "  ", CONTRASENA)))
                .containsExactlyInAnyOrder("nombre", "correo");
    }

    @Test
    void nombreDeMasDeCienCaracteresEsInvalido() {
        assertThat(camposInvalidos(new RegistroRequest("a".repeat(101), CORREO, CONTRASENA)))
                .containsOnly("nombre");
        assertThat(camposInvalidos(new RegistroRequest("a".repeat(100), CORREO, CONTRASENA)))
                .isEmpty();
    }

    @Test
    void correoSinDominioEsInvalido() {
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, "ana.perez@ejemplo", CONTRASENA)))
                .containsOnly("correo");
    }

    @Test
    void correoDeMasDeCientoCincuentaCaracteresEsInvalido() {
        String correoLargo = "a".repeat(139) + "@ejemplo.com";
        assertThat(correoLargo).hasSize(151);

        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, correoLargo, CONTRASENA)))
                .containsOnly("correo");
    }

    @Test
    void contrasenaDeSieteCaracteresEsInvalidaYDeOchoEsValida() {
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, "1234567")))
                .containsOnly("contrasena");
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, "12345678")))
                .isEmpty();
    }

    @Test
    void contrasenaDeSoloEspaciosEsInvalida() {
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, " ".repeat(10))))
                .containsOnly("contrasena");
    }

    @Test
    void contrasenaDeSetentaYDosBytesEsValida() {
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, "a".repeat(72)))).isEmpty();
        // 36 letras "ñ" son 72 bytes en UTF-8: justo en el límite.
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, "ñ".repeat(36)))).isEmpty();
    }

    @Test
    void contrasenaDeCuarentaLetrasEnieSuperaLosSetentaYDosBytes() {
        String contrasena = "ñ".repeat(40);
        // Son solo 40 caracteres, pero 80 bytes: @Size no lo detectaría.
        assertThat(contrasena).hasSize(40);
        assertThat(contrasena.getBytes(StandardCharsets.UTF_8)).hasSize(80);

        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, contrasena)))
                .containsOnly("contrasena");
    }

    @Test
    void contrasenaDeSetentaYTresCaracteresAsciiEsInvalida() {
        assertThat(camposInvalidos(new RegistroRequest(NOMBRE, CORREO, "a".repeat(73))))
                .containsOnly("contrasena");
    }

    @Test
    void elCorreoSeRecortaYSeConvierteAMinusculasYElNombreSeRecorta() {
        RegistroRequest solicitud = new RegistroRequest("  Ana Pérez  ", "  Ana.Perez@Ejemplo.COM ", CONTRASENA);

        assertThat(solicitud.nombre()).isEqualTo("Ana Pérez");
        assertThat(solicitud.correo()).isEqualTo("ana.perez@ejemplo.com");
    }

    @Test
    void laContrasenaNoSeModifica() {
        String contrasenaConEspacios = "  Clave Con Espacios  ";

        assertThat(new RegistroRequest(NOMBRE, CORREO, contrasenaConEspacios).contrasena())
                .isEqualTo(contrasenaConEspacios);
    }

    private Set<String> camposInvalidos(RegistroRequest solicitud) {
        return VALIDADOR.validate(solicitud).stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
