package com.superinter.producto.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.superinter.categoria.domain.Categoria;
import com.superinter.producto.domain.Producto;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar o actualizar un producto.
 * Las validaciones reflejan las restricciones de la tabla productos.
 */
public record ProductoRequest(

        @NotBlank(message = "El código es obligatorio")
        @Size(max = 50, message = "El código no puede superar 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser mayor que cero")
        @Digits(integer = 10, fraction = 2, message = "El precio admite máximo 10 dígitos enteros y 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        LocalDate fechaVencimiento,

        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "La categoría debe ser un identificador válido")
        Integer idCategoria) {

    public Producto aProducto(Categoria categoria) {
        return Producto.builder()
                .codigo(codigo.strip())
                .nombre(nombre.strip())
                .precio(precio)
                .stock(stock)
                .fechaVencimiento(fechaVencimiento)
                .categoria(categoria)
                .build();
    }
}