package com.superinter.producto.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.superinter.categoria.domain.Categoria;
import com.superinter.common.exception.AccesoDatosException;
import com.superinter.common.exception.ConflictoException;
import com.superinter.producto.domain.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Implementación de {@link ProductoRepository} con JDBC puro.
 */
@Repository
@RequiredArgsConstructor
public class JdbcProductoRepository implements ProductoRepository {

    /** Códigos SQLSTATE estándar de violación de restricciones. */
    private static final String VIOLACION_UNICIDAD = "23505";
    private static final String VIOLACION_LLAVE_FORANEA = "23503";

    private static final String[] COLUMNAS_GENERADAS = {"id_producto"};

    private static final String SQL_SELECCION_BASE = """
            SELECT p.id_producto, p.codigo, p.nombre, p.precio, p.stock, p.fecha_vencimiento,
                   c.id_categoria, c.nombre AS nombre_categoria
            FROM productos p
            JOIN categorias c ON c.id_categoria = p.id_categoria
            """;

    private static final String SQL_LISTAR_TODOS = SQL_SELECCION_BASE + "ORDER BY p.nombre";

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECCION_BASE + "WHERE p.id_producto = ?";

    private static final String SQL_INSERTAR = """
            INSERT INTO productos (codigo, nombre, precio, stock, fecha_vencimiento, id_categoria)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ACTUALIZAR = """
            UPDATE productos
            SET codigo = ?, nombre = ?, precio = ?, stock = ?, fecha_vencimiento = ?, id_categoria = ?
            WHERE id_producto = ?
            """;

    private static final String SQL_ELIMINAR = "DELETE FROM productos WHERE id_producto = ?";

    private static final String SQL_EXISTE_CODIGO =
            "SELECT EXISTS (SELECT 1 FROM productos WHERE codigo = ?)";

    private static final String SQL_EXISTE_CODIGO_EN_OTRO_PRODUCTO =
            "SELECT EXISTS (SELECT 1 FROM productos WHERE codigo = ? AND id_producto <> ?)";

    private final DataSource dataSource;

    @Override
    public Producto guardar(Producto producto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERTAR, COLUMNAS_GENERADAS)) {

            asignarParametrosDeEscritura(statement, producto);
            statement.executeUpdate();

            try (ResultSet llavesGeneradas = statement.getGeneratedKeys()) {
                llavesGeneradas.next();
                return producto.toBuilder()
                        .idProducto(llavesGeneradas.getInt(1))
                        .build();
            }

        } catch (SQLException excepcion) {
            throw traducirErrorDeEscritura(excepcion, producto, "Error al guardar el producto");
        }
    }

    @Override
    public List<Producto> listarTodos() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LISTAR_TODOS);
             ResultSet resultSet = statement.executeQuery()) {

            List<Producto> productos = new ArrayList<>();
            while (resultSet.next()) {
                productos.add(mapearProducto(resultSet));
            }
            return productos;

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al listar los productos", excepcion);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            statement.setInt(1, idProducto);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearProducto(resultSet))
                        : Optional.empty();
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al buscar el producto con id " + idProducto, excepcion);
        }
    }

    @Override
    public boolean actualizar(Producto producto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR)) {

            int posicionId = asignarParametrosDeEscritura(statement, producto);
            statement.setInt(posicionId, producto.getIdProducto());
            return statement.executeUpdate() > 0;

        } catch (SQLException excepcion) {
            throw traducirErrorDeEscritura(excepcion, producto, "Error al actualizar el producto");
        }
    }

    @Override
    public boolean eliminarPorId(int idProducto) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_ELIMINAR)) {

            statement.setInt(1, idProducto);
            return statement.executeUpdate() > 0;

        } catch (SQLException excepcion) {
            if (VIOLACION_LLAVE_FORANEA.equals(excepcion.getSQLState())) {
                throw new ConflictoException(
                        "No se puede eliminar el producto con id " + idProducto
                                + " porque tiene ventas o compras registradas");
            }
            throw new AccesoDatosException("Error al eliminar el producto con id " + idProducto, excepcion);
        }
    }

    @Override
    public boolean existePorCodigo(String codigo) {
        return consultarExistencia(SQL_EXISTE_CODIGO, codigo);
    }

    @Override
    public boolean existePorCodigoEnOtroProducto(String codigo, int idProductoExcluido) {
        return consultarExistencia(SQL_EXISTE_CODIGO_EN_OTRO_PRODUCTO, codigo, idProductoExcluido);
    }

    /**
     * Asigna los parámetros comunes de INSERT y UPDATE.
     *
     * @return la siguiente posición libre de parámetro
     */
    private int asignarParametrosDeEscritura(PreparedStatement statement, Producto producto) throws SQLException {
        int posicion = 1;
        statement.setString(posicion++, producto.getCodigo());
        statement.setString(posicion++, producto.getNombre());
        statement.setBigDecimal(posicion++, producto.getPrecio());
        statement.setInt(posicion++, producto.getStock());
        asignarFechaOpcional(statement, posicion++, producto.getFechaVencimiento());
        statement.setInt(posicion++, producto.getCategoria().getIdCategoria());
        return posicion;
    }

    private void asignarFechaOpcional(PreparedStatement statement, int posicion, LocalDate fecha) throws SQLException {
        if (fecha == null) {
            statement.setNull(posicion, Types.DATE);
        } else {
            statement.setObject(posicion, fecha);
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
            throw new AccesoDatosException("Error al verificar el código del producto", excepcion);
        }
    }

    private RuntimeException traducirErrorDeEscritura(SQLException excepcion, Producto producto, String mensaje) {
        String estadoSql = excepcion.getSQLState();

        if (VIOLACION_UNICIDAD.equals(estadoSql)) {
            return new ConflictoException("Ya existe un producto con el código " + producto.getCodigo());
        }
        if (VIOLACION_LLAVE_FORANEA.equals(estadoSql)) {
            return new ConflictoException(
                    "La categoría con id " + producto.getCategoria().getIdCategoria() + " no existe");
        }
        return new AccesoDatosException(mensaje, excepcion);
    }

    private Producto mapearProducto(ResultSet resultSet) throws SQLException {
        Categoria categoria = Categoria.builder()
                .idCategoria(resultSet.getInt("id_categoria"))
                .nombre(resultSet.getString("nombre_categoria"))
                .build();

        return Producto.builder()
                .idProducto(resultSet.getInt("id_producto"))
                .codigo(resultSet.getString("codigo"))
                .nombre(resultSet.getString("nombre"))
                .precio(resultSet.getBigDecimal("precio"))
                .stock(resultSet.getInt("stock"))
                .fechaVencimiento(resultSet.getObject("fecha_vencimiento", LocalDate.class))
                .categoria(categoria)
                .build();
    }
}