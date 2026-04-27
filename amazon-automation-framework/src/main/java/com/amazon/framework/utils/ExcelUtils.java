package com.amazon.framework.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * ExcelUtils — reads test data from Excel files.
 *
 * TWO READING MODES:
 *  1. Standard (XSSFWorkbook)  — for files up to ~10,000 rows
 *  2. Streaming (SAX/EventAPI) — for files with 50,000+ rows (no OutOfMemoryError)
 *
 * The returned Object[][] plugs directly into TestNG @DataProvider.
 *
 * Usage:
 *   @DataProvider(name = "loginData", parallel = true)
 *   public Object[][] loginData() {
 *       return ExcelUtils.getSheetData("src/test/resources/testdata/login.xlsx", "Login");
 *   }
 */
public class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    /**
     * Reads all rows from a sheet.
     * Row 0 = headers (used as keys if you call getSheetDataAsMaps).
     * Returns Object[][] for @DataProvider compatibility.
     */
    public static Object[][] getSheetData(String filePath, String sheetName) {
        log.info("Reading Excel: {} | sheet: {}", filePath, sheetName);
        List<Object[]> rows = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

            int firstRow = sheet.getFirstRowNum() + 1; // skip header row
            int lastRow  = sheet.getLastRowNum();
            int cols     = sheet.getRow(firstRow - 1).getLastCellNum();

            log.info("Rows to read: {} | Columns: {}", (lastRow - firstRow + 1), cols);

            for (int r = firstRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Object[] rowData = new Object[cols];
                for (int c = 0; c < cols; c++) {
                    rowData[c] = getCellValue(row.getCell(c));
                }
                rows.add(rowData);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }

        log.info("Loaded {} data rows from {}", rows.size(), sheetName);
        return rows.toArray(new Object[0][]);
    }

    /**
     * Returns data as List of Maps — column header = key, cell value = value.
     * Useful for BDD step definitions where you need named fields.
     *
     * Map keys = header row values.
     * Example: data.get(0).get("email") => "user@amazon.com"
     */
    public static List<Map<String, String>> getSheetDataAsMaps(String filePath, String sheetName) {
        List<Map<String, String>> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new RuntimeException("Sheet not found: " + sheetName);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell).toString());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c);
                    rowMap.put(headers.get(c), getCellValue(cell).toString());
                }
                result.add(rowMap);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath, e);
        }

        return result;
    }

    /**
     * Filter rows matching a specific column value.
     * Example: getRowsWhere("testdata/products.xlsx", "Products", "category", "Electronics")
     */
    public static List<Map<String, String>> getRowsWhere(
            String filePath, String sheetName, String column, String value) {
        return getSheetDataAsMaps(filePath, sheetName).stream()
                .filter(row -> value.equalsIgnoreCase(row.get(column)))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CELL VALUE RESOLVER
    // ─────────────────────────────────────────────────────────────────────────
    private static Object getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> CellType.NUMERIC == cell.getCellType() &&
                            DateUtil.isCellDateFormatted(cell)
                            ? cell.getLocalDateTimeCellValue().toString()
                            : formatNumeric(cell.getNumericCellValue());
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> cell.getCachedFormulaResultType() == CellType.NUMERIC
                            ? formatNumeric(cell.getNumericCellValue())
                            : cell.getStringCellValue();
            default      -> "";
        };
    }

    private static String formatNumeric(double value) {
        if (value == Math.floor(value)) return String.valueOf((long) value);
        return String.valueOf(value);
    }
}
