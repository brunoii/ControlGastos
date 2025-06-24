package com.bruno.gastos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Gasto {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montoTotal;
    private LocalDate fechaOperacion;

    @Enumerated(EnumType.STRING)
    private MedioPago medioPago;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @ManyToOne
    @JoinColumn(name = "mes_contable_id")
    private MesContable mesContable;
    
    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    private Categoria categoria;

    private boolean esTercero;
    private String nombreTercero;
    private boolean fueReembolsado;

    private Integer cuotas; // 0 si no hay cuotas
    private LocalDate mesInicioCuotas;
	public Double getMontoTotal() {
		return montoTotal;
	}
	public void setMontoTotal(Double montoTotal) {
		this.montoTotal = montoTotal;
	}
	public LocalDate getFechaOperacion() {
		return fechaOperacion;
	}
	public void setFechaOperacion(LocalDate fechaOperacion) {
		this.fechaOperacion = fechaOperacion;
	}
	public MedioPago getMedioPago() {
		return medioPago;
	}
	public void setMedioPago(MedioPago medioPago) {
		this.medioPago = medioPago;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	public Categoria getCategoria() {
		return categoria;
	}
	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}
	public boolean isEsTercero() {
		return esTercero;
	}
	public void setEsTercero(boolean esTercero) {
		this.esTercero = esTercero;
	}
	public String getNombreTercero() {
		return nombreTercero;
	}
	public void setNombreTercero(String nombreTercero) {
		this.nombreTercero = nombreTercero;
	}
	public boolean isFueReembolsado() {
		return fueReembolsado;
	}
	public void setFueReembolsado(boolean fueReembolsado) {
		this.fueReembolsado = fueReembolsado;
	}
	public Integer getCuotas() {
		return cuotas;
	}
	public void setCuotas(Integer cuotas) {
		this.cuotas = cuotas;
	}
	public LocalDate getMesInicioCuotas() {
		return mesInicioCuotas;
	}
	public void setMesInicioCuotas(LocalDate mesInicioCuotas) {
		this.mesInicioCuotas = mesInicioCuotas;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public MesContable getMesContable() {
		return mesContable;
	}
	public void setMesContable(MesContable mesContable) {
		this.mesContable = mesContable;
	}
    
    
}
