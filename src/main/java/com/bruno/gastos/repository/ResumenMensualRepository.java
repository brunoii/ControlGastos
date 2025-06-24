package com.bruno.gastos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bruno.gastos.model.ResumenMensual;

public interface ResumenMensualRepository extends JpaRepository<ResumenMensual, Long> {
    Optional<ResumenMensual> findByMesIdAndUsuarioId(Long mesId, Long usuarioId);
}
