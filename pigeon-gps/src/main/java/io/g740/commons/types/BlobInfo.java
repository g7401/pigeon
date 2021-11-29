package io.g740.commons.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author : zxiuwu
 * @version : V1.0
 * @function :
 * @date : 2019/10/14 13:53
 * @description :
 */

public class BlobInfo {
    private String blobName;
    private String blobId;
    private String blobDir;

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    public String getBlobId() {
        return blobId;
    }

    public void setBlobId(String blobId) {
        this.blobId = blobId;
    }

    public String getBlobDir() {
        return blobDir;
    }

    public void setBlobDir(String blobDir) {
        this.blobDir = blobDir;
    }

    @Override
    public String toString() {
        return "BlobInfo{" +
                "blobName='" + blobName + '\'' +
                ", blobId='" + blobId + '\'' +
                ", blobDir='" + blobDir + '\'' +
                '}';
    }
}
