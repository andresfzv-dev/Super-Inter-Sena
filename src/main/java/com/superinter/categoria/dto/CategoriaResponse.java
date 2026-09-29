package com.superinter.categoria.dto;

import com.superinter.categoria.domain.Categoria;

/**
 * Representación de una categoría en las respuestas de la API.
 */
public record CategoriaResponse(Integer idCategoria, String nombre) {

    public static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.getIdCategoria(), categoria.getNombre());
    }
}