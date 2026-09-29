package com.superinter.categoria.domain;

import lombok.Builder;
import lombok.Getter;

/**
 * Clasificación de los productos del supermercado.
 * Inmutable: sus valores se asignan al construirla y no cambian.
 */
@Getter
@Builder
public class Categoria {

    private final Integer idCategoria;
    private final String nombre;
}