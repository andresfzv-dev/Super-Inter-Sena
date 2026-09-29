package com.superinter.producto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.superinter.categoria.dto.CategoriaResponse;
import com.superinter.producto.domain.Producto;

/**
 * Representación de un producto en las respuestas de la API.
 */
public record ProductoResponse(
        Integer idProducto,
        String codigo,
        String nombre,
        BigDecimal precio,
        Integer stock,
        LocalDate fechaVencimiento,
        CategoriaResponse categoria) {

    public static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getIdProducto(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getFechaVencimiento(),
                CategoriaResponse.desde(producto.getCategoria()));
    }
}