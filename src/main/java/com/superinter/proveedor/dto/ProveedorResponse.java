package com.superinter.proveedor.dto;

import com.superinter.proveedor.domain.Proveedor;

/**
 * Representación de un proveedor hacia las vistas.
 * Está desacoplada de la entidad para que la futura API REST la reutilice sin cambios.
 */
public record ProveedorResponse(
        Integer idProveedor,
        String nombre,
        String contacto,
        String telefono,
        String correo) {

    public static ProveedorResponse desde(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getIdProveedor(),
                proveedor.getNombre(),
                proveedor.getContacto(),
                proveedor.getTelefono(),
                proveedor.getCorreo());
    }
}