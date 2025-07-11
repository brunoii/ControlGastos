package com.bruno.gastos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

@Entity
public class Usuario {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    public Long getId() { return id; }
    
    public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
