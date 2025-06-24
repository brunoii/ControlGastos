package com.bruno.gastos.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
public class ResumenMensual {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MesContable mes;

    @ManyToOne
    private Usuario usuario;

    private double totalIngresos;
    private double totalGastos;
    private double totalCuotas;
    private double saldoFinal;

    private LocalDate fechaCierre;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MesContable getMes() {
		return mes;
	}

	public void setMes(MesContable mes) {
		this.mes = mes;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public double getTotalIngresos() {
		return totalIngresos;
	}

	public void setTotalIngresos(double totalIngresos) {
		this.totalIngresos = totalIngresos;
	}

	public double getTotalGastos() {
		return totalGastos;
	}

	public void setTotalGastos(double totalGastos) {
		this.totalGastos = totalGastos;
	}

	public double getTotalCuotas() {
		return totalCuotas;
	}

	public void setTotalCuotas(double totalCuotas) {
		this.totalCuotas = totalCuotas;
	}

	public double getSaldoFinal() {
		return saldoFinal;
	}

	public void setSaldoFinal(double saldoFinal) {
		this.saldoFinal = saldoFinal;
	}

	public LocalDate getFechaCierre() {
		return fechaCierre;
	}

	public void setFechaCierre(LocalDate fechaCierre) {
		this.fechaCierre = fechaCierre;
	}
    
    
}
