package io.g740.pigeon.biz.service.handler.admin;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import io.g740.commons.types.Handler;
import io.g740.components.uid.SingleInstanceUidGenerator;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Handler
public class SqlBuildStrategyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlBuildStrategyHandler.class);

    @Autowired
    private DsConnectionHandler dsConnectionHandler;

    @Autowired
    private DsRepository dsRepository;

    @Autowired
    private SingleInstanceUidGenerator singleInstanceUidGenerator;

    public SimpleTreeNode testBuildStrategy(SqlBuildStrategyContentDto buildStrategyContent) throws Exception {
        DsDo dsDo = this.dsRepository.findByUid(buildStrategyContent.getDsUid());
        if (dsDo == null) {
            throw new Exception("cannot find ds " + buildStrategyContent.getDsUid());
        }

        // Step 1, 测试查询SQL语句，同时返回SQL语句执行结果
        SimpleQueryResult queryResult = this.dsConnectionHandler.executeQuery(
                dsDo.getType(),
                dsDo.getConnectionProfileProps(),
                buildStrategyContent.getSqlStatement());

        // Step 2, 转换成树形结构，按照字典类目的结构组织
        return transformTable(queryResult.getColumnNames(), queryResult.getRows());
    }

    public SimpleTreeNode executeBuildStrategy(String buildStrategyContent) throws Exception {
        SqlBuildStrategyContentDto objectifiedDataProps = JSON.parseObject(buildStrategyContent,
                SqlBuildStrategyContentDto.class);
        if (objectifiedDataProps == null) {
            String errorMessage = "illegal build strategy content format, " + buildStrategyContent;
            throw new Exception(errorMessage);
        }

        DsDo dsDo = this.dsRepository.findByUid(objectifiedDataProps.getDsUid());
        if (dsDo == null) {
            throw new Exception("cannot find ds " + objectifiedDataProps.getDsUid());
        }

        SimpleQueryResult queryResult = this.dsConnectionHandler.executeQuery(
                dsDo.getType(),
                dsDo.getConnectionProfileProps(),
                objectifiedDataProps.getSqlStatement());

        return transformTable(queryResult.getColumnNames(), queryResult.getRows());
    }

    /**
     * 将表格（行和列）转换成树形结构，第N列即是第N层
     *
     * @param columnNames
     * @param rows
     * @return
     */
    private SimpleTreeNode transformTable(List<String> columnNames, List<Map<String, Object>> rows) {
        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();

        // 记录已经在树上登记了的列名和列值(统一转换成字符串)
        Map<String, Map<String, SimpleTreeNode>> mapOfExistingTreeNode = new HashMap<>(10);

        // 遍历每一行
        for (Map<String, Object> row : rows) {
            // 遍历该行每一列
            for (int columnIndex = 0; columnIndex < columnNames.size(); columnIndex++) {
                String columnName = columnNames.get(columnIndex);
                Object columnValue = row.get(columnName);
                String columnValueAsString = String.valueOf(columnValue);

                if (!mapOfExistingTreeNode.containsKey(columnName)) {
                    mapOfExistingTreeNode.put(columnName, new HashMap<>(10));
                }

                if (!mapOfExistingTreeNode.get(columnName).containsKey(columnValueAsString)) {
                    SimpleTreeNode treeNode = new SimpleTreeNode();
                    treeNode.setUidCode(this.singleInstanceUidGenerator.generateUid(
                            SqlBuildStrategyHandler.class.getName()));
                    treeNode.setName(columnValueAsString);

                    mapOfExistingTreeNode.get(columnName).put(columnValueAsString, treeNode);

                    if (columnIndex == 0) {
                        // 如果是第1列，将root tree node设为parent
                        rootTreeNode.getChildren().add(treeNode);
                    } else {
                        // 如果是第2列及以后，则自己[行m,列n]一定是[行m,列n-1]的child
                        String parentColumnName = columnNames.get(columnIndex - 1);
                        Object parentColumnValue = row.get(parentColumnName);
                        String parentColumnValueAsString = String.valueOf(parentColumnValue);
                        SimpleTreeNode parentTreeNode =
                                mapOfExistingTreeNode.get(parentColumnName).get(parentColumnValueAsString);
                        if (parentTreeNode == null) {
                            LOGGER.error("unexpected case, " +
                                    parentColumnName + ", " +
                                    parentColumnValueAsString);
                        } else {
                            if (CollectionUtils.isEmpty(parentTreeNode.getChildren())) {
                                parentTreeNode.setChildren(new ArrayList<>());
                            }
                            parentTreeNode.getChildren().add(treeNode);
                        }
                    }
                }
            }
        }

        return rootTreeNode;
    }
}
