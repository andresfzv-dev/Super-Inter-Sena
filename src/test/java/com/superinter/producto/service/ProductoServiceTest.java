package com.superinter.producto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepositoryEnMemoria;
import com.superinter.categoria.service.CategoriaService;
import com.superinter.common.exception.ConflictoException;
import com.superinter.common.exception.RecursoNoEncontradoException;
import com.superinter.producto.dto.ProductoRequest;
import com.superinter.producto.dto.ProductoResponse;
import com.superinter.producto.repository.ProductoRepositoryEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductoServiceTest {

    private static final int ID_ABARROTES = 1;
    private static final int ID_CATEGORIA_INEXISTENTE = 99;
    private static final int ID_PRODUCTO_INEXISTENTE = 999;

    private ProductoService productoService;

    @BeforeEach
    void configurar() {
        Categoria abarrotes = Categoria.builder().idCategoria(ID_ABARROTES).nombre("Abarrotes").build();
        CategoriaService categoriaService =
                new CategoriaService(new CategoriaRepositoryEnMemoria(List.of(abarrotes)));

        productoService = new ProductoService(new ProductoRepositoryEnMemoria(), categoriaService);
    }

    @Test
    void registrarAsignaIdEIncluyeLaCategoria() {
        ProductoResponse registrado = productoService.registrar(crearSolicitud("770-001", ID_ABARROTES));

        assertThat(registrado.idProducto()).isNotNull();
        assertThat(registrado.categoria().nombre()).isEqualTo("Abarrotes");
    }

    @Test
    void registrarConCodigoExistenteLanzaConflicto() {
        productoService.registrar(crearSolicitud("770-001", ID_ABARROTES));

        assertThatThrownBy(() -> productoService.registrar(crearSolicitud("770-001", ID_ABARROTES)))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un producto con el código 770-001");
    }

    @Test
    void registrarConCategoriaInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> productoService.registrar(crearSolicitud("770-001", ID_CATEGORIA_INEXISTENTE)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Categoría con id 99 no existe");
    }

    @Test
    void obtenerPorIdInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> productoService.obtenerPorId(ID_PRODUCTO_INEXISTENTE))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("Producto con id 999 no existe");
    }

    @Test
    void actualizarModificaElProductoYPermiteConservarSuCodigo() {
        ProductoResponse registrado = productoService.registrar(crearSolicitud("770-001", ID_ABARROTES));
        ProductoRequest cambios = new ProductoRequest(
                "770-001", "Arroz Diana 5kg", new BigDecimal("21000.00"), 5, null, ID_ABARROTES);

        ProductoResponse actualizado = productoService.actualizar(registrado.idProducto(), cambios);

        assertThat(actualizado.nombre()).isEqualTo("Arroz Diana 5kg");
        assertThat(productoService.obtenerPorId(registrado.idProducto()).stock()).isEqualTo(5);
    }

    @Test
    void actualizarConCodigoDeOtroProductoLanzaConflicto() {
        productoService.registrar(crearSolicitud("770-001", ID_ABARROTES));
        ProductoResponse segundo = productoService.registrar(crearSolicitud("770-002", ID_ABARROTES));

        assertThatThrownBy(() -> productoService.actualizar(
                segundo.idProducto(), crearSolicitud("770-001", ID_ABARROTES)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizarProductoInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> productoService.actualizar(
                ID_PRODUCTO_INEXISTENTE, crearSolicitud("770-001", ID_ABARROTES)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarBorraElProducto() {
        ProductoResponse registrado = productoService.registrar(crearSolicitud("770-001", ID_ABARROTES));

        productoService.eliminar(registrado.idProducto());

        assertThat(productoService.listarTodos()).isEmpty();
    }

    @Test
    void eliminarProductoInexistenteLanzaRecursoNoEncontrado() {
        assertThatThrownBy(() -> productoService.eliminar(ID_PRODUCTO_INEXISTENTE))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private ProductoRequest crearSolicitud(String codigo, int idCategoria) {
        return new ProductoRequest(codigo, "Arroz Diana 1kg", new BigDecimal("4500.00"), 20, null, idCategoria);
    }
}