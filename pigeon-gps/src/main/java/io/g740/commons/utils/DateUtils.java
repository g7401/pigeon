package io.g740.commons.utils;

public class DateUtils {
    public static String format(long millis) {
        if (millis <= 0) {
            return "0";
        }

        StringBuilder formatted = new StringBuilder();

        if (millis > 60 * 60 * 1000) {
            // 按小时，分，格式化
            long hours = millis / (60 * 60 * 1000);
            long mins = (millis - hours * 60 * 60 * 1000) / (60 * 1000);
            if (mins == 0) {
                formatted.append(hours).append("小时");
            } else {
                formatted.append(hours).append("小时").append(mins).append("分钟");
            }
        } else if (millis > 60 * 1000) {
            // 按分，秒，格式化
            long mins = millis / (60 * 1000);
            long seconds = (millis - mins * 60 * 1000) / 1000;
            if (seconds == 0) {
                formatted.append(mins).append("分钟");
            } else {
                formatted.append(mins).append("分钟").append(seconds).append("秒");
            }
        } else if (millis > 1000) {
            // 按秒，毫秒格式化
            long seconds = millis / 1000;
            millis = millis % 1000;
            if (millis == 0) {
                formatted.append(seconds).append("秒");
            } else {
                formatted.append(seconds).append("秒").append(millis).append("毫秒");
            }
        } else {
            formatted.append(millis).append("毫秒");
        }

        return formatted.toString();
    }

    public static void main(String[] args) {

        long millis = 3 * 60 * 60 * 1000;
        System.out.println(format(millis));
    }
}
