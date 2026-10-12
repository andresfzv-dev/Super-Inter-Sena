package com.superinter.proveedor.repository;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.superinter.common.exception.ConflictoException;
import com.superinter.proveedor.domain.Proveedor;

/**
 * Implementación en memoria para pruebas unitarias, sin base de datos.
 * Reproduce las reglas de la tabla real: nombre único sin distinguir mayúsculas
 * y bloqueo de la eliminación cuando el proveedor tiene compras.
 */
public class ProveedorRepositoryEnMemoria implements ProveedorRepository {

    private final Map<Integer, Proveedor> proveedores = new LinkedHashMap<>();
    private final Set<Integer> proveedoresConCompras = new HashSet<>();
    private int siguienteId = 1;

    /**
     * Simula que el proveedor tiene compras, como lo haría la llave foránea fk_compra_proveedor.
     */
    public void registrarCompraDe(int idProveedor) {
        proveedoresConCompras.add(idProveedor);
    }

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        Proveedor guardado = proveedor.toBuilder().idProveedor(siguienteId++).build();
        proveedores.put(guardado.getIdProveedor(), guardado);
        return guardado;
    }

    @Override
    public List<Proveedor> buscar(String nombre) {
        return proveedores.values().stream()
                .filter(proveedor -> nombre == null || nombre.isBlank()
                        || proveedor.getNombre().toLowerCase().contains(nombre.strip().toLowerCase()))
                .toList();
    }

    @Override
    public Optional<Proveedor> buscarPorId(int idProveedor) {
        return Optional.ofNullable(proveedores.get(idProveedor));
    }

    @Override
    public boolean actualizar(Proveedor proveedor) {
        return proveedores.replace(proveedor.getIdProveedor(), proveedor) != null;
    }

    @Override
    public boolean eliminarPorId(int idProveedor) {
        if (proveedoresConCompras.contains(idProveedor)) {
            throw new ConflictoException("No se puede eliminar el proveedor con id " + idProveedor
                    + " porque tiene compras registradas");
        }
        return proveedores.remove(idProveedor) != null;
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return proveedores.values().stream()
                .anyMatch(proveedor -> proveedor.getNombre().equalsIgnoreCase(nombre));
    }

    @Override
    public boolean existePorNombreEnOtroProveedor(String nombre, int idProveedorExcluido) {
        return proveedores.values().stream()
                .anyMatch(proveedor -> proveedor.getNombre().equalsIgnoreCase(nombre)
                        && proveedor.getIdProveedor() != idProveedorExcluido);
    }
}