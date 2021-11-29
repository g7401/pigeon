package io.g740.pigeon.biz.service.handler.deployment;

import io.g740.commons.constants.CsvExportStrategyEnum;

import java.util.List;
import java.util.Map;

public class SplitCsvRunnable implements Runnable {
    private final List<Map<String, Object>> content;
    private final List<String> requiredDataFieldsInOrder;

    private final CsvExportStrategyEnum csvExportStrategy;

    private final Integer fileSegmentSize;

    private final StringBuilder lines;

    public SplitCsvRunnable(
            List<Map<String, Object>> content,
            List<String> requiredDataFieldsInOrder,
            CsvExportStrategyEnum csvExportStrategy,
            Integer fileSegmentSize) {
        this.content = content;
        this.requiredDataFieldsInOrder = requiredDataFieldsInOrder;
        this.csvExportStrategy = csvExportStrategy;
        this.fileSegmentSize = fileSegmentSize;

        lines = new StringBuilder();
    }

    @Override
    public void run() {
        for (int no = 0; no < content.size(); no++) {
            Map<String, Object> contentTuple = content.get(no);
            // 遍历每一列
            for (String fieldName : requiredDataFieldsInOrder) {
                if (contentTuple.containsKey(fieldName)) {
                    Object fieldValue = contentTuple.get(fieldName);

                    if (fieldValue != null) {
                        if (fieldValue instanceof String) {
                            String fieldValueStr = (String) fieldValue;

                            switch (csvExportStrategy) {
                                case DEFAULT:
                                    lines.append(fieldValueStr).append(",");
                                    break;
                                case REPLACE:
                                    fieldValueStr = fieldValueStr.replaceAll("[,]", " ");
                                    fieldValueStr = fieldValueStr.replaceAll("[\\r\\n]", " ");
                                    fieldValueStr = fieldValueStr.replaceAll("\"", " ");
                                    lines.append(fieldValueStr).append(",");
                                    break;
                                case RFC:
                                    fieldValueStr = fieldValueStr.replaceAll("\"", "\"\"");
                                    lines.append("\"").append(fieldValueStr).append("\"").append(",");
                                    break;
                            }
                        } else {
                            lines.append(fieldValue).append(",");
                        }
                    } else {
                        lines.append(",");
                    }
                }
            }
            lines.deleteCharAt(lines.length() - 1);
            lines.append("\r\n");
        }
        lines.deleteCharAt(lines.length() - 1);
        lines.deleteCharAt(lines.length() - 1);
    }

    public StringBuilder getLines() {
        return this.lines;
    }
}
