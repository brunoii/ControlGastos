package com.bruno.gastos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Ingreso {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private LocalDate fecha;
    private Double monto;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private MesContable mesContable;

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public Double getMonto() {
		return monto;
	}

	public void setMonto(Double monto) {
		this.monto = monto;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public MesContable getMesContable() {
		return mesContable;
	}

	public void setMesContable(MesContable mesContable) {
		this.mesContable = mesContable;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    
    
}
