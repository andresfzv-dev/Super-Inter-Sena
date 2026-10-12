package com.superinter.proveedor.repository;

import java.util.List;
import java.util.Optional;

import com.superinter.proveedor.domain.Proveedor;

/**
 * Operaciones de acceso a datos de los proveedores (CRUD).
 */
public interface ProveedorRepository {

    /**
     * Inserta el proveedor y devuelve una nueva instancia con el identificador generado.
     */
    Proveedor guardar(Proveedor proveedor);

    /**
     * Busca proveedores cuyo nombre contenga el texto, sin distinguir mayúsculas.
     * Si el texto es nulo o vacío, devuelve todos. El resultado se ordena por nombre.
     */
    List<Proveedor> buscar(String nombre);

    Optional<Proveedor> buscarPorId(int idProveedor);

    /**
     * @return {@code false} si no existe un proveedor con ese identificador
     */
    boolean actualizar(Proveedor proveedor);

    /**
     * @return {@code false} si no existe un proveedor con ese identificador
     */
    boolean eliminarPorId(int idProveedor);

    /**
     * Indica si ya existe un proveedor con ese nombre, sin distinguir mayúsculas.
     */
    boolean existePorNombre(String nombre);

    /**
     * Igual que {@link #existePorNombre(String)}, pero ignora al proveedor indicado.
     * Permite que un proveedor conserve su propio nombre al ser actualizado.
     */
    boolean existePorNombreEnOtroProveedor(String nombre, int idProveedorExcluido);
}