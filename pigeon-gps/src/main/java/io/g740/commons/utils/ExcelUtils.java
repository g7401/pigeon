package io.g740.commons.utils;

import io.g740.commons.types.TwoTuple;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author bbottong
 */
public class ExcelUtils {
    // 单sheet最大行数限制，减1是考虑了一行表头
    public static final int MAX_ROWS_PER_SHEET = 1048576 - 1;

    private static final int INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS = 10;

    public static final int EXCEL_SHEET_MAX_ROWS_COUNT = 1048576 - 1;
    public static final String EXCEL_SHEET_DEFAULT_NAME_PREFIX = "sheet";
    public static final String EXCEL_DEFAULT_NAME_SUFFIX = ".xlsx";
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 从Excel File的指定sheet中解析Header
     *
     * @param file
     * @param headerRowNum
     * @param sheetIndex
     * @return
     * @throws IOException
     */
    public static List<String> extractHeaderFromExcelFile(File file, Integer headerRowNum, int sheetIndex)
            throws IOException {
        List<String> header = new LinkedList<>();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            //
            // 开始读取header行
            //
            if (headerRowNum < firstRowNum) {
                String errorMessage = "headerRowNum " + headerRowNum + " < " + "firstRowNum " + firstRowNum;
                throw new IOException(errorMessage);
            }
            Row headerRow = sheet.getRow(headerRowNum);
            int firstCellNum = headerRow.getFirstCellNum();
            int lastCellNum = headerRow.getLastCellNum();
            int cellNum = firstCellNum;
            while (cellNum < lastCellNum) {
                Cell cellOfHeaderRow = headerRow.getCell(cellNum);
                header.add(cellOfHeaderRow.getStringCellValue());

                cellNum++;
            }
        }

        return header;
    }

    /**
     * 从Excel File的指定sheet中解析Header并推测每列的数据类型
     *
     * @param file
     * @param headerRowNum
     * @return
     * @throws IOException
     */
    public static List<TwoTuple<String, Class<?>>> extractHeaderAndInferDataTypesFromExcelFile(File file,
                                                                                               Integer headerRowNum, int sheetIndex) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file)) {
            Sheet sheet = workbook.getSheetAt(sheetIndex);

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            //
            // 开始读取header行
            //

            // header每列的列名
            List<String> listOfHeader = new ArrayList<>();

            if (headerRowNum < firstRowNum) {
                String errorMessage = "headerRowNum " + headerRowNum + " < " + "firstRowNum " + firstRowNum;
                throw new IOException(errorMessage);
            }
            Row headerRow = sheet.getRow(headerRowNum);
            Row realHeaderRow=sheet.getRow(firstRowNum);
            //修改处全部以Excel的首行有多少列为准
            int firstCellNum = realHeaderRow.getFirstCellNum();
            //cellNum从1开始
            int lastCellNum = realHeaderRow.getLastCellNum();
            int cellNum = firstCellNum;
            while (cellNum < lastCellNum) {
                Cell cellOfHeaderRow = headerRow.getCell(cellNum);
                if(cellOfHeaderRow==null) {
                    throw new Exception("head cell is null");
                }
                String cellValue = null;
                switch (cellOfHeaderRow.getCellType()) {
                    case NUMERIC: {
                        if (DateUtil.isCellDateFormatted(cellOfHeaderRow)) {
                            CellStyle cellStyle = cellOfHeaderRow.getCellStyle();
                            cellValue = sdf.format(cellOfHeaderRow.getDateCellValue());
                        } else {
                            if (String.valueOf(cellOfHeaderRow.getNumericCellValue()).indexOf("E") == -1) {
                                cellValue=	 String.valueOf(cellOfHeaderRow.getNumericCellValue());
                            } else {

                                cellValue=new DecimalFormat("#").format(cellOfHeaderRow.getNumericCellValue());
                            }
//						double cellValueAsDouble = cellOfHeaderRow.getNumericCellValue();
//						cellValue = String.valueOf(cellValueAsDouble);
                        }
                        break;
                    }
                    case STRING: {
                        cellValue = cellOfHeaderRow.getStringCellValue();
                        break;
                    }
                    case BOOLEAN:{
                        boolean booleanCellValue = cellOfHeaderRow.getBooleanCellValue();
                        if(booleanCellValue) {
                            cellValue="true";
                        }else {
                            cellValue="false";
                        }
                        break;
                    }
                    default:
                        throw new Exception("head cell is null");
//				 break;
                }
                listOfHeader.add(cellValue);

                cellNum++;
            }

            if (listOfHeader.isEmpty()) {
                return null;
            }

            //
            // 读取header之后的N行，推测每列数据类型
            //

            // header每列及其确定的数据类型，需要通过infer N条数据行才能得到
            List<TwoTuple<String, Class<?>>> listOfHeaderAndDeterminedDataType = new ArrayList<>(listOfHeader.size());
            // header每列及其候选的数据类型，infer header 每列数据类型的过程中，收集到的每列的候选数据类型
            List<TwoTuple<String, List<Class<?>>>> listOfHeaderAndCandidateDataType = new ArrayList<>(
                    listOfHeader.size());

            int startReadRowNum = headerRowNum + 1;
            int endReadRowNum = startReadRowNum + INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS;
            if (lastRowNum < endReadRowNum) {
                // 如果不够INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS行来推测每列的数据类型，则只能少用一些行
                endReadRowNum = lastRowNum;
            }
            int dataRowNum = startReadRowNum;
            while (dataRowNum <= endReadRowNum) {
                Row dataRow = sheet.getRow(dataRowNum);

                if (dataRow.getFirstCellNum() != firstCellNum || dataRow.getLastCellNum() != lastCellNum) {
                    //
                }

                cellNum = firstCellNum;
                while (cellNum < lastCellNum) {
                    int index = cellNum - firstCellNum;

                    List<Class<?>> listOfCandidateDataType = null;
                    TwoTuple<String, List<Class<?>>> tuple = null;
                    if (listOfHeaderAndCandidateDataType.size() > index) {
                        tuple = listOfHeaderAndCandidateDataType.get(index);
                    }

                    if (tuple == null) {
                        listOfCandidateDataType = new LinkedList<>();
                        tuple = new TwoTuple<>(listOfHeader.get(index), listOfCandidateDataType);
                        listOfHeaderAndCandidateDataType.add(index, tuple);
                    } else {
                        listOfCandidateDataType = tuple.s;
                    }

                    Cell cellOfDataRow = dataRow.getCell(cellNum);
                    if (cellOfDataRow == null) {
                        listOfCandidateDataType.add(String.class);
                    } else {
                        CellType cellType = cellOfDataRow.getCellType();
                        switch (cellType) {
                            case BOOLEAN:
                                listOfCandidateDataType.add(Boolean.class);
                                break;
                            case STRING:
                                listOfCandidateDataType.add(String.class);
                                break;
                            case NUMERIC:
                                try {
                                    cellOfDataRow.getNumericCellValue();
                                    listOfCandidateDataType.add(Double.class);
                                } catch (Exception e2) {
                                    listOfCandidateDataType.add(String.class);
                                }
                                break;
                            default:
                                try {
                                    cellOfDataRow.getDateCellValue();
                                    listOfCandidateDataType.add(Date.class);
                                } catch (Exception e1) {
                                    listOfCandidateDataType.add(String.class);
                                }
                                break;
                        }
                    }

                    cellNum++;
                }

                dataRowNum++;
            }

            //
            // 从每个header的candidate data types中找出出现次数最多的那个data type，作为最终data type
            //
            for (int i = 0; i < listOfHeaderAndCandidateDataType.size(); i++) {
                TwoTuple<String, List<Class<?>>> tuple = listOfHeaderAndCandidateDataType.get(i);
                List<Class<?>> listOfCandidateDataType = tuple.s;

                Map<Class<?>, Integer> count = new HashMap<>();
                for (Class<?> clazz : listOfCandidateDataType) {
                    if (count.get(clazz) == null) {
                        count.put(clazz, 1);
                    } else {
                        count.put(clazz, count.get(clazz) + 1);
                    }
                }

                Class<?> clazz = null;
                Integer max = 0;

                for (Map.Entry<Class<?>, Integer> entry : count.entrySet()) {
                    if (entry.getValue() > max) {
                        max = entry.getValue();
                        clazz = entry.getKey();
                    }
                }

                TwoTuple<String, Class<?>> determinedDataTypeTuple = new TwoTuple<>(tuple.f, clazz);
                listOfHeaderAndDeterminedDataType.add(i, determinedDataTypeTuple);
            }

            return listOfHeaderAndDeterminedDataType;
        }
    }
}
