package com.bruno.gastos.controller;

import com.bruno.gastos.model.Usuario;
import com.bruno.gastos.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class TestController {
	private final UsuarioRepository repo;

    public TestController(UsuarioRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Usuario> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Usuario save(@RequestBody Usuario u) {
        return repo.save(u);
    }
}
