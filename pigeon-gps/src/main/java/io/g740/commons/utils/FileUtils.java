package io.g740.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description :
 */
@Component
public class FileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 读取文本文件内容
     *
     * @param file
     * @return
     */
    public static String readContent(File file) {

        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        StringBuilder sb = null;
        try {
            fileInputStream = new FileInputStream(file);

            inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);

            bufferedReader = new BufferedReader(inputStreamReader);

            sb = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.error("", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }

            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }

            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                LOGGER.error("", e);
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return null;
    }

    public static void zipFile(String srcFilePath, String destFilePath) throws Exception {
        FileInputStream fis = null;
        ZipOutputStream zos = null;

        File file = new File(srcFilePath);
        try {
            fis = new FileInputStream(srcFilePath);
            FileOutputStream fos = new FileOutputStream(destFilePath);
            zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
        } catch (IOException e) {
            LOGGER.error("failed to compress src:" + srcFilePath + " to dest:" + destFilePath, e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 格式化文件长度，自适应用GB/MB/KB/B表示长度
     *
     * @param length
     * @return
     */
    public static String formatFileLength(long length) {
        String result;
        if (length > (1000 * 1000 * 1000)) {
            long integerPart = length / 1000 / 1000 / 1000;
            long remainder = length % (1000 * 1000 * 1000);
            if (remainder > 0) {
                result = integerPart + "." + remainder / (1000 * 1000 * 100) + "GB";
            } else {
                result = integerPart + "GB";
            }
        } else if (length > (1000 * 1000)) {
            long integerPart = length / 1000 / 1000;
            long remainder = length % (1000 * 1000);
            if (remainder > 0) {
                result = integerPart + "." + remainder / (1000 * 100) + "MB";
            } else {
                result = integerPart + "MB";
            }
        } else if (length > 1000) {
            long integerPart = length / 1000;
            long remainder = length % (1000);
            if (remainder > 0) {
                result = integerPart + "." + remainder / 100 + "KB";
            } else {
                result = integerPart + "KB";
            }
        } else {
            result = length + "B";
        }

        return result;
    }
}
