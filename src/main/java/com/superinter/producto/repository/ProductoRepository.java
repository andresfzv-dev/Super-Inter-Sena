package com.superinter.producto.repository;

import java.util.List;
import java.util.Optional;

import com.superinter.producto.domain.Producto;

/**
 * Operaciones de acceso a datos de los productos (CRUD).
 */
public interface ProductoRepository {

    Producto guardar(Producto producto);

    List<Producto> listarTodos();

    Optional<Producto> buscarPorId(int idProducto);

    boolean actualizar(Producto producto);

    boolean eliminarPorId(int idProducto);

    boolean existePorCodigo(String codigo);

    boolean existePorCodigoEnOtroProducto(String codigo, int idProductoExcluido);

    List<Producto> buscar(String nombre, Integer idCategoria);
}
