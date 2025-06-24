package com.bruno.gastos.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Cuota {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Gasto gasto;

    private Double montoCuota;
    private LocalDate fechaCuota;
    private Integer numeroCuota; 
    private Integer totalCuotas;

    @ManyToOne
    private MesContable mesContable;

	public Gasto getGasto() {
		return gasto;
	}

	public void setGasto(Gasto gasto) {
		this.gasto = gasto;
	}

	public Double getMontoCuota() {
		return montoCuota;
	}

	public void setMontoCuota(Double montoCuota) {
		this.montoCuota = montoCuota;
	}

	public LocalDate getFechaCuota() {
		return fechaCuota;
	}

	public void setFechaCuota(LocalDate fechaCuota) {
		this.fechaCuota = fechaCuota;
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

	public Integer getNumeroCuota() {
		return numeroCuota;
	}

	public void setNumeroCuota(Integer numeroCuota) {
		this.numeroCuota = numeroCuota;
	}

	public Integer getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(Integer totalCuotas) {
		this.totalCuotas = totalCuotas;
	}
    
    
}
