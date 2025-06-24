package com.bruno.gastos.repository;

import com.bruno.gastos.model.Ingreso;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IngresoRepository extends JpaRepository<Ingreso, Long> {
	List<Ingreso> findByUsuarioIdAndMesContableId(Long usuarioId, Long mesContableId);
}
