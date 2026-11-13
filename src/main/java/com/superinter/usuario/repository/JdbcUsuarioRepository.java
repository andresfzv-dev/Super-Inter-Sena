package com.superinter.usuario.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import com.superinter.common.exception.AccesoDatosException;
import com.superinter.common.exception.ConflictoException;
import com.superinter.usuario.domain.Rol;
import com.superinter.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Implementación de {@link UsuarioRepository} con JDBC puro.
 */
@Repository
@RequiredArgsConstructor
public class JdbcUsuarioRepository implements UsuarioRepository {

    /** Código SQLSTATE estándar de violación de unicidad. */
    private static final String VIOLACION_UNICIDAD = "23505";

    private static final String[] COLUMNAS_GENERADAS = {"id_usuario"};

    private static final String SQL_INSERTAR = """
            INSERT INTO usuarios (nombre, correo, contrasena, rol)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SQL_BUSCAR_POR_CORREO = """
            SELECT id_usuario, nombre, correo, contrasena, rol
            FROM usuarios
            WHERE correo = ?
            """;

    // Comparación exacta: chk_usuarios_correo_minusculas garantiza que en la tabla solo hay
    // correos en minúsculas, así que no hace falta lower() y se aprovecha el índice de la
    // restricción única usuarios_correo_key.
    private static final String SQL_EXISTE_CORREO =
            "SELECT EXISTS (SELECT 1 FROM usuarios WHERE correo = ?)";

    private final DataSource dataSource;

    @Override
    public Usuario guardar(Usuario usuario) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERTAR, COLUMNAS_GENERADAS)) {

            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getCorreo());
            statement.setString(3, usuario.getContrasenaCifrada());
            statement.setString(4, usuario.getRol().name());
            statement.executeUpdate();

            try (ResultSet llavesGeneradas = statement.getGeneratedKeys()) {
                llavesGeneradas.next();
                return usuario.toBuilder()
                        .idUsuario(llavesGeneradas.getInt(1))
                        .build();
            }

        } catch (SQLException excepcion) {
            // Cubre el caso en que dos registros simultáneos con el mismo correo superan la
            // verificación previa del servicio: la restricción usuarios_correo_key rechaza el
            // segundo y se responde con el mismo mensaje de conflicto.
            if (VIOLACION_UNICIDAD.equals(excepcion.getSQLState())) {
                throw new ConflictoException(
                        "Ya existe un usuario registrado con el correo " + usuario.getCorreo());
            }
            throw new AccesoDatosException("Error al guardar el usuario", excepcion);
        }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_CORREO)) {

            statement.setString(1, correo);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearUsuario(resultSet))
                        : Optional.empty();
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al buscar el usuario por correo", excepcion);
        }
    }

    @Override
    public boolean existePorCorreo(String correo) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTE_CORREO)) {

            statement.setString(1, correo);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al verificar el correo del usuario", excepcion);
        }
    }

    private Usuario mapearUsuario(ResultSet resultSet) throws SQLException {
        return Usuario.builder()
                .idUsuario(resultSet.getInt("id_usuario"))
                .nombre(resultSet.getString("nombre"))
                .correo(resultSet.getString("correo"))
                .contrasenaCifrada(resultSet.getString("contrasena"))
                .rol(Rol.desdeCodigo(resultSet.getString("rol")))
                .build();
    }
}
