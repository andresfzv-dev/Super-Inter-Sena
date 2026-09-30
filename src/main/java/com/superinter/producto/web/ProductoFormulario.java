package com.superinter.producto.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;

import com.superinter.common.web.ParametrosWeb;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Valores del formulario web de productos tal como los escribe el usuario (texto).
 * Permite volver a mostrar el formulario con lo ingresado cuando hay errores.
 */
public record ProductoFormulario(
        Integer idProducto,
        String codigo,
        String nombre,
        String precio,
        String stock,
        String fechaVencimiento,
        String idCategoria) {

    public static ProductoFormulario vacio() {
        return new ProductoFormulario(null, "", "", "", "", "", "");
    }

    public static ProductoFormulario desde(ProductoResponse producto) {
        return new ProductoFormulario(
                producto.idProducto(),
                producto.codigo(),
                producto.nombre(),
                producto.precio().toPlainString(),
                String.valueOf(producto.stock()),
                producto.fechaVencimiento() == null ? "" : producto.fechaVencimiento().toString(),
                String.valueOf(producto.categoria().idCategoria()));
    }

    public static ProductoFormulario desde(HttpServletRequest request) {
        return new ProductoFormulario(
                ParametrosWeb.enteroOpcional(request.getParameter("idProducto")),
                request.getParameter("codigo"),
                request.getParameter("nombre"),
                request.getParameter("precio"),
                request.getParameter("stock"),
                request.getParameter("fechaVencimiento"),
                request.getParameter("idCategoria"));
    }

    /**
     * Convierte los textos del formulario a una solicitud con tipos.
     * Los campos con formato inválido se registran en {@code errores} y quedan en null.
     */
    public ProductoRequest aSolicitud(Map<String, String> errores) {
        BigDecimal precioConvertido =
                convertir(precio, BigDecimal::new, "precio", "El precio debe ser un número válido", errores);
        Integer stockConvertido =
                convertir(stock, Integer::valueOf, "stock", "El stock debe ser un número entero", errores);
        LocalDate fechaConvertida =
                convertir(fechaVencimiento, LocalDate::parse, "fechaVencimiento", "La fecha no es válida", errores);
        Integer categoriaConvertida =
                convertir(idCategoria, Integer::valueOf, "idCategoria", "Seleccione una categoría válida", errores);

        return new ProductoRequest(
                codigo, nombre, precioConvertido, stockConvertido, fechaConvertida, categoriaConvertida);
    }

    private static <T> T convertir(String valor, Function<String, T> conversor,
                                   String campo, String mensaje, Map<String, String> errores) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return conversor.apply(valor.strip());
        } catch (RuntimeException excepcion) {
            errores.put(campo, mensaje);
            return null;
        }
    }
}