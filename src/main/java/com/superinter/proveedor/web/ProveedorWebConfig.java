package com.superinter.proveedor.web;

import com.superinter.proveedor.service.ProveedorService;
import jakarta.validation.Validator;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra en Tomcat los servlets del módulo web de proveedores.
 *
 * <p>Se usa {@link ServletRegistrationBean} en lugar de {@code @WebServlet} porque así
 * Spring crea los servlets y les inyecta el servicio por constructor.
 */
@Configuration
public class ProveedorWebConfig {

    @Bean
    public ServletRegistrationBean<ProveedorListaServlet> proveedorListaServlet(ProveedorService proveedorService) {
        return new ServletRegistrationBean<>(
                new ProveedorListaServlet(proveedorService), ProveedorListaServlet.RUTA);
    }

    @Bean
    public ServletRegistrationBean<ProveedorFormularioServlet> proveedorFormularioServlet(
            ProveedorService proveedorService, Validator validador) {
        return new ServletRegistrationBean<>(
                new ProveedorFormularioServlet(proveedorService, validador),
                ProveedorFormularioServlet.RUTA_NUEVO,
                ProveedorFormularioServlet.RUTA_EDITAR);
    }

    @Bean
    public ServletRegistrationBean<ProveedorEliminarServlet> proveedorEliminarServlet(
            ProveedorService proveedorService) {
        return new ServletRegistrationBean<>(
                new ProveedorEliminarServlet(proveedorService), ProveedorEliminarServlet.RUTA);
    }
}