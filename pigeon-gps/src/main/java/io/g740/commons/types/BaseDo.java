package io.g740.commons.types;

import javax.persistence.*;
import java.util.Date;

/**
 * @author bbottong
 */
@MappedSuperclass
public abstract class BaseDo {
    public static final String CREATE_TIMESTAMP_FIELD_NAME = "createTimestamp";
    public static final String CREATE_TIMESTAMP = "create_timestamp";
    public static final String CREATE_BY = "create_by";
    public static final String LAST_UPDATE_BY = "last_update_by";
    public static final String LAST_UPDATE_TIMESTAMP = "last_update_timestamp";
    public static final String LAST_UPDATE_TIMESTAMP_FIELD_NAME = "lastUpdateTimestamp";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private Boolean deleted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_timestamp")
    private Date createTimestamp;

    @Column(name = "create_by", length = 45)
    private String createBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_timestamp")
    private Date lastUpdateTimestamp;

    @Column(name = "last_update_by", length = 45)
    private String lastUpdateBy;

    @Column(name = "owner", length = 45)
    private String owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Date createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(Date lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    public String getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setLastUpdateBy(String lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public static void create(BaseDo object, String username, Date timestamp) {
        object.setCreateBy(username);
        object.setCreateTimestamp(timestamp);
        object.setLastUpdateTimestamp(timestamp);
        object.setLastUpdateBy(username);
        object.setDeleted(Boolean.FALSE);
        object.setOwner(username);
    }

    public static void update(BaseDo object, String username, Date timestamp) {
        object.setLastUpdateBy(username);
        object.setLastUpdateTimestamp(timestamp);
    }

    public static void delete(BaseDo object, String username, Date timestamp) {
        object.setDeleted(Boolean.TRUE);
        object.setLastUpdateBy(username);
        object.setLastUpdateTimestamp(timestamp);
    }
}
