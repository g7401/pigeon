package io.g740.pigeon.biz.persistence.jdbc.impl;

import com.google.common.base.Strings;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.commons.utils.MysqlUtils;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.dto.export.AsyncExportJobDto;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.object.entity.df.DfTagDo;
import io.g740.pigeon.biz.object.entity.export.AsyncExportJobDo;
import io.g740.pigeon.biz.persistence.jdbc.NativeRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author bbottong
 */
@Repository
public class NativeRepositoryMysqlImpl implements NativeRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeRepositoryMysqlImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询异步导出工作
     *
     * @param parameterMap
     * @param pageable
     * @param operationUserInfo
     * @return
     */
    @Override
    public PageResult<AsyncExportJobDto> queryAsyncExportJob(
            Map<String, String[]> parameterMap, Pageable pageable, UserInfo operationUserInfo) throws Exception {
        // Step 1, 收集表名，列名
        String tableName;
        Table table = AsyncExportJobDo.class.getAnnotation(Table.class);
        if (table != null) {
            tableName = table.name();
        } else {
            throw new Exception("failed to retrieve table name from " + AsyncExportJobDo.class);
        }

        Map<String, Object> availableColumns = new HashMap<>();
        Field[] fields = AsyncExportJobDo.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!Strings.isNullOrEmpty(column.name())) {
                    availableColumns.put(column.name(), field.getType());
                } else {
                    availableColumns.put(field.getName(), field.getType());
                }
            } else {
                availableColumns.put(field.getName(), field.getType());
            }
        }

        // Step 2,
        if (!parameterMap.containsKey(BaseDo.CREATE_BY)) {
            parameterMap.put(BaseDo.CREATE_BY, new String[]{operationUserInfo.getUsername()});
        }

        // Step 2, count
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) as cnt FROM " + tableName);


        // Step 2, query

        return null;
    }


    @Override
    public PageResult<DfSimpleDto> queryDfByTagsAndDfNameWithinLimitRange(
            List<String> tags,
            String name,
            List<Long> listOfAvailableDfUid,
            String createBy,
            Pageable pageable) throws Exception {
        // Step 1, 收集表名，列名
        String dfTableName;
        Table dfTable = DfDo.class.getAnnotation(Table.class);
        if (dfTable != null) {
            dfTableName = dfTable.name();
        } else {
            throw new Exception("failed to retrieve table name from " + DfDo.class);
        }

        String dfTagTableName;
        Table dfTagTable = DfTagDo.class.getAnnotation(Table.class);
        if (dfTagTable != null) {
            dfTagTableName = dfTagTable.name();
        } else {
            throw new Exception("failed to retrieve table name from " + DfTagDo.class);
        }

        // Step 2, 构造SQL
        StringBuilder embeddedQuerySql = new StringBuilder();
        embeddedQuerySql.append("SELECT")
                .append(" " + "tc.uid, tc.kee as `key`, tc.name, tc.description, tc.create_timestamp, tc.create_by")
                .append(" " + "FROM")
                .append(" " + "(SELECT")
                .append(" " + "ta.*, tb.tag")
                .append(" " + "FROM")
                .append(" " + "`" + dfTableName + "`" + " ta")
                .append(" " + "LEFT JOIN").append(" " + "`" + dfTagTableName + "`" + " tb")
                .append(" " + "ON ta.uid = tb.df_uid AND ta.is_deleted = 0 AND tb.is_deleted = 0");

        if (CollectionUtils.isNotEmpty(listOfAvailableDfUid) || !Strings.isNullOrEmpty(createBy)) {
            embeddedQuerySql.append(" " + "AND (");

            StringBuilder specificConditionClause = new StringBuilder();

            if (CollectionUtils.isNotEmpty(listOfAvailableDfUid)) {
                specificConditionClause.append(" " + "ta.uid IN (");
                for (Long availableDfUid : listOfAvailableDfUid) {
                    specificConditionClause.append(availableDfUid + ",");
                }
                specificConditionClause.deleteCharAt(specificConditionClause.length() - 1);
                specificConditionClause.append(" " + ")");
            }

            if (!Strings.isNullOrEmpty(createBy)) {
                if (specificConditionClause.length() > 0) {
                    specificConditionClause.append(" " + "OR ta.create_by=" + "'" + createBy + "'");
                } else {
                    specificConditionClause.append(" " + "ta.create_by=" + "'" + createBy + "'");
                }
            }

            embeddedQuerySql.append(specificConditionClause);

            embeddedQuerySql.append(")");
        }

        embeddedQuerySql.append(" " + "ORDER BY ta.id ASC) tc ");
        embeddedQuerySql.append(" " + "WHERE 1=1");
        if (CollectionUtils.isNotEmpty(tags)) {
            embeddedQuerySql.append(" " + "AND tc.tag IN (");
            for (String tag : tags) {
                embeddedQuerySql.append("'" + tag + "'" + ",");
            }
            embeddedQuerySql.deleteCharAt(embeddedQuerySql.length() - 1);
            embeddedQuerySql.append(")");
        }
        if (!Strings.isNullOrEmpty(name)) {
            embeddedQuerySql.append(" " + "AND tc.name LIKE '%" + MysqlUtils.buildEscapedValue(name) + "%'");
        }
        embeddedQuerySql.append(" " + "GROUP BY tc.uid , tc.kee , tc.name , tc.description , tc.create_timestamp , tc.create_by");

        // Step 3, exec count sql
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) as cnt FROM (");
        countSql.append(embeddedQuerySql);
        countSql.append(") tz");

        Integer countResult = this.jdbcTemplate.queryForObject(countSql.toString(), Integer.class);
        if (countResult == 0) {
            PageResult<DfSimpleDto> pageResult = new PageResult<>();
            pageResult.setNumberOfElements(0);
            pageResult.setPageNumber(pageable.getPageNumber());
            pageResult.setPageSize(pageable.getPageSize());
            pageResult.setTotalElements(0L);
            pageResult.setTotalPages(0);

            return pageResult;
        }

        PageResult<DfSimpleDto> pageResult = new PageResult<>();
        pageResult.setPageNumber(pageable.getPageNumber());
        pageResult.setPageSize(pageable.getPageSize());
        pageResult.setTotalElements(countResult.longValue());
        if (countResult % pageable.getPageSize() == 0) {
            pageResult.setTotalPages(countResult.intValue() / pageable.getPageSize());
        } else {
            pageResult.setTotalPages(countResult.intValue() / pageable.getPageSize() + 1);
        }

        StringBuilder querySql = new StringBuilder();
        querySql.append("SELECT tz.* FROM (");
        querySql.append(embeddedQuerySql);
        querySql.append(") tz");

        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            StringBuilder orderByClause = new StringBuilder();
            pageable.getSort().forEach(order -> {
                orderByClause.append("`" + order.getProperty() + "`" + " " + order.getDirection().name() + ",");
            });
            if (orderByClause.length() > 0) {
                orderByClause.deleteCharAt(orderByClause.length() - 1);
                querySql.append(" " + "ORDER BY " + orderByClause);
            }
        }

        // Step 4, pagination clause
        querySql.append(" LIMIT " + pageable.getOffset() + "," + pageable.getPageSize());

        List<Map<String, Object>> queryResult = this.jdbcTemplate.queryForList(querySql.toString());
        if (CollectionUtils.isNotEmpty(queryResult)) {
            pageResult.setContent(new ArrayList<>());
            for (Map<String, Object> row : queryResult) {
                DfSimpleDto dfSimpleDto = new DfSimpleDto();
                dfSimpleDto.setDfUid((Long) row.get("uid"));
                dfSimpleDto.setDfKey((String) row.get("key"));
                dfSimpleDto.setDfName((String) row.get("name"));
                dfSimpleDto.setDfDescription((String) row.get("description"));
                dfSimpleDto.setCreateTimestamp((Date) row.get("create_timestamp"));
                dfSimpleDto.setCreateBy((String) row.get("create_by"));

                pageResult.getContent().add(dfSimpleDto);
            }
            pageResult.setNumberOfElements(queryResult.size());
        }

        return pageResult;
    }

    @Override
    public Long queryMaximumUid(String tableName) throws Exception {
        StringBuilder querySql = new StringBuilder();
        querySql.append("SELECT MAX(uid) FROM `" + tableName + "`");
        Long max = this.jdbcTemplate.queryForObject(querySql.toString(), Long.class);
        return max;
    }
}
