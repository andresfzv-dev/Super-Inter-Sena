package com.superinter.categoria.service;

import java.util.List;

import com.superinter.categoria.domain.Categoria;
import com.superinter.categoria.dto.CategoriaResponse;
import com.superinter.categoria.repository.CategoriaRepository;
import com.superinter.common.exception.RecursoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Casos de uso de las categorías. Las categorías son de solo lectura.
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private static final String RECURSO = "Categoría";

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.listarTodas().stream()
                .map(CategoriaResponse::desde)
                .toList();
    }

    public Categoria obtenerPorId(int idCategoria) {
        return categoriaRepository.buscarPorId(idCategoria)
                .orElseThrow(() -> new RecursoNoEncontradoException(RECURSO, idCategoria));
    }
}