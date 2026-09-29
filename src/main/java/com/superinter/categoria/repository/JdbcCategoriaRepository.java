package com.superinter.categoria.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.superinter.categoria.domain.Categoria;
import com.superinter.common.exception.AccesoDatosException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Implementación de {@link CategoriaRepository} con JDBC puro.
 */
@Repository
@RequiredArgsConstructor
public class JdbcCategoriaRepository implements CategoriaRepository {

    private static final String SQL_LISTAR_TODAS =
            "SELECT id_categoria, nombre FROM categorias ORDER BY nombre";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_categoria, nombre FROM categorias WHERE id_categoria = ?";

    private final DataSource dataSource;

    @Override
    public List<Categoria> listarTodas() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_LISTAR_TODAS);
             ResultSet resultSet = statement.executeQuery()) {

            List<Categoria> categorias = new ArrayList<>();
            while (resultSet.next()) {
                categorias.add(mapearCategoria(resultSet));
            }
            return categorias;

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al listar las categorías", excepcion);
        }
    }

    @Override
    public Optional<Categoria> buscarPorId(int idCategoria) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_BUSCAR_POR_ID)) {

            statement.setInt(1, idCategoria);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapearCategoria(resultSet))
                        : Optional.empty();
            }

        } catch (SQLException excepcion) {
            throw new AccesoDatosException("Error al buscar la categoría con id " + idCategoria, excepcion);
        }
    }

    private Categoria mapearCategoria(ResultSet resultSet) throws SQLException {
        return Categoria.builder()
                .idCategoria(resultSet.getInt("id_categoria"))
                .nombre(resultSet.getString("nombre"))
                .build();
    }
}