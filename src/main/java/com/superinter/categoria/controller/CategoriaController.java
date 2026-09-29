package com.superinter.categoria.controller;

import java.util.List;

import com.superinter.categoria.dto.CategoriaResponse;
import com.superinter.categoria.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST de categorías (solo lectura).
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaResponse> listarTodas() {
        return categoriaService.listarTodas();
    }
}