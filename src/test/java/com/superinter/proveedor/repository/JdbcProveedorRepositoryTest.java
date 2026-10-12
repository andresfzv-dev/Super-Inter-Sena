package com.superinter.proveedor.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.superinter.common.exception.ConflictoException;
import com.superinter.proveedor.domain.Proveedor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integración del CRUD de proveedores contra la base de datos real.
 * Los proveedores de prueba usan el prefijo TEST- y se eliminan antes y después de cada prueba.
 */
@SpringBootTest
class JdbcProveedorRepositoryTest {

    private static final String PREFIJO = "TEST-";
    private static final String SQL_LIMPIAR_DATOS_DE_PRUEBA = "DELETE FROM proveedores WHERE nombre LIKE 'TEST-%'";
    private static final int ID_INEXISTENTE = 999_999;

    private final ProveedorRepository proveedorRepository;
    private final DataSource dataSource;

    @Autowired
    JdbcProveedorRepositoryTest(ProveedorRepository proveedorRepository, DataSource dataSource) {
        this.proveedorRepository = proveedorRepository;
        this.dataSource = dataSource;
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
    void guardarAsignaElIdGeneradoYConservaTodosLosCampos() {
        Proveedor guardado = proveedorRepository.guardar(crearProveedor("Postobón"));

        assertThat(guardado.getIdProveedor()).isNotNull();

        Proveedor consultado = proveedorRepository.buscarPorId(guardado.getIdProveedor()).orElseThrow();
        assertThat(consultado.getNombre()).isEqualTo(PREFIJO + "Postobón");
        assertThat(consultado.getContacto()).isEqualTo("Laura Gómez");
        assertThat(consultado.getTelefono()).isEqualTo("+57 300 123 4567");
        assertThat(consultado.getCorreo()).isEqualTo("ventas@ejemplo.com");
    }

    @Test
    void guardarSinDatosDeContactoLosAlmacenaComoNulos() {
        Proveedor soloNombre = Proveedor.builder().nombre(PREFIJO + "Sin contacto").build();

        Proveedor guardado = proveedorRepository.guardar(soloNombre);

        Proveedor consultado = proveedorRepository.buscarPorId(guardado.getIdProveedor()).orElseThrow();
        assertThat(consultado.getContacto()).isNull();
        assertThat(consultado.getTelefono()).isNull();
        assertThat(consultado.getCorreo()).isNull();
    }

    @Test
    void guardarConNombreDuplicadoSinDistinguirMayusculasLanzaConflicto() {
        proveedorRepository.guardar(crearProveedor("Postobón"));

        assertThatThrownBy(() -> proveedorRepository.guardar(crearProveedor("POSTOBÓN")))
                .isInstanceOf(ConflictoException.class)
                .hasMessage("Ya existe un proveedor con el nombre TEST-POSTOBÓN");
    }

    @Test
    void buscarPorNombreEsParcialYNoDistingueMayusculas() {
        proveedorRepository.guardar(crearProveedor("Alpina Productos"));
        proveedorRepository.guardar(crearProveedor("Colanta"));

        assertThat(proveedorRepository.buscar("alpina"))
                .extracting(Proveedor::getNombre)
                .containsExactly(PREFIJO + "Alpina Productos");
    }

    @Test
    void buscarSinFiltroDevuelveTodosOrdenadosPorNombre() {
        proveedorRepository.guardar(crearProveedor("Zenú"));
        proveedorRepository.guardar(crearProveedor("Alpina"));

        assertThat(proveedorRepository.buscar(null))
                .extracting(Proveedor::getNombre)
                .containsSubsequence(PREFIJO + "Alpina", PREFIJO + "Zenú");
    }

    @Test
    void actualizarModificaLosDatosYPermiteQuitarElCorreo() {
        Proveedor guardado = proveedorRepository.guardar(crearProveedor("Postobón"));
        Proveedor modificado = guardado.toBuilder()
                .contacto("Carlos Ruiz")
                .correo(null)
                .build();

        assertThat(proveedorRepository.actualizar(modificado)).isTrue();

        Proveedor consultado = proveedorRepository.buscarPorId(guardado.getIdProveedor()).orElseThrow();
        assertThat(consultado.getContacto()).isEqualTo("Carlos Ruiz");
        assertThat(consultado.getCorreo()).isNull();
    }

    @Test
    void actualizarDevuelveFalsoCuandoElProveedorNoExiste() {
        Proveedor inexistente = crearProveedor("Inexistente").toBuilder().idProveedor(ID_INEXISTENTE).build();

        assertThat(proveedorRepository.actualizar(inexistente)).isFalse();
    }

    @Test
    void eliminarPorIdBorraElProveedor() {
        Proveedor guardado = proveedorRepository.guardar(crearProveedor("Postobón"));

        assertThat(proveedorRepository.eliminarPorId(guardado.getIdProveedor())).isTrue();
        assertThat(proveedorRepository.buscarPorId(guardado.getIdProveedor())).isEmpty();
    }

    @Test
    void eliminarPorIdDevuelveFalsoCuandoElProveedorNoExiste() {
        assertThat(proveedorRepository.eliminarPorId(ID_INEXISTENTE)).isFalse();
    }

    @Test
    void existePorNombreNoDistingueMayusculasYExcluyeAlPropioProveedor() {
        Proveedor guardado = proveedorRepository.guardar(crearProveedor("Postobón"));

        assertThat(proveedorRepository.existePorNombre(PREFIJO + "postobón")).isTrue();
        assertThat(proveedorRepository.existePorNombre(PREFIJO + "Otro")).isFalse();
        assertThat(proveedorRepository.existePorNombreEnOtroProveedor(
                PREFIJO + "Postobón", guardado.getIdProveedor())).isFalse();
    }

    private Proveedor crearProveedor(String nombre) {
        return Proveedor.builder()
                .nombre(PREFIJO + nombre)
                .contacto("Laura Gómez")
                .telefono("+57 300 123 4567")
                .correo("ventas@ejemplo.com")
                .build();
    }

    private void eliminarDatosDePrueba() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LIMPIAR_DATOS_DE_PRUEBA)) {
            statement.executeUpdate();
        }
    }
}