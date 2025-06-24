package com.bruno.gastos.controller;

import com.bruno.gastos.model.Categoria;
import com.bruno.gastos.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
	private final CategoriaRepository repo;

    public CategoriaController(CategoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Categoria> obtener() {
        return repo.findAll();
    }
}
