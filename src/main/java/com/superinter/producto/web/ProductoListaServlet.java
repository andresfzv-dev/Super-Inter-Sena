package com.superinter.producto.web;

import java.io.IOException;
import java.util.Map;

import com.superinter.categoria.service.CategoriaService;
import com.superinter.common.web.AtributosVista;
import com.superinter.common.web.ParametrosWeb;
import com.superinter.producto.service.ProductoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Muestra el listado de productos con búsqueda por nombre y filtro por categoría (GET).
 */
public class ProductoListaServlet extends HttpServlet {

    static final String VISTA = "/WEB-INF/jsp/productos/lista.jsp";

    private static final Map<String, String> MENSAJES_EXITO = Map.of(
            "creado", "Producto registrado correctamente.",
            "actualizado", "Producto actualizado correctamente.",
            "eliminado", "Producto eliminado correctamente.");

    private final transient ProductoService productoService;
    private final transient CategoriaService categoriaService;

    public ProductoListaServlet(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("nombre");
        Integer idCategoria = ParametrosWeb.enteroOpcional(request.getParameter("idCategoria"));

        request.setAttribute(AtributosVista.TITULO_PAGINA, "Productos");
        request.setAttribute(AtributosVista.PAGINA_ACTIVA, "productos");
        request.setAttribute("productos", productoService.buscar(nombre, idCategoria));
        request.setAttribute("categorias", categoriaService.listarTodas());
        request.setAttribute("nombreBuscado", nombre);
        request.setAttribute("idCategoriaSeleccionada", idCategoria);
        request.setAttribute("mensajeExito", obtenerMensajeExito(request.getParameter("mensaje")));

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private String obtenerMensajeExito(String codigoMensaje) {
        return codigoMensaje == null ? null : MENSAJES_EXITO.get(codigoMensaje);
    }
}