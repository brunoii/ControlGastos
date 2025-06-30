package com.bruno.gastos.controller;

import com.bruno.gastos.service.ResumenNaranjaService;
import com.bruno.gastos.service.ResumenSantanderService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/importar")
public class ImportarResumenController {
	private final ResumenNaranjaService resumenService;
	private final ResumenSantanderService resumenSantanderService;

    public ImportarResumenController(ResumenNaranjaService resumenService, ResumenSantanderService resumenSantanderService) {
        this.resumenService = resumenService;
        this.resumenSantanderService = resumenSantanderService;
    }

    @PostMapping(value = "/naranja", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importarResumen(@RequestPart("archivo") MultipartFile archivo,
            @RequestPart("usuarioId") String usuarioIdStr){
    	Long usuarioId = Long.parseLong(usuarioIdStr);
        try {
            int importados = resumenService.procesarResumenPdf(archivo, usuarioId);
            return ResponseEntity.ok("Importados correctamente: " + importados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar: " + e.getMessage());
        }
    }
    
    @PostMapping(value = "/santander", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importarResumenSantander(
            @RequestPart("archivo") MultipartFile archivo,
            @RequestPart("usuarioId") String usuarioIdStr) {
        try {
            Long usuarioId = Long.parseLong(usuarioIdStr);
            int importados = resumenSantanderService.procesarResumenPdf(archivo, usuarioId);
            return ResponseEntity.ok("Se importaron " + importados + " consumos de Santander correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar el resumen.");
        }
    }
}
