package com.superinter.producto.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.superinter.categoria.domain.Categoria;
import lombok.Builder;
import lombok.Getter;

/**
 * Producto del inventario del supermercado.
 * Inmutable: cualquier cambio produce una nueva instancia mediante toBuilder().
 */
@Getter
@Builder(toBuilder = true)
public class Producto {

    private final Integer idProducto;
    private final String codigo;
    private final String nombre;
    private final BigDecimal precio;
    private final Integer stock;
    private final LocalDate fechaVencimiento;
    private final Categoria categoria;
}