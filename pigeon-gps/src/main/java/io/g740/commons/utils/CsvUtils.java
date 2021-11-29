package io.g740.commons.utils;


import io.g740.commons.types.TwoTuple;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CsvUtils {
    private static final int INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS = 10;

    /**
     * 从CSV File中解析Header并推测每列的数据类型
     *
     * @param file
     * @param textEncoding
     * @param headerRowNum
     * @param delimiter
     * @return
     * @throws IOException
     */
    public static List<TwoTuple<String, Class<?>>> extractHeaderAndInferDataTypesFromCsvFile(File file, String
            textEncoding,
                                                                                             Integer
                                                                                                     headerRowNum, String
                                                                                                     delimiter)
            throws IOException {
        // 每个header column最终有且只有1个determined data type
        List<TwoTuple<String, Class<?>>> listOfHeaderAndDeterminedDataType = new LinkedList<>();
        // 每个header column有N个candidate data types
        List<TwoTuple<String, List<Class<?>>>> listOfHeaderAndCandidateDataType = new LinkedList<>();
        int headerColumnCount = 0;

        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(file), Charset.forName(textEncoding));
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            int rowNo = 0;
            while ((line = bufferedReader.readLine()) != null) {
                if (rowNo < headerRowNum) {
                    rowNo++;
                    continue;
                }
                if (rowNo == headerRowNum) {
                    //
                    // 读取header row，获取header row的列名
                    //
                    String[] header = line.split(delimiter);
                    headerColumnCount = header.length;
                    for (int i = 0; i < header.length; i++) {
                        TwoTuple<String, Class<?>> tuple = new TwoTuple<>(header[i], String.class);

                        listOfHeaderAndDeterminedDataType.add(tuple);
                    }
                } else if (rowNo < headerRowNum + INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS) {
                    //
                    // 读取header之后的N行，推测每列数据类型
                    //

                    String[] values = line.split(delimiter);
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];

                        List<Class<?>> listOfCandidateDataType = null;
                        TwoTuple<String, List<Class<?>>> tuple = null;
                        if (listOfHeaderAndCandidateDataType.size() > i)
                            tuple = listOfHeaderAndCandidateDataType.get(i);
                        if (tuple == null) {
                            listOfCandidateDataType = new LinkedList<>();
                            tuple = new TwoTuple<>(listOfHeaderAndDeterminedDataType.get(i).f, listOfCandidateDataType);
                            listOfHeaderAndCandidateDataType.add(i, tuple);
                        } else {
                            listOfCandidateDataType = tuple.s;
                        }

                        try {
                            Integer.parseInt(value);
                            listOfCandidateDataType.add(Integer.class);
                        } catch (NumberFormatException e1) {
                            try {
                                Long.parseLong(value);
                                listOfCandidateDataType.add(Long.class);
                            } catch (NumberFormatException e2) {
                                try {
                                    Double.parseDouble(value);
                                    listOfCandidateDataType.add(Double.class);
                                } catch (NumberFormatException e3) {
                                    try {
                                        Boolean.parseBoolean(value);
                                        if ("TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value)) {
                                            listOfCandidateDataType.add(Boolean.class);
                                        } else {
                                            listOfCandidateDataType.add(String.class);
                                        }
                                    } catch (NumberFormatException e4) {
                                        try {
                                            Date.valueOf(value);
                                            listOfCandidateDataType.add(Date.class);
                                        } catch (Exception e5) {
                                            try {
                                                Timestamp.valueOf(value);
                                                listOfCandidateDataType.add(Timestamp.class);
                                            } catch (Exception e6) {
                                                listOfCandidateDataType.add(String.class);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                rowNo++;

                if (rowNo >= headerRowNum + INFER_COLUMN_DATA_TYPE_BY_ANALYZING_N_ROWS)
                    break;
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

                listOfHeaderAndDeterminedDataType.get(i).s = clazz;
            }


            return listOfHeaderAndDeterminedDataType;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}