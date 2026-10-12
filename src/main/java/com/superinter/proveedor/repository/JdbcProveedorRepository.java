package com.superinter.proveedor.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.superinter.common.exception.AccesoDatosException;
import com.superinter.common.exception.ConflictoException;
import com.superinter.proveedor.domain.Proveedor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Implementación de {@link ProveedorRepository} con JDBC puro.
 */
@Repository
@RequiredArgsConstructor
public class JdbcProveedorRepository implements ProveedorRepository {

    /** Códigos SQLSTATE estándar de violación de restricciones. */
    private static final String VIOLACION_UNICIDAD = "23505";
    private static final String VIOLACION_LLAVE_FORANEA = "23503";

    private static final String[] COLUMNAS_GENERADAS = {"id_proveedor"};

    private static final String SQL_SELECCION_BASE =
            "SELECT id_proveedor, nombre, contacto, telefono, correo FROM proveedores ";

    private static final String SQL_ORDEN = "ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_NOMBRE =
            SQL_SELECCION_BASE + "WHERE nombre ILIKE ? " + SQL_ORDEN;

    private static final String SQL_LISTAR_TODOS = SQL_SELECCION_BASE + SQL_ORDEN;

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECCION_BASE + "WHERE id_proveedor = ?";

    private static final String SQL_INSERTAR = """
            INSERT INTO proveedores (nombre, contacto, telefono, correo)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SQL_ACTUALIZAR = """
            UPDATE proveedores
            SET nombre = ?, contacto = ?, telefono = ?, correo = ?
            WHERE id_proveedor = ?
            """;

    private static final String SQL_ELIMINAR = "DELETE FROM proveedores WHERE id_proveedor = ?";

    // lower() en ambos lados reproduce la regla del índice único uq_proveedores_nombre.
    private static final String SQL_EXISTE_NOMBRE =
            "SELECT EXISTS (SELECT 1 FROM proveedores WHERE lower(nombre) = lower(?))";

    private static final String SQL_EXISTE_NOMBRE_EN_OTRO_PROVEEDOR =
            "SELECT EXISTS (SELECT 1 FROM proveedores WHERE lower(nombre) = lower(?) AND id_proveedor <> ?)";

    private final DataSource dataSource;

    @Override
    public Proveedor guardar(Proveedor proveedor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERTAR, COLUMNAS_GENERADAS)) {

            asignarParametrosDeEscritura(statement, proveedor);
            statement.executeUpdate();

            try (ResultSet llavesGeneradas = statement.getGeneratedKeys()) {
                llavesGeneradas.next();
                return proveedor.toBuilder()
                        .idProveedor(llavesGeneradas.getInt(1))
                        .build();
            }

        } catch (SQLException excepcion) {
            throw traducirErrorDeEscritura(excepcion, proveedor, "Error al guardar el proveedor");
        }
    }

    @Override
    public List<Proveedor> buscar(String nombre) {
        boolean sinFiltro = nombre == null || nombre.isBlank();
        String sql = sinFiltro ? SQL_LISTAR_TODOS : SQL_BUSCAR_POR_NOMBRE;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (!sinFiltro) {
                // Los % se agregan al valor, no al SQL: el texto del usuario
                // siempre viaja como parámetro y no puede alterar la consulta.
                statement.setString(1, "%" + nombre.strip() + "%");
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Proveedor> proveedores = new ArrayList<>();
                while (resultSet.next()) {
                    proveedores.add(mapearProveedor(resultSet));
                }
                return proveedores;
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al consultar los proveedores", excepcion);
        }
    }

    @Override
    public Optional<Proveedor> buscarPorId(int idProveedor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            statement.setInt(1, idProveedor);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearProveedor(resultSet))
                        : Optional.empty();
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al buscar el proveedor con id " + idProveedor, excepcion);
        }
    }

    @Override
    public boolean actualizar(Proveedor proveedor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR)) {

            int posicionId = asignarParametrosDeEscritura(statement, proveedor);
            statement.setInt(posicionId, proveedor.getIdProveedor());
            return statement.executeUpdate() > 0;

        } catch (SQLException excepcion) {
            throw traducirErrorDeEscritura(excepcion, proveedor, "Error al actualizar el proveedor");
        }
    }

    @Override
    public boolean eliminarPorId(int idProveedor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_ELIMINAR)) {

            statement.setInt(1, idProveedor);
            return statement.executeUpdate() > 0;

        } catch (SQLException excepcion) {
            // La llave foránea fk_compra_proveedor impide borrar un proveedor con compras.
            if (VIOLACION_LLAVE_FORANEA.equals(excepcion.getSQLState())) {
                throw new ConflictoException(
                        "No se puede eliminar el proveedor con id " + idProveedor
                                + " porque tiene compras registradas");
            }
            throw new AccesoDatosException("Error al eliminar el proveedor con id " + idProveedor, excepcion);
        }
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return consultarExistencia(SQL_EXISTE_NOMBRE, nombre);
    }

    @Override
    public boolean existePorNombreEnOtroProveedor(String nombre, int idProveedorExcluido) {
        return consultarExistencia(SQL_EXISTE_NOMBRE_EN_OTRO_PROVEEDOR, nombre, idProveedorExcluido);
    }

    /**
     * Asigna los parámetros comunes de INSERT y UPDATE, en el mismo orden de las columnas.
     *
     * @return la siguiente posición libre de parámetro
     */
    private int asignarParametrosDeEscritura(PreparedStatement statement, Proveedor proveedor) throws SQLException {
        int posicion = 1;
        statement.setString(posicion++, proveedor.getNombre());
        asignarTextoOpcional(statement, posicion++, proveedor.getContacto());
        asignarTextoOpcional(statement, posicion++, proveedor.getTelefono());
        asignarTextoOpcional(statement, posicion++, proveedor.getCorreo());
        return posicion;
    }

    /**
     * La especificación JDBC indica usar setNull para enviar NULL. Se declara el tipo
     * VARCHAR para que la base de datos reciba un nulo del tipo de la columna.
     */
    private void asignarTextoOpcional(PreparedStatement statement, int posicion, String valor) throws SQLException {
        if (valor == null) {
            statement.setNull(posicion, Types.VARCHAR);
        } else {
            statement.setString(posicion, valor);
        }
    }

    private boolean consultarExistencia(String sql, Object... parametros) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int indice = 0; indice < parametros.length; indice++) {
                statement.setObject(indice + 1, parametros[indice]);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al verificar el nombre del proveedor", excepcion);
        }
    }

    /**
     * Traduce la violación del índice único a un conflicto de negocio. Cubre el caso en que
     * dos usuarios registran el mismo nombre al mismo tiempo y ambos superan la validación
     * previa del servicio.
     */
    private RuntimeException traducirErrorDeEscritura(SQLException excepcion, Proveedor proveedor, String mensaje) {
        if (VIOLACION_UNICIDAD.equals(excepcion.getSQLState())) {
            return new ConflictoException("Ya existe un proveedor con el nombre " + proveedor.getNombre());
        }
        return new AccesoDatosException(mensaje, excepcion);
    }

    private Proveedor mapearProveedor(ResultSet resultSet) throws SQLException {
        return Proveedor.builder()
                .idProveedor(resultSet.getInt("id_proveedor"))
                .nombre(resultSet.getString("nombre"))
                .contacto(resultSet.getString("contacto"))
                .telefono(resultSet.getString("telefono"))
                .correo(resultSet.getString("correo"))
                .build();
    }
}