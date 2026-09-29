package com.superinter.categoria.repository;

import java.util.List;
import java.util.Optional;

import com.superinter.categoria.domain.Categoria;

/**
 * Operaciones de acceso a datos de las categorías.
 * Las categorías son de solo lectura para la aplicación.
 */
public interface CategoriaRepository {

    List<Categoria> listarTodas();

    Optional<Categoria> buscarPorId(int idCategoria);
}