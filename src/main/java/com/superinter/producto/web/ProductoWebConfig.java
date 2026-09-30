package com.superinter.producto.web;

import com.superinter.categoria.service.CategoriaService;
import com.superinter.producto.service.ProductoService;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra en Tomcat los servlets del módulo web de productos,
 * inyectándoles los servicios administrados por Spring.
 */
@Configuration
public class ProductoWebConfig {

    @Bean
    public ServletRegistrationBean<ProductoListaServlet> productoListaServlet(
            ProductoService productoService, CategoriaService categoriaService) {
        return new ServletRegistrationBean<>(
                new ProductoListaServlet(productoService, categoriaService), "/productos");
    }
}