package com.superinter.producto.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ProductoRequestTest {

    private static final Validator VALIDADOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static final String CODIGO_VALIDO = "7701234567890";
    private static final BigDecimal PRECIO_VALIDO = new BigDecimal("4500.00");
    private static final int STOCK_VALIDO = 20;

    @Test
    void solicitudValidaNoTieneErrores() {
        ProductoRequest solicitud = crearSolicitud(CODIGO_VALIDO, PRECIO_VALIDO, STOCK_VALIDO);

        assertThat(VALIDADOR.validate(solicitud)).isEmpty();
    }

    @Test
    void codigoEnBlancoEsInvalido() {
        ProductoRequest solicitud = crearSolicitud("   ", PRECIO_VALIDO, STOCK_VALIDO);

        assertThat(camposInvalidos(solicitud)).containsOnly("codigo");
    }

    @Test
    void precioCeroEsInvalido() {
        ProductoRequest solicitud = crearSolicitud(CODIGO_VALIDO, BigDecimal.ZERO, STOCK_VALIDO);

        assertThat(camposInvalidos(solicitud)).containsOnly("precio");
    }

    @Test
    void precioConMasDeDosDecimalesEsInvalido() {
        ProductoRequest solicitud = crearSolicitud(CODIGO_VALIDO, new BigDecimal("4500.555"), STOCK_VALIDO);

        assertThat(camposInvalidos(solicitud)).containsOnly("precio");
    }

    @Test
    void stockNegativoEsInvalido() {
        ProductoRequest solicitud = crearSolicitud(CODIGO_VALIDO, PRECIO_VALIDO, -1);

        assertThat(camposInvalidos(solicitud)).containsOnly("stock");
    }

    private ProductoRequest crearSolicitud(String codigo, BigDecimal precio, Integer stock) {
        return new ProductoRequest(codigo, "Arroz Diana 1kg", precio, stock, null, 1);
    }

    private Set<String> camposInvalidos(ProductoRequest solicitud) {
        return VALIDADOR.validate(solicitud).stream()
                .map(violacion -> violacion.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void losTextosSeRecortanAlCrearLaSolicitud() {
        ProductoRequest solicitud = new ProductoRequest(
                "  7701234567890 ", " Arroz Diana 1kg  ", PRECIO_VALIDO, STOCK_VALIDO, null, 1);

        assertThat(solicitud.codigo()).isEqualTo("7701234567890");
        assertThat(solicitud.nombre()).isEqualTo("Arroz Diana 1kg");
    }
}