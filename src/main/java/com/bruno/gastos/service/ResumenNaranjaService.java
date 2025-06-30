package com.bruno.gastos.service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
public class ResumenNaranjaService {
	private final GastoService gastoService;
    private final UsuarioRepository usuarioRepo;
    private final CategoriaRepository categoriaRepo;
    private final GastoRepository gastoRepo;

    public ResumenNaranjaService(GastoService gastoService, UsuarioRepository usuarioRepo,
                                  CategoriaRepository categoriaRepo, GastoRepository gastoRepo) {
        this.gastoService = gastoService;
        this.usuarioRepo = usuarioRepo;
        this.categoriaRepo = categoriaRepo;
        this.gastoRepo = gastoRepo;
    }

    public int procesarResumenPdf(MultipartFile archivo, Long usuarioId) throws Exception {
        // Guardar el archivo temporalmente en disco (porque iText no acepta InputStream directamente en todas versiones)
        File tempFile = File.createTempFile("resumen", ".pdf");
        archivo.transferTo(tempFile); // guarda el archivo en el disco

        StringBuilder textoCompleto = new StringBuilder();

        try (PdfReader reader = new PdfReader(tempFile); PdfDocument pdfDoc = new PdfDocument(reader)) {
            int totalPaginas = pdfDoc.getNumberOfPages();

            for (int i = 1; i <= totalPaginas; i++) {
                String textoPagina = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                textoCompleto.append(textoPagina).append("\n");
            }

        } finally {
            tempFile.delete(); // borra el archivo temporal
        }

        String texto = textoCompleto.toString();
        System.out.println(">>> Texto extraído del PDF: " + texto.length() + " caracteres");
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
        
        
        // Filtrado por sección
        String nombreSeccion = usuario.getNombre().trim();
        String seccionRegex = "(?s)Consumos\\s+tarjeta\\s+de\\s+crédito\\s+de\\s+" + Pattern.quote(nombreSeccion) + ".*?(?=Consumos\\s+tarjeta\\s+de\\s+crédito\\s+de\\s+|$)";
        Pattern seccionPattern = Pattern.compile(seccionRegex, Pattern.CASE_INSENSITIVE);
        Matcher seccionMatcher = seccionPattern.matcher(texto);
        if (!seccionMatcher.find()) return 0;
        
        String seccionTexto = seccionMatcher.group();
        String[] lineas = seccionTexto.split("\\r?\\n");
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        	
        // Línea con formato: 22/06/2025 123456 Televisor 02/06 60000
        String regex = "(?<fecha>\\d{2}/\\d{2}/\\d{2,4})\\s+.*?\\s+(?<cupon>\\d{3,6})\\s+(?<descripcion>.+?)\\s+(?<cuotaPlan>Zeta|\\d{2}/\\d{2}|01|Deb\\.Aut\\.)\\s+(?<monto>\\d{1,3}(?:[.,]\\d{3})*[.,]\\d{2})";
        Pattern patron = Pattern.compile(regex);


        int importados = 0;
        DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("dd/MM/yy");
        for (String linea : lineas) {
            Matcher matcher = patron.matcher(linea);
            if (matcher.find()) {
                try {
                	
                	LocalDate fecha = LocalDate.parse(matcher.group("fecha"), formatoCorto);
                	String cupon = matcher.group("cupon");
                	String descripcion = matcher.group("descripcion").trim();
                	String cuotaPlan = matcher.group("cuotaPlan").toUpperCase();
                	String montoStr = matcher.group("monto")
                		    .replace(".", "")   // eliminamos puntos (separadores de miles)
                		    .replace(",", "."); // reemplazamos coma decimal por punto decimal
                	double monto = Double.parseDouble(montoStr);

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
                    System.err.println("Error al parsear línea: " + linea);
                    e.printStackTrace();
                }
            }
        }
        return importados;
    }
    
    private void configurarPago(Gasto gasto, String cuotaPlan, double monto, LocalDate fecha) {
    	if (cuotaPlan.equalsIgnoreCase("ZETA")) {
            int cantidadCuotas = 3;
            gasto.setCuotas(cantidadCuotas);
            gasto.setMontoTotal(monto * cantidadCuotas);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
            gasto.setMedioPago(MedioPago.CREDITO);

        } else if (cuotaPlan.matches("\\d{2}/\\d{2}")) {
            String[] partes = cuotaPlan.split("/");
            int total = Integer.parseInt(partes[1]);
            gasto.setCuotas(total);
            gasto.setMontoTotal(monto * total);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
            gasto.setMedioPago(MedioPago.CREDITO);

        } else if (cuotaPlan.equalsIgnoreCase("DEB.AUT.")) {
            gasto.setCuotas(0);
            gasto.setMontoTotal(monto);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
            gasto.setMedioPago(MedioPago.DEBITO);

        } else if (cuotaPlan.equals("01")) {
            gasto.setCuotas(0);
            gasto.setMontoTotal(monto);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
            gasto.setMedioPago(MedioPago.EFECTIVO);

        } else {
            gasto.setCuotas(0);
            gasto.setMontoTotal(monto);
            gasto.setMesInicioCuotas(fecha.plusMonths(1));
            gasto.setMedioPago(MedioPago.OTRO); 
        }
    }
}
