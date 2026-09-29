package com.superinter.categoria.repository;

import java.util.List;
import java.util.Optional;

import com.superinter.categoria.domain.Categoria;

/**
 * Implementación en memoria para pruebas unitarias, sin base de datos.
 */
public class CategoriaRepositoryEnMemoria implements CategoriaRepository {

    private final List<Categoria> categorias;

    public CategoriaRepositoryEnMemoria(List<Categoria> categorias) {
        this.categorias = List.copyOf(categorias);
    }

    @Override
    public List<Categoria> listarTodas() {
        return categorias;
    }

    @Override
    public Optional<Categoria> buscarPorId(int idCategoria) {
        return categorias.stream()
                .filter(categoria -> categoria.getIdCategoria() == idCategoria)
                .findFirst();
    }
}