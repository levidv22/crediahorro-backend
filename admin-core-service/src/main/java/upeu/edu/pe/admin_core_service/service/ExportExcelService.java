package upeu.edu.pe.admin_core_service.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import upeu.edu.pe.admin_core_service.entities.Cliente;
import upeu.edu.pe.admin_core_service.entities.Cuota;
import upeu.edu.pe.admin_core_service.entities.Prestamo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ExportExcelService {

    public ByteArrayInputStream exportarPrestamoPagado(Cliente cliente, Prestamo prestamo) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte de Préstamo");

            // ==== ESTILOS ====
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);

            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Font sectionFont = workbook.createFont();
            sectionFont.setBold(true);

            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(sectionFont);
            sectionStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle labelStyle = workbook.createCellStyle();
            labelStyle.setFont(sectionFont);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderTop(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);
            labelStyle.setBorderLeft(BorderStyle.THIN);
            labelStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle valueStyle = workbook.createCellStyle();
            valueStyle.setBorderBottom(BorderStyle.THIN);
            valueStyle.setBorderTop(BorderStyle.THIN);
            valueStyle.setBorderRight(BorderStyle.THIN);
            valueStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            CellStyle borderedStyle = workbook.createCellStyle();
            borderedStyle.setBorderBottom(BorderStyle.THIN);
            borderedStyle.setBorderTop(BorderStyle.THIN);
            borderedStyle.setBorderRight(BorderStyle.THIN);
            borderedStyle.setBorderLeft(BorderStyle.THIN);

            int rowIdx = 0;

            // ==== TÍTULO ====
            Row titulo = sheet.createRow(rowIdx++);
            Cell celdaTitulo = titulo.createCell(0);
            celdaTitulo.setCellValue("Reporte de Préstamo Pagado");
            celdaTitulo.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            rowIdx++;

            // ==== DATOS DEL CLIENTE (como tabla 2 columnas) ====
            Row sectionCliente = sheet.createRow(rowIdx++);
            Cell clienteHeader = sectionCliente.createCell(0);
            clienteHeader.setCellValue("Datos del Cliente");
            clienteHeader.setCellStyle(sectionStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));

            String[][] datosCliente = {
                    {"Nombre", cliente.getNombre()},
                    {"DNI", cliente.getDni()},
                    {"Dirección", cliente.getDireccion()},
                    {"Correo", cliente.getCorreoElectronico()},
                    {"Teléfono", cliente.getTelefonoWhatsapp()},
                    {"Fecha de Registro", String.valueOf(cliente.getFechaCreacion())}
            };

            for (String[] fila : datosCliente) {
                Row row = sheet.createRow(rowIdx++);
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(fila[0]);
                labelCell.setCellStyle(labelStyle);

                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(fila[1]);
                valueCell.setCellStyle(valueStyle);
            }

            rowIdx++;

            // ==== DATOS DEL PRÉSTAMO ====
            Row sectionPrestamo = sheet.createRow(rowIdx++);
            Cell prestamoHeader = sectionPrestamo.createCell(0);
            prestamoHeader.setCellValue("Datos del Préstamo");
            prestamoHeader.setCellStyle(sectionStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));

            String[][] datosPrestamo = {
                    {"Monto", "S/ " + prestamo.getMonto()},
                    {"Tasa de interés mensual", prestamo.getTasaInteresMensual() + "%"},
                    {"Número de cuotas", String.valueOf(prestamo.getNumeroCuotas())},
                    {"Tipo de cuota", prestamo.getTipoCuota()},
                    {"Interés total", "S/ " + prestamo.getInteresTotal()},
                    {"Monto total", "S/ " + prestamo.getMontoTotal()},
                    {"Fecha inicio", String.valueOf(prestamo.getFechaInicio())},
                    {"Estado", prestamo.getEstado()}
            };

            for (String[] fila : datosPrestamo) {
                Row row = sheet.createRow(rowIdx++);
                Cell labelCell = row.createCell(0);
                labelCell.setCellValue(fila[0]);
                labelCell.setCellStyle(labelStyle);

                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(fila[1]);
                valueCell.setCellStyle(valueStyle);
            }

            rowIdx++;

            // ==== TABLA DE CUOTAS ====
            Row tablaHeader = sheet.createRow(rowIdx++);
            String[] columnas = {"N°", "Fecha a Pagar", "Monto", "Fecha Pagada", "Tipo de Pago"};

            for (int i = 0; i < columnas.length; i++) {
                Cell cell = tablaHeader.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int num = 1;
            for (Cuota cuota : prestamo.getCuotas()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(num++);
                row.createCell(1).setCellValue(String.valueOf(cuota.getFechaPago()));
                row.createCell(2).setCellValue(cuota.getMontoCuota());
                row.createCell(3).setCellValue(cuota.getFechaPagada() != null ? cuota.getFechaPagada().toString() : "");
                row.createCell(4).setCellValue(cuota.getTipoPago());

                for (int i = 0; i < 5; i++) {
                    row.getCell(i).setCellStyle(borderedStyle);
                }
            }

            // Auto-ajustar columnas
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}

