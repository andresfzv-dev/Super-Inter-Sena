package com.superinter.producto.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.sql.DataSource;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.repository.CategoriaRepository;
import com.superinter.common.exception.ConflictoException;
import com.superinter.producto.domain.Producto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del CRUD contra la base de datos real.
 * Los productos de prueba usan el prefijo TEST- y se eliminan antes y después de cada prueba.
 */
@SpringBootTest
class JdbcProductoRepositoryTest {

    private static final String PREFIJO_CODIGO = "TEST-";
    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "DELETE FROM productos WHERE codigo LIKE 'TEST-%'";
    private static final int ID_INEXISTENTE = 999_999;

    private final ProductoRepository productoRepository;
    private final DataSource dataSource;
    private final Categoria categoria;

    @Autowired
    JdbcProductoRepositoryTest(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               DataSource dataSource) {
        this.productoRepository = productoRepository;
        this.dataSource = dataSource;
        this.categoria = categoriaRepository.listarTodas().getFirst();
    }

    @BeforeEach
    void limpiarAntes() throws SQLException {
        eliminarDatosDePrueba();
    }

    @AfterEach
    void limpiarDespues() throws SQLException {
        eliminarDatosDePrueba();
    }

    @Test
    void guardarAsignaElIdGeneradoYElProductoPuedeConsultarse() {
        Producto guardado = productoRepository.guardar(crearProducto("001"));

        assertThat(guardado.getIdProducto()).isNotNull();

        Producto consultado = productoRepository.buscarPorId(guardado.getIdProducto()).orElseThrow();
        assertThat(consultado.getCodigo()).isEqualTo(PREFIJO_CODIGO + "001");
        assertThat(consultado.getPrecio()).isEqualByComparingTo("4500.00");
        assertThat(consultado.getStock()).isEqualTo(20);
        assertThat(consultado.getFechaVencimiento()).isEqualTo(LocalDate.of(2027, 3, 15));
        assertThat(consultado.getCategoria().getNombre()).isEqualTo(categoria.getNombre());
    }

    @Test
    void guardarConCodigoDuplicadoLanzaConflicto() {
        productoRepository.guardar(crearProducto("002"));

        assertThatThrownBy(() -> productoRepository.guardar(crearProducto("002")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un producto con el código TEST-002");
    }

    @Test
    void listarTodosIncluyeLosProductosGuardados() {
        productoRepository.guardar(crearProducto("003"));

        assertThat(productoRepository.listarTodos())
                .extracting(Producto::getCodigo)
                .contains(PREFIJO_CODIGO + "003");
    }

    @Test
    void actualizarModificaLosDatosDelProducto() {
        Producto guardado = productoRepository.guardar(crearProducto("004"));
        Producto modificado = guardado.toBuilder()
                .nombre("Arroz Diana 5kg")
                .precio(new BigDecimal("21000.00"))
                .stock(5)
                .fechaVencimiento(null)
                .build();

        boolean actualizado = productoRepository.actualizar(modificado);

        assertThat(actualizado).isTrue();
        Producto consultado = productoRepository.buscarPorId(guardado.getIdProducto()).orElseThrow();
        assertThat(consultado.getNombre()).isEqualTo("Arroz Diana 5kg");
        assertThat(consultado.getPrecio()).isEqualByComparingTo("21000.00");
        assertThat(consultado.getStock()).isEqualTo(5);
        assertThat(consultado.getFechaVencimiento()).isNull();
    }

    @Test
    void actualizarDevuelveFalsoCuandoElProductoNoExiste() {
        Producto inexistente = crearProducto("005").toBuilder().idProducto(ID_INEXISTENTE).build();

        assertThat(productoRepository.actualizar(inexistente)).isFalse();
    }

    @Test
    void eliminarPorIdBorraElProducto() {
        Producto guardado = productoRepository.guardar(crearProducto("006"));

        assertThat(productoRepository.eliminarPorId(guardado.getIdProducto())).isTrue();
        assertThat(productoRepository.buscarPorId(guardado.getIdProducto())).isEmpty();
    }

    @Test
    void eliminarPorIdDevuelveFalsoCuandoElProductoNoExiste() {
        assertThat(productoRepository.eliminarPorId(ID_INEXISTENTE)).isFalse();
    }

    @Test
    void existePorCodigoDistingueElPropioProductoDeOtros() {
        Producto guardado = productoRepository.guardar(crearProducto("007"));

        assertThat(productoRepository.existePorCodigo(PREFIJO_CODIGO + "007")).isTrue();
        assertThat(productoRepository.existePorCodigo(PREFIJO_CODIGO + "999")).isFalse();
        assertThat(productoRepository.existePorCodigoEnOtroProducto(
                PREFIJO_CODIGO + "007", guardado.getIdProducto())).isFalse();
    }

    private Producto crearProducto(String sufijoCodigo) {
        return Producto.builder()
                .codigo(PREFIJO_CODIGO + sufijoCodigo)
                .nombre("Arroz Diana 1kg")
                .precio(new BigDecimal("4500.00"))
                .stock(20)
                .fechaVencimiento(LocalDate.of(2027, 3, 15))
                .categoria(categoria)
                .build();
    }

    private void eliminarDatosDePrueba() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LIMPIAR_DATOS_DE_PRUEBA)) {
            statement.executeUpdate();
        }
    }
}