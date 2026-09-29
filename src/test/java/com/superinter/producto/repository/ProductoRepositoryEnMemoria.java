package com.superinter.producto.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.superinter.producto.domain.Producto;

/**
 * Implementación en memoria para pruebas unitarias, sin base de datos.
 */
public class ProductoRepositoryEnMemoria implements ProductoRepository {

    private final Map<Integer, Producto> productos = new LinkedHashMap<>();
    private int siguienteId = 1;

    @Override
    public Producto guardar(Producto producto) {
        Producto guardado = producto.toBuilder().idProducto(siguienteId++).build();
        productos.put(guardado.getIdProducto(), guardado);
        return guardado;
    }

    @Override
    public List<Producto> listarTodos() {
        return List.copyOf(productos.values());
    }

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        return Optional.ofNullable(productos.get(idProducto));
    }

    @Override
    public boolean actualizar(Producto producto) {
        return productos.replace(producto.getIdProducto(), producto) != null;
    }

    @Override
    public boolean eliminarPorId(int idProducto) {
        return productos.remove(idProducto) != null;
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return productos.values().stream()
                .anyMatch(producto -> producto.getCodigo().equals(codigo));
    }

    @Override
    public boolean existePorCodigoEnOtroProducto(String codigo, int idProductoExcluido) {
        return productos.values().stream()
                .anyMatch(producto -> producto.getCodigo().equals(codigo)
                        && producto.getIdProducto() != idProductoExcluido);
    }
}