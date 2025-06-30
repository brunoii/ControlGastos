package com.bruno.gastos.service;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bruno.gastos.model.Categoria;
import com.bruno.gastos.model.Gasto;
import com.bruno.gastos.model.MedioPago;
import com.bruno.gastos.model.Usuario;
import com.bruno.gastos.repository.CategoriaRepository;
import com.bruno.gastos.repository.GastoRepository;
import com.bruno.gastos.repository.UsuarioRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

@Service
public class ResumenSantanderService {
	private final UsuarioRepository usuarioRepo;
    private final GastoService gastoService;
    private final GastoRepository gastoRepo;
    private final CategoriaRepository categoriaRepo;

    public ResumenSantanderService(UsuarioRepository usuarioRepo, GastoService gastoService, GastoRepository gastoRepo, CategoriaRepository categoriaRepo) {
        this.usuarioRepo = usuarioRepo;
        this.gastoService = gastoService;
        this.gastoRepo = gastoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    public int procesarResumenPdf(MultipartFile archivo, Long usuarioId) throws Exception {
        File tempFile = File.createTempFile("resumen", ".pdf");
        archivo.transferTo(tempFile);

        StringBuilder textoCompleto = new StringBuilder();

        try (PdfReader reader = new PdfReader(tempFile); PdfDocument pdfDoc = new PdfDocument(reader)) {
            int totalPaginas = pdfDoc.getNumberOfPages();

            for (int i = 1; i <= totalPaginas; i++) {
                String textoPagina = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                textoCompleto.append(textoPagina).append("\n");
            }

        } finally {
            tempFile.delete();
        }

        String texto = textoCompleto.toString();
        System.out.println(">>> Texto extraído del PDF Santander: " + texto.length() + " caracteres");
        return procesarTextoPlano(texto, usuarioId);
    }

    private int procesarTextoPlano(String texto, Long usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepo.findById(usuarioId);
        if (usuarioOpt.isEmpty()) throw new RuntimeException("Usuario no encontrado");
        Usuario usuario = usuarioOpt.get();
        
        long idCategoriaGastosVarios = 6;
        Optional<Categoria> categoriaOpt = categoriaRepo.findById(idCategoriaGastosVarios);
        if (categoriaOpt.isEmpty()) throw new RuntimeException("Categoria no encontrada");
        Categoria gastoVariosCategoria = categoriaOpt.get();

        String[] lineas = texto.split("\\r?\\n");
        Map<String, Integer> meses = Map.ofEntries(
            Map.entry("enero", 1), Map.entry("febrero", 2), Map.entry("marzo", 3),
            Map.entry("abril", 4), Map.entry("mayo", 5), Map.entry("junio", 6),
            Map.entry("julio", 7), Map.entry("agosto", 8),
            Map.entry("septie.", 9), Map.entry("setiem", 9),
            Map.entry("octubre", 10), Map.entry("noviem", 11), Map.entry("diciem", 12)
        );

        String regexCompleta =
        	    "^(?<anio>\\d{2})\\s+(?<mes>\\p{L}+\\.?)+\\s+(?<dia>\\d{2})\\s+" +
        	    "(?<cupon>\\d{6})\\s+[\\*K]?\\s+(?<descripcion>.*?)\\s+" +
        	    "(?<cuotaPlan>C\\.\\d{2}/\\d{2})?\\s+" +
        	    "(?<monto>\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$";

    	String regexSoloDia =
	    	    "^\\s*(?<dia>\\d{2})\\s+(?<cupon>\\d{6})\\s+[\\*K]?\\s+" +
	    	    "(?<descripcion>.*?)\\s+" +
	    	    "(?<cuotaPlan>C\\.\\d{2}/\\d{2})?\\s+" +
	    	    "(?<monto>\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$";

        Pattern patronCompleta = Pattern.compile(regexCompleta, Pattern.UNICODE_CHARACTER_CLASS);
        Pattern patronParcial = Pattern.compile(regexSoloDia, Pattern.UNICODE_CHARACTER_CLASS);

        int importados = 0;
        Integer anioActual = null;
        Integer mesActual = null;

        for (String linea : lineas) {
            Matcher matcherCompleta = patronCompleta.matcher(linea);
            Matcher matcherParcial = patronParcial.matcher(linea);

            try {
                LocalDate fecha;
                String cupon, descripcion, cuotaPlan = null, montoStr;

                if (matcherCompleta.find()) {
                    String anioStr = matcherCompleta.group("anio");
                    String mesStr = matcherCompleta.group("mes").toLowerCase().replace(".", "").trim();
                    String diaStr = matcherCompleta.group("dia");

                    anioActual = 2000 + Integer.parseInt(anioStr);
                    mesActual = meses.get(mesStr);
                    int dia = Integer.parseInt(diaStr);
                    fecha = LocalDate.of(anioActual, mesActual, dia);
                    cupon = matcherCompleta.group("cupon");
                    descripcion = matcherCompleta.group("descripcion").trim();
                    cuotaPlan = matcherCompleta.group("cuotaPlan");
                    montoStr = matcherCompleta.group("monto");
                } else if (matcherParcial.find() && anioActual != null && mesActual != null) {
                    int dia = Integer.parseInt(matcherParcial.group("dia"));
                    fecha = LocalDate.of(anioActual, mesActual, dia);
                    cupon = matcherParcial.group("cupon");
                    descripcion = matcherParcial.group("descripcion").trim();
                    cuotaPlan = matcherParcial.group("cuotaPlan");
                    montoStr = matcherParcial.group("monto");
                } else {
                    continue; // línea no compatible
                }

                double monto = Double.parseDouble(
                    montoStr.replace(".", "").replace(",", ".")
                );
                
                // Evitar duplicados por cupon
                if (gastoRepo.existsByCupon(cupon)) continue;
                
                Gasto gasto = new Gasto();
                gasto.setUsuario(usuario);
                gasto.setFechaOperacion(fecha);
                gasto.setDescripcion(descripcion);                    
                gasto.setCategoria(gastoVariosCategoria); 
                gasto.setEsTercero(false);
                gasto.setFueReembolsado(false);
                gasto.setCupon(cupon);
                
                configurarPago(gasto, cuotaPlan, monto, fecha);
                
                gastoService.registrarGasto(gasto);
                importados++;

            } catch (Exception e) {
                System.err.println("Error al procesar línea: " + linea);
                e.printStackTrace();
            }
        }

        return importados;
    }
    
    private void configurarPago(Gasto gasto, String cuotaPlan, double monto, LocalDate fecha) {
        if (cuotaPlan == null) {
            // Pago contado, débito
            gasto.setCuotas(0);
            gasto.setMontoTotal(monto);
            gasto.setMedioPago(MedioPago.DEBITO);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
        } else if (cuotaPlan.matches("C\\.\\d{2}/\\d{2}")) {
            // Ejemplo: C.04/06
            String[] partes = cuotaPlan.substring(2).split("/"); // saca el "C." y separa "04/06"
            int totalCuotas = Integer.parseInt(partes[1]);
            gasto.setCuotas(totalCuotas);
            gasto.setMontoTotal(monto * totalCuotas); // monto es cuota individual
            gasto.setMesInicioCuotas(fecha);
            gasto.setMedioPago(MedioPago.CREDITO);
            gasto.setMesInicioCuotas(fecha.plusMonths(1)); // Primera cuota el mes siguiente
        } else {
            // Desconocido, se guarda como OTRO
            gasto.setCuotas(0);
            gasto.setMontoTotal(monto);
            gasto.setMedioPago(MedioPago.OTRO);
            gasto.setMesInicioCuotas(fecha.plusMonths(1)); // Por defecto también al mes siguiente
        }
    }
}

