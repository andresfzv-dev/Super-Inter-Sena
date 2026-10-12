package com.superinter.proveedor.service;

import java.util.List;

import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.proveedor.domain.Proveedor;
import com.superinter.proveedor.dto.ProveedorRequest;
import com.superinter.proveedor.dto.ProveedorResponse;
import com.superinter.proveedor.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de la gestión de proveedores (RF-04) y sus reglas de negocio.
 *
 * <p>No depende de servlets ni de JSP: la interfaz web actual y la futura API REST
 * usan este mismo servicio.
 */
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private static final String RECURSO = "Proveedor";

    private final ProveedorRepository proveedorRepository;

    public ProveedorResponse registrar(ProveedorRequest solicitud) {
        if (proveedorRepository.existePorNombre(solicitud.nombre())) {
            throw nombreDuplicado(solicitud.nombre());
        }
        return ProveedorResponse.desde(proveedorRepository.guardar(solicitud.aProveedor()));
    }

    public List<ProveedorResponse> buscar(String nombre) {
        return proveedorRepository.buscar(nombre).stream()
                .map(ProveedorResponse::desde)
                .toList();
    }

    public ProveedorResponse obtenerPorId(int idProveedor) {
        return ProveedorResponse.desde(buscarExistente(idProveedor));
    }

    public ProveedorResponse actualizar(int idProveedor, ProveedorRequest solicitud) {
        // El 404 tiene prioridad: si el proveedor no existe, no tiene sentido revisar el nombre.
        buscarExistente(idProveedor);

        if (proveedorRepository.existePorNombreEnOtroProveedor(solicitud.nombre(), idProveedor)) {
            throw nombreDuplicado(solicitud.nombre());
        }

        Proveedor proveedor = solicitud.aProveedor().toBuilder()
                .idProveedor(idProveedor)
                .build();

        // Cubre el caso en que otro usuario lo eliminó entre la verificación y la actualización.
        if (!proveedorRepository.actualizar(proveedor)) {
            throw new RecursoNoEncontradoException(RECURSO, idProveedor);
        }
        return ProveedorResponse.desde(proveedor);
    }

    /**
     * Elimina el proveedor. Si tiene compras registradas, el repositorio lanza
     * {@link ConflictoException} y la eliminación no se realiza.
     */
    public void eliminar(int idProveedor) {
        if (!proveedorRepository.eliminarPorId(idProveedor)) {
            throw new RecursoNoEncontradoException(RECURSO, idProveedor);
        }
    }

    private Proveedor buscarExistente(int idProveedor) {
        return proveedorRepository.buscarPorId(idProveedor)
                .orElseThrow(() -> new RecursoNoEncontradoException(RECURSO, idProveedor));
    }

    private ConflictoException nombreDuplicado(String nombre) {
        return new ConflictoException("Ya existe un proveedor con el nombre " + nombre);
    }
}