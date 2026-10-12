package com.superinter.proveedor.dto;

import com.superinter.proveedor.domain.Proveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar o actualizar un proveedor.
 * Las longitudes máximas coinciden con las columnas de la tabla proveedores.
 */
public record ProveedorRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 100, message = "El contacto no puede superar 100 caracteres")
        String contacto,

        @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$",
                message = "El teléfono solo admite números, espacios, +, - y paréntesis (mínimo 7 caracteres)")
        String telefono,

        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        @Email(regexp = ".+@.+\\..+", message = "El correo no tiene un formato válido")
        String correo) {

    /**
     * Constructor compacto: normaliza los textos antes de validarlos.
     * El nombre solo se recorta, para que un nombre en blanco llegue como "" y lo
     * rechace {@code @NotBlank}. Los campos opcionales vacíos se convierten en
     * {@code null}, porque en la base de datos "sin dato" se representa con NULL.
     */
    public ProveedorRequest {
        nombre = nombre == null ? null : nombre.strip();
        contacto = textoOpcional(contacto);
        telefono = textoOpcional(telefono);
        correo = textoOpcional(correo);
    }

    public Proveedor aProveedor() {
        return Proveedor.builder()
                .nombre(nombre)
                .contacto(contacto)
                .telefono(telefono)
                .correo(correo)
                .build();
    }

    private static String textoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.strip();
    }
}