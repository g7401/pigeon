package io.g740.pigeon.biz.service.handler.admin;

import com.google.common.base.Strings;
import io.g740.commons.exception.impl.AuthUnauthorizedException;
import io.g740.commons.exception.impl.ResourceDuplicateException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.df.CreateOrReplaceDfDto;
import io.g740.pigeon.biz.object.dto.df.DfDto;
import io.g740.pigeon.biz.object.dto.df.DfSimpleDto;
import io.g740.pigeon.biz.object.entity.df.DfDataObjectDo;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.object.entity.df.DfTagDo;
import io.g740.pigeon.biz.object.entity.ds.DsDataObjectDo;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.persistence.jpa.df.*;
import io.g740.pigeon.biz.persistence.jpa.ds.DsDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.pigeon.biz.share.constants.DataObjectDependencyTypeEnum;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class DfDefHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfDefHandler.class);

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private DsRepository dsRepository;

    @Autowired
    private DfRepository dfRepository;

    @Autowired
    private DfTagRepository dfTagRepository;

    @Autowired
    private DfDataObjectRepository dfDataObjectRepository;

    @Autowired
    private DfAvailableDataFieldRepository dfAvailableDataFieldRepository;

    @Autowired
    private DfConfDataFieldRepository dfConfDataFieldRepository;

    @Autowired
    private DfConfAdvancedRepository dfConfAdvancedRepository;

    @Autowired
    private DsDataObjectRepository dsDataObjectRepository;

    @Autowired
    private DsConnectionHandler dsConnectionHandler;

    @Autowired
    private DictionaryHandler dictionaryHandler;

    public DfDto createDf(CreateOrReplaceDfDto createDfDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源重复
        boolean existsDuplicate = this.dfRepository.existsByKey(createDfDto.getKey());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DfDo.RESOURCE_NAME + ":" + "key," + createDfDto.getKey());
        }
        existsDuplicate = this.dfRepository.existsByName(createDfDto.getName());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DfDo.RESOURCE_NAME + ":" + "name," + createDfDto.getName());
        }

        // Step 2, 检查所依存的data object存在
        boolean existsPrimaryDataObject = this.dsDataObjectRepository.existsByUid(createDfDto.getPrimaryDataObjectUid());
        if (!existsPrimaryDataObject) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + createDfDto.getPrimaryDataObjectUid());
        }

        if (CollectionUtils.isNotEmpty(createDfDto.getSecondaryDataObjectUids())) {
            for (Long secondaryDataObjectUid : createDfDto.getSecondaryDataObjectUids()) {
                boolean existsSecondaryDataObject = this.dsDataObjectRepository.existsByUid(secondaryDataObjectUid);
                if (!existsSecondaryDataObject) {
                    throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + secondaryDataObjectUid);
                }
            }
        }

        // Step 3, 创建资源
        DfDo dfDo = new DfDo();
        dfDo.setUid(this.idHelper.getNextId(DfDo.RESOURCE_NAME));
        dfDo.setKey(createDfDto.getKey());
        dfDo.setName(createDfDto.getName());
        dfDo.setDescription(createDfDto.getDescription());
        dfDo.setProcessingNeeded(createDfDto.getProcessingNeeded());
        dfDo.setProcessingLogic(createDfDto.getProcessingLogic());
        BaseDo.create(dfDo, operationUserInfo.getUsername(), new Date());
        this.dfRepository.save(dfDo);

        // Step 3.1, 创建df tag(s)
        if (CollectionUtils.isNotEmpty(createDfDto.getTags())) {
            List<DfTagDo> dfTagDoList = new ArrayList<>();
            for (String tag : createDfDto.getTags()) {
                DfTagDo dfTagDo = new DfTagDo();
                dfTagDo.setDfUid(dfDo.getUid());
                dfTagDo.setTag(tag);
                BaseDo.create(dfTagDo, operationUserInfo.getUsername(), new Date());
                dfTagDoList.add(dfTagDo);
            }
            this.dfTagRepository.saveAll(dfTagDoList);
        }

        // Step 3.2, 创建df primary data object
        DfDataObjectDo primaryDfDataObjectDo = new DfDataObjectDo();
        primaryDfDataObjectDo.setDataObjectDependencyType(DataObjectDependencyTypeEnum.PRIMARY);
        primaryDfDataObjectDo.setDataObjectUid(createDfDto.getPrimaryDataObjectUid());
        primaryDfDataObjectDo.setDfUid(dfDo.getUid());
        BaseDo.create(primaryDfDataObjectDo, operationUserInfo.getUsername(), new Date());
        this.dfDataObjectRepository.save(primaryDfDataObjectDo);

        // Step 3.3, 创建df secondary data object(s) if have
        if (CollectionUtils.isNotEmpty(createDfDto.getSecondaryDataObjectUids())) {
            List<DfDataObjectDo> secondaryDfDataObjectDoList = new ArrayList<>();

            for (Long item : createDfDto.getSecondaryDataObjectUids()) {
                DfDataObjectDo secondaryDfDataObjectDo = new DfDataObjectDo();
                secondaryDfDataObjectDo.setDataObjectDependencyType(DataObjectDependencyTypeEnum.SECONDARY);
                secondaryDfDataObjectDo.setDataObjectUid(item);
                secondaryDfDataObjectDo.setDfUid(dfDo.getUid());
                BaseDo.create(secondaryDfDataObjectDo, operationUserInfo.getUsername(), new Date());

                secondaryDfDataObjectDoList.add(secondaryDfDataObjectDo);
            }
            this.dfDataObjectRepository.saveAll(secondaryDfDataObjectDoList);
        }

        // Step 4, 构造返回对象
        DfDto dfDto = new DfDto();
        dfDto.setUidCode(dfDo.getUid());
        dfDto.setKey(dfDo.getKey());
        dfDto.setName(dfDo.getName());
        dfDto.setDescription(dfDo.getDescription());
        dfDto.setTags(createDfDto.getTags());
        dfDto.setProcessingNeeded(dfDo.getProcessingNeeded());
        dfDto.setProcessingLogic(dfDo.getProcessingLogic());
        dfDto.setPrimaryDataObjectUid(createDfDto.getPrimaryDataObjectUid());
        dfDto.setSecondaryDataObjectUids(createDfDto.getSecondaryDataObjectUids());

        return dfDto;
    }

    public DfDto replaceDf(CreateOrReplaceDfDto replaceDfDto, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        DfDo dfDo = this.dfRepository.findByUid(replaceDfDto.getUid());
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + replaceDfDto.getUid());
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dfDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 检查资源重复
        if (!Strings.isNullOrEmpty(replaceDfDto.getKey()) &&
                !replaceDfDto.getKey().equalsIgnoreCase(dfDo.getKey())) {
            boolean existsDuplicate = this.dfRepository.existsByKey(replaceDfDto.getKey());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DfDo.RESOURCE_NAME + ":" + "key, " + replaceDfDto.getKey());
            }
        }
        if (!Strings.isNullOrEmpty(replaceDfDto.getName()) &&
                !replaceDfDto.getName().equalsIgnoreCase(dfDo.getName())) {
            boolean existsDuplicate = this.dfRepository.existsByName(replaceDfDto.getName());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DfDo.RESOURCE_NAME + ":" + "name, " + replaceDfDto.getName());
            }
        }

        // Step 3, 检查所依存的data object存在
        boolean existsPrimaryDataObject = this.dsDataObjectRepository.existsByUid(replaceDfDto.getPrimaryDataObjectUid());
        if (!existsPrimaryDataObject) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + replaceDfDto.getPrimaryDataObjectUid());
        }

        if (CollectionUtils.isNotEmpty(replaceDfDto.getSecondaryDataObjectUids())) {
            for (Long secondaryDataObjectUid : replaceDfDto.getSecondaryDataObjectUids()) {
                boolean existsSecondaryDataObject = this.dsDataObjectRepository.existsByUid(secondaryDataObjectUid);
                if (!existsSecondaryDataObject) {
                    throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + secondaryDataObjectUid);
                }
            }
        }

        // Step 4, 替换资源
        dfDo.setKey(replaceDfDto.getKey());
        dfDo.setName(replaceDfDto.getName());
        dfDo.setDescription(replaceDfDto.getDescription());
        dfDo.setProcessingNeeded(replaceDfDto.getProcessingNeeded());
        dfDo.setProcessingLogic(replaceDfDto.getProcessingLogic());
        BaseDo.update(dfDo, operationUserInfo.getUsername(), new Date());
        this.dfRepository.save(dfDo);

        // Step 4.1, 替换df tag(s) if have
        this.dfTagRepository.deleteByDfUid(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(replaceDfDto.getTags())) {
            List<DfTagDo> dfTagDoList = new ArrayList<>();
            for (String tag : replaceDfDto.getTags()) {
                DfTagDo dfTagDo = new DfTagDo();
                dfTagDo.setDfUid(dfDo.getUid());
                dfTagDo.setTag(tag);
                BaseDo.create(dfTagDo, operationUserInfo.getUsername(), new Date());
                dfTagDoList.add(dfTagDo);
            }
            this.dfTagRepository.saveAll(dfTagDoList);
        }

        // Step 4.2, 替换df primary data object
        this.dfDataObjectRepository.deleteByDfUid(dfDo.getUid());
        DfDataObjectDo primaryDfDataObjectDo = new DfDataObjectDo();
        primaryDfDataObjectDo.setDataObjectDependencyType(DataObjectDependencyTypeEnum.PRIMARY);
        primaryDfDataObjectDo.setDataObjectUid(replaceDfDto.getPrimaryDataObjectUid());
        primaryDfDataObjectDo.setDfUid(dfDo.getUid());
        BaseDo.create(primaryDfDataObjectDo, operationUserInfo.getUsername(), new Date());
        this.dfDataObjectRepository.save(primaryDfDataObjectDo);

        // Step 4.3, 替换df secondary data object(s) if have
        if (CollectionUtils.isNotEmpty(replaceDfDto.getSecondaryDataObjectUids())) {
            List<DfDataObjectDo> secondaryDfDataObjectDoList = new ArrayList<>();

            for (Long secondaryDataObjectUid : replaceDfDto.getSecondaryDataObjectUids()) {
                DfDataObjectDo secondaryDfDataObjectDo = new DfDataObjectDo();
                secondaryDfDataObjectDo.setDataObjectDependencyType(DataObjectDependencyTypeEnum.SECONDARY);
                secondaryDfDataObjectDo.setDataObjectUid(secondaryDataObjectUid);
                secondaryDfDataObjectDo.setDfUid(dfDo.getUid());
                BaseDo.create(secondaryDfDataObjectDo, operationUserInfo.getUsername(), new Date());

                secondaryDfDataObjectDoList.add(secondaryDfDataObjectDo);
            }
            this.dfDataObjectRepository.saveAll(secondaryDfDataObjectDoList);
        }

        // Step 5, 构造返回对象
        DfDto dfDto = new DfDto();
        dfDto.setUidCode(dfDo.getUid());
        dfDto.setKey(dfDo.getKey());
        dfDto.setName(dfDo.getName());
        dfDto.setDescription(dfDo.getDescription());
        dfDto.setTags(replaceDfDto.getTags());
        dfDto.setProcessingNeeded(dfDo.getProcessingNeeded());
        dfDto.setProcessingLogic(dfDo.getProcessingLogic());
        dfDto.setPrimaryDataObjectUid(replaceDfDto.getPrimaryDataObjectUid());
        dfDto.setSecondaryDataObjectUids(replaceDfDto.getSecondaryDataObjectUids());

        return dfDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDf(Long uid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        DfDo dfDo = this.dfRepository.findByUid(uid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dfDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 删除关联资源
        this.dfConfAdvancedRepository.deleteByDfUid(uid);
        this.dfConfDataFieldRepository.deleteByDfUid(uid);
        this.dfAvailableDataFieldRepository.deleteByDfUid(uid);
        this.dfDataObjectRepository.deleteByDfUid(uid);
        this.dfTagRepository.deleteByDfUid(uid);

        // Step 3, 删除自己
        this.dfRepository.deleteByUid(uid);
    }

    public DfDto getDfByUid(Long uid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取资源
        DfDo dfDo = this.dfRepository.findByUid(uid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dfDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 一边构造返回对象，一边获取资源的扩展属性
        DfDto dfDto = new DfDto();
        dfDto.setUidCode(dfDo.getUid());
        dfDto.setKey(dfDo.getKey());
        dfDto.setName(dfDo.getName());
        dfDto.setDescription(dfDo.getDescription());
        dfDto.setProcessingNeeded(dfDo.getProcessingNeeded());
        dfDto.setProcessingLogic(dfDo.getProcessingLogic());

        List<DfTagDo> dfTagDoList = this.dfTagRepository.findByDfUid(uid);
        if (CollectionUtils.isNotEmpty(dfTagDoList)) {
            List<String> tags = new ArrayList<>();
            for (DfTagDo item : dfTagDoList) {
                tags.add(item.getTag());
            }
            dfDto.setTags(tags);
        }

        List<DfDataObjectDo> dfDataObjectDoList = this.dfDataObjectRepository.findByDfUid(uid);
        if (CollectionUtils.isNotEmpty(dfDataObjectDoList)) {
            for (DfDataObjectDo item : dfDataObjectDoList) {
                switch (item.getDataObjectDependencyType()) {
                    case PRIMARY: {
                        dfDto.setPrimaryDataObjectUid(item.getDataObjectUid());
                    }
                    break;
                    case SECONDARY: {
                        if (dfDto.getSecondaryDataObjectUids() == null) {
                            dfDto.setSecondaryDataObjectUids(new ArrayList<>());
                        }
                        dfDto.getSecondaryDataObjectUids().add(item.getDataObjectUid());
                    }
                    break;
                }
            }
        }

        return dfDto;
    }

    public DfSimpleDto getDfByKey(String key, UserInfo operationUserInfo) throws ServiceException {
        DfDo dfDo = this.dfRepository.findByKey(key);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + "key:" + key);
        }

        DfSimpleDto dfSimpleDto = new DfSimpleDto();
        dfSimpleDto.setDfUid(dfDo.getUid());
        dfSimpleDto.setDfKey(dfDo.getKey());
        dfSimpleDto.setDfName(dfDo.getName());
        dfSimpleDto.setDfDescription(dfDo.getDescription());
        dfSimpleDto.setCreateTimestamp(dfDo.getCreateTimestamp());
        dfSimpleDto.setCreateBy(dfDo.getCreateBy());

        List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByDfUid(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
            dfSimpleDto.setTags(new ArrayList<>());
            listOfDfTagDo.forEach(dfTagDo -> {
                dfSimpleDto.getTags().add(dfTagDo.getTag());
            });
        }

        return dfSimpleDto;
    }

    /**
     * 列出所有Data Objects w/ or w/o Data Facets
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public SimpleTreeNode listAllDataObjectsWithOrWithoutDf(
            UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();
        Map<String, SimpleTreeNode> mapOfAllTreeNodes = new HashedMap(10);

        // Step 1, 收集数据

        // 收集ds
        Map<Long, DsDo> mapOfDsDo = new HashMap<>();
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            this.dsRepository.findAll().forEach(dsDo -> {
                mapOfDsDo.put(dsDo.getUid(), dsDo);
            });
        } else {
            this.dsRepository.findByCreateBy(operationUserInfo.getUsername()).forEach(dsDo -> {
                mapOfDsDo.put(dsDo.getUid(), dsDo);
            });
        }

        // 收集df
        List<Long> listOfDfUid = new ArrayList<>();
        Map<Long, DfDo> mapOfDfDo = new HashMap<>();
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            this.dfRepository.findAll().forEach(dfDo -> {
                mapOfDfDo.put(dfDo.getUid(), dfDo);
                listOfDfUid.add(dfDo.getUid());
            });
        } else {
            this.dfRepository.findByCreateBy(operationUserInfo.getUsername()).forEach(dfDo -> {
                mapOfDfDo.put(dfDo.getUid(), dfDo);
                listOfDfUid.add(dfDo.getUid());
            });
        }

        // 收集所有DF的primary data object uid
        Map<Long, List<Long>> mapOfDataObjectUidAndDfUids = new HashMap<>(10);
        List<DfDataObjectDo> listOfDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidInAndDataObjectDependencyType(listOfDfUid, DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isNotEmpty(listOfDfDataObjectDo)) {
            for (DfDataObjectDo dfDataObjectDo : listOfDfDataObjectDo) {
                if (!mapOfDataObjectUidAndDfUids.containsKey(dfDataObjectDo.getDataObjectUid())) {
                    mapOfDataObjectUidAndDfUids.put(dfDataObjectDo.getDataObjectUid(), new ArrayList<>());
                }
                mapOfDataObjectUidAndDfUids.get(dfDataObjectDo.getDataObjectUid()).add(dfDataObjectDo.getDfUid());
            }
        }

        // Step 3, 列出所有ds的所有data object， 并且登记data object uid
        Iterable<DsDataObjectDo> dataObjectDoIterable = this.dsDataObjectRepository.findAll();
        if (dataObjectDoIterable == null) {
            return rootTreeNode;
        }
        dataObjectDoIterable.forEach(dataObjectDo -> {
            DsDo dsDo = mapOfDsDo.get(dataObjectDo.getDsUid());
            if (dsDo != null) {
                if (!Strings.isNullOrEmpty(dataObjectDo.getSchemaName())) {
                    buildDataObjectListWithSchema(rootTreeNode, mapOfAllTreeNodes,
                            mapOfDfDo, mapOfDataObjectUidAndDfUids, dsDo, dataObjectDo);
                } else {
                    buildDataObjectListWithoutSchema(rootTreeNode, mapOfAllTreeNodes,
                            mapOfDfDo, mapOfDataObjectUidAndDfUids, dsDo, dataObjectDo);
                }
            }
        });

        return rootTreeNode;
    }

    private void buildDataObjectListWithSchema(SimpleTreeNode rootTreeNode,
                                               Map<String, SimpleTreeNode> mapOfAllTreeNodes,
                                               Map<Long, DfDo> mapOfDfDo,
                                               Map<Long, List<Long>> mapOfDataObjectUidAndDfUids,
                                               DsDo dsDo,
                                               DsDataObjectDo dsDataObjectDo) {
        // 先看 ds
        String dsName = dsDo.getName();
        String dsKey = dsName;
        SimpleTreeNode dsTreeNode = mapOfAllTreeNodes.get(dsKey);
        if (dsTreeNode == null) {
            dsTreeNode = new SimpleTreeNode();
            dsTreeNode.setUidCode(dsDo.getUid());
            dsTreeNode.setName(dsName);
            dsTreeNode.setLevel(1);
            dsTreeNode.setType("server");
            dsTreeNode.setChildren(new ArrayList<>());
            rootTreeNode.getChildren().add(dsTreeNode);
            mapOfAllTreeNodes.put(dsKey, dsTreeNode);
        }

        // 再看 db
        String dbName = dsDataObjectDo.getDbName();
        String dbKey = dsName + "*#*##*" + dbName;
        SimpleTreeNode dbTreeNode = mapOfAllTreeNodes.get(dbKey);
        if (dbTreeNode == null) {
            dbTreeNode = new SimpleTreeNode();
            dbTreeNode.setName(dbName);
            dbTreeNode.setLevel(2);
            dbTreeNode.setType("database");
            dbTreeNode.setChildren(new ArrayList<>());
            dsTreeNode.getChildren().add(dbTreeNode);
            mapOfAllTreeNodes.put(dbKey, dbTreeNode);
        }

        // 再看 schema
        String schemaName = dsDataObjectDo.getSchemaName();
        String schemaKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName;
        SimpleTreeNode schemaTreeNode = mapOfAllTreeNodes.get(schemaKey);
        if (schemaTreeNode == null) {
            schemaTreeNode = new SimpleTreeNode();
            schemaTreeNode.setName(schemaName);
            schemaTreeNode.setLevel(3);
            schemaTreeNode.setType("schema");
            schemaTreeNode.setChildren(new ArrayList<>());
            dbTreeNode.getChildren().add(schemaTreeNode);
            mapOfAllTreeNodes.put(schemaKey, schemaTreeNode);
        }

        // 再看 table or view
        String dataObjectName = dsDataObjectDo.getDataObjectName();
        String dataObjectKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName + "*#*##*" + dataObjectName;
        SimpleTreeNode dataObjectTreeNode = mapOfAllTreeNodes.get(dataObjectKey);
        if (dataObjectTreeNode == null) {
            dataObjectTreeNode = new SimpleTreeNode();
            dataObjectTreeNode.setUidCode(dsDataObjectDo.getUid());
            dataObjectTreeNode.setName(dataObjectName);
            dataObjectTreeNode.setLevel(4);

            switch (dsDataObjectDo.getDataObjectType()) {
                case TABLE:
                    dataObjectTreeNode.setType("table");
                    break;
                case VIEW:
                    dataObjectTreeNode.setType("view");
                    break;
            }

            dataObjectTreeNode.setChildren(new ArrayList<>());
            schemaTreeNode.getChildren().add(dataObjectTreeNode);
            mapOfAllTreeNodes.put(dataObjectKey, dataObjectTreeNode);
        }

        // 再看 data facet
        if (mapOfDataObjectUidAndDfUids.containsKey(dsDataObjectDo.getUid())) {
            for (Long dfUid : mapOfDataObjectUidAndDfUids.get(dsDataObjectDo.getUid())) {
                if (mapOfDfDo.containsKey(dfUid)) {
                    String dfName = mapOfDfDo.get(dfUid).getName();
                    String dfKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName + "*#*##*" + dataObjectName +
                            "*#*##*" + dfName;
                    SimpleTreeNode dfTreeNode = mapOfAllTreeNodes.get(dfKey);
                    if (dfTreeNode == null) {
                        dfTreeNode = new SimpleTreeNode();
                        dfTreeNode.setUidCode(dfUid);
                        dfTreeNode.setName(dfName);
                        dfTreeNode.setLevel(5);
                        dfTreeNode.setType("df");
                        dataObjectTreeNode.getChildren().add(dfTreeNode);
                        mapOfAllTreeNodes.put(dfKey, dfTreeNode);
                    }
                }
            }
        }
    }

    private void buildDataObjectListWithoutSchema(SimpleTreeNode rootTreeNode,
                                                  Map<String, SimpleTreeNode> mapOfAllTreeNodes,
                                                  Map<Long, DfDo> mapOfDfDo,
                                                  Map<Long, List<Long>> mapOfDataObjectUidAndDfUids,
                                                  DsDo dsDo,
                                                  DsDataObjectDo dsDataObjectDo) {
        // 先看 ds
        String dsName = dsDo.getName();
        String dsKey = dsName;
        SimpleTreeNode dsTreeNode = mapOfAllTreeNodes.get(dsKey);
        if (dsTreeNode == null) {
            dsTreeNode = new SimpleTreeNode();
            dsTreeNode.setUidCode(dsDo.getUid());
            dsTreeNode.setName(dsName);
            dsTreeNode.setLevel(1);
            dsTreeNode.setType("server");
            dsTreeNode.setChildren(new ArrayList<>());
            rootTreeNode.getChildren().add(dsTreeNode);
            mapOfAllTreeNodes.put(dsKey, dsTreeNode);
        }

        // 再看 db
        String dbName = dsDataObjectDo.getDbName();
        String dbKey = dsName + "*#*##*" + dbName;
        SimpleTreeNode dbTreeNode = mapOfAllTreeNodes.get(dbKey);
        if (dbTreeNode == null) {
            dbTreeNode = new SimpleTreeNode();
            dbTreeNode.setName(dbName);
            dbTreeNode.setLevel(2);
            dbTreeNode.setType("database");
            dbTreeNode.setChildren(new ArrayList<>());
            dsTreeNode.getChildren().add(dbTreeNode);
            mapOfAllTreeNodes.put(dbKey, dbTreeNode);
        }

        // 再看 table or view
        String dataObjectName = dsDataObjectDo.getDataObjectName();
        String dataObjectKey = dsName + "*#*##*" + dbName + "*#*##*" + dataObjectName;
        SimpleTreeNode dataObjectTreeNode = mapOfAllTreeNodes.get(dataObjectKey);
        if (dataObjectTreeNode == null) {
            dataObjectTreeNode = new SimpleTreeNode();
            dataObjectTreeNode.setUidCode(dsDataObjectDo.getUid());
            dataObjectTreeNode.setName(dataObjectName);
            dataObjectTreeNode.setLevel(3);

            switch (dsDataObjectDo.getDataObjectType()) {
                case TABLE:
                    dataObjectTreeNode.setType("table");
                    break;
                case VIEW:
                    dataObjectTreeNode.setType("view");
                    break;
            }

            dataObjectTreeNode.setChildren(new ArrayList<>());
            dbTreeNode.getChildren().add(dataObjectTreeNode);
            mapOfAllTreeNodes.put(dataObjectKey, dataObjectTreeNode);
        }

        // 再看 data facet
        if (mapOfDataObjectUidAndDfUids.containsKey(dsDataObjectDo.getUid())) {
            for (Long dfUid : mapOfDataObjectUidAndDfUids.get(dsDataObjectDo.getUid())) {
                if (mapOfDfDo.containsKey(dfUid)) {
                    String dfName = mapOfDfDo.get(dfUid).getName();
                    String dfKey = dsName + "*#*##*" + dbName + "*#*##*" + dataObjectName +
                            "*#*##*" + dfName;
                    SimpleTreeNode dfTreeNode = mapOfAllTreeNodes.get(dfKey);
                    if (dfTreeNode == null) {
                        dfTreeNode = new SimpleTreeNode();
                        dfTreeNode.setUidCode(dfUid);
                        dfTreeNode.setName(dfName);
                        dfTreeNode.setLevel(4);
                        dfTreeNode.setType("df");
                        dataObjectTreeNode.getChildren().add(dfTreeNode);
                        mapOfAllTreeNodes.put(dfKey, dfTreeNode);
                    }
                }
            }
        }
    }

    /**
     * 列出所有Data Objects w/ Data Facets
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public SimpleTreeNode listAllDataObjectsWithDf(
            UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();

        // Step 1, 收集数据

        // 收集df
        List<Long> listOfDfUid = new ArrayList<>();
        Map<Long, DfDo> mapOfDfDo = new HashMap<>();
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            this.dfRepository.findAll().forEach(dfDo -> {
                mapOfDfDo.put(dfDo.getUid(), dfDo);
                listOfDfUid.add(dfDo.getUid());
            });
        } else {
            this.dfRepository.findByCreateBy(operationUserInfo.getUsername()).forEach(dfDo -> {
                mapOfDfDo.put(dfDo.getUid(), dfDo);
                listOfDfUid.add(dfDo.getUid());
            });
        }

        // 收集df data object uid
        List<DfDataObjectDo> dfDataObjectDoList =
                this.dfDataObjectRepository.findByDfUidInAndDataObjectDependencyType(listOfDfUid, DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(dfDataObjectDoList)) {
            return rootTreeNode;
        }

        // 收集所有data object uid，以便一次性从DB获取这些data object的信息
        List<Long> dataObjectUidList = new ArrayList<>();
        for (DfDataObjectDo dfDataObjectDo : dfDataObjectDoList) {
            dataObjectUidList.add(dfDataObjectDo.getDataObjectUid());
        }
        List<DsDataObjectDo> dsDataObjectDoList = this.dsDataObjectRepository.findByUidIn(dataObjectUidList);
        // data object list to map, key - data object uid
        // 同时，收集所有data source uid，以便一次性从DB获取这些data source的信息
        List<Long> dsUidList = new ArrayList<>();
        Map<Long, DsDataObjectDo> mapOfDataObjectDo = new HashMap<>();
        dsDataObjectDoList.forEach(dataObjectDo -> {
            mapOfDataObjectDo.put(dataObjectDo.getUid(), dataObjectDo);

            if (!dsUidList.contains(dataObjectDo.getDsUid())) {
                dsUidList.add(dataObjectDo.getDsUid());
            }
        });
        List<DsDo> dsDoList = this.dsRepository.findByUidIn(dsUidList);
        // ds list to map, key - ds uid
        Map<Long, DsDo> mapOfDsDo = new HashMap<>();
        dsDoList.forEach(dsDo -> {
            mapOfDsDo.put(dsDo.getUid(), dsDo);
        });

        // Step 2, 遍历df data object，构造tree
        Map<String, SimpleTreeNode> mapOfAllTreeNodes = new HashedMap();

        for (DfDataObjectDo dfDataObjectDo : dfDataObjectDoList) {
            Long dataObjectUid = dfDataObjectDo.getDataObjectUid();
            DsDataObjectDo dsDataObjectDo = mapOfDataObjectDo.get(dataObjectUid);
            DsDo dsDo = mapOfDsDo.get(dsDataObjectDo.getDsUid());
            if (dsDo == null) {
                LOGGER.warn("data mismatch: " + "data object " +
                        dsDataObjectDo.getUid() + " match to an unknown ds " + dsDataObjectDo.getDsUid());
                continue;
            }

            // 先看 ds
            String dsName = dsDo.getName();
            String dsKey = dsName;
            SimpleTreeNode dsTreeNode = mapOfAllTreeNodes.get(dsKey);
            if (dsTreeNode == null) {
                dsTreeNode = new SimpleTreeNode();
                dsTreeNode.setUidCode(dsDo.getUid());
                dsTreeNode.setName(dsName);
                dsTreeNode.setLevel(1);
                dsTreeNode.setType("server");
                dsTreeNode.setChildren(new ArrayList<>());
                rootTreeNode.getChildren().add(dsTreeNode);
                mapOfAllTreeNodes.put(dsKey, dsTreeNode);
            }

            // 再看 db
            String dbName = dsDataObjectDo.getDbName();
            String dbKey = dsName + "*#*##*" + dbName;
            SimpleTreeNode dbTreeNode = mapOfAllTreeNodes.get(dbKey);
            if (dbTreeNode == null) {
                dbTreeNode = new SimpleTreeNode();
                dbTreeNode.setName(dsName);
                dbTreeNode.setLevel(2);
                dbTreeNode.setType("database");
                dbTreeNode.setChildren(new ArrayList<>());
                dsTreeNode.getChildren().add(dbTreeNode);
                mapOfAllTreeNodes.put(dbKey, dbTreeNode);
            }

            boolean schemaRequired = !Strings.isNullOrEmpty(dsDataObjectDo.getSchemaName());
            if (schemaRequired) {
                // 再看 schema
                String schemaName = dsDataObjectDo.getSchemaName();
                String schemaKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName;
                SimpleTreeNode schemaTreeNode = mapOfAllTreeNodes.get(schemaKey);
                if (schemaTreeNode == null) {
                    schemaTreeNode = new SimpleTreeNode();
                    schemaTreeNode.setName(schemaName);
                    schemaTreeNode.setLevel(3);
                    schemaTreeNode.setType("schema");
                    schemaTreeNode.setChildren(new ArrayList<>());
                    dsTreeNode.getChildren().add(schemaTreeNode);
                    mapOfAllTreeNodes.put(schemaKey, schemaTreeNode);
                }

                // 再看 table or view
                String dataObjectName = dsDataObjectDo.getDataObjectName();
                String dataObjectKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName + "*#*##*" + dataObjectName;
                SimpleTreeNode dataObjectTreeNode = mapOfAllTreeNodes.get(dataObjectKey);
                if (dataObjectTreeNode == null) {
                    dataObjectTreeNode = new SimpleTreeNode();
                    dataObjectTreeNode.setUidCode(dsDataObjectDo.getUid());
                    dataObjectTreeNode.setName(dataObjectName);
                    dataObjectTreeNode.setLevel(4);

                    switch (dsDataObjectDo.getDataObjectType()) {
                        case TABLE:
                            dataObjectTreeNode.setType("table");
                            break;
                        case VIEW:
                            dataObjectTreeNode.setType("view");
                            break;
                    }

                    dataObjectTreeNode.setChildren(new ArrayList<>());
                    schemaTreeNode.getChildren().add(dataObjectTreeNode);
                    mapOfAllTreeNodes.put(dataObjectKey, dataObjectTreeNode);
                }

                // 再看 data facet
                Long dfUid = dfDataObjectDo.getDfUid();
                String dfName = mapOfDfDo.get(dfUid).getName();
                String dfKey = dsName + "*#*##*" + dbName + "*#*##*" + schemaName + "*#*##*" + dataObjectName +
                        "*#*##*" + dfName;
                SimpleTreeNode dfTreeNode = mapOfAllTreeNodes.get(dfKey);
                if (dfTreeNode == null) {
                    dfTreeNode = new SimpleTreeNode();
                    dfTreeNode.setUidCode(dfUid);
                    dfTreeNode.setName(dfName);
                    dfTreeNode.setLevel(5);
                    dfTreeNode.setType("df");
                    dfTreeNode.setChildren(new ArrayList<>());
                    dataObjectTreeNode.getChildren().add(dfTreeNode);
                    mapOfAllTreeNodes.put(dfKey, dfTreeNode);
                }
            } else {
                // 再看 table or view
                String dataObjectName = dsDataObjectDo.getDataObjectName();
                String dataObjectKey = dsName + "*#*##*" + dbName + "*#*##*" + dataObjectName;
                SimpleTreeNode dataObjectTreeNode = mapOfAllTreeNodes.get(dataObjectKey);
                if (dataObjectTreeNode == null) {
                    dataObjectTreeNode = new SimpleTreeNode();
                    dataObjectTreeNode.setUidCode(dsDataObjectDo.getUid());
                    dataObjectTreeNode.setName(dataObjectName);
                    dataObjectTreeNode.setLevel(3);

                    switch (dsDataObjectDo.getDataObjectType()) {
                        case TABLE:
                            dataObjectTreeNode.setType("table");
                            break;
                        case VIEW:
                            dataObjectTreeNode.setType("view");
                            break;
                    }

                    dataObjectTreeNode.setChildren(new ArrayList<>());
                    dbTreeNode.getChildren().add(dataObjectTreeNode);
                    mapOfAllTreeNodes.put(dataObjectKey, dataObjectTreeNode);
                }

                // 再看 data facet
                Long dfUid = dfDataObjectDo.getDfUid();
                String dfName = mapOfDfDo.get(dfUid).getName();
                String dfKey = dsName + "*#*##*" + dbName + "*#*##*" + dataObjectName + "*#*##*" + dfName;
                SimpleTreeNode dfTreeNode = mapOfAllTreeNodes.get(dfKey);
                if (dfTreeNode == null) {
                    dfTreeNode = new SimpleTreeNode();
                    dfTreeNode.setUidCode(dfUid);
                    dfTreeNode.setName(dfName);
                    dfTreeNode.setLevel(4);
                    dfTreeNode.setType("df");
                    dataObjectTreeNode.getChildren().add(dfTreeNode);
                    mapOfAllTreeNodes.put(dfKey, dfTreeNode);
                }
            }
        }

        return rootTreeNode;
    }

    public List<DfSimpleDto> listDfByCreatedBy(String username, UserInfo operationUserInfo) throws ServiceException {
        List<DfDo> listOfDfDo = this.dfRepository.findByCreateBy(username);
        if (CollectionUtils.isNotEmpty(listOfDfDo)) {
            List<Long> listOfDfUid = new ArrayList<>();
            List<DfSimpleDto> listOfDfSimpleDto = new ArrayList<>();
            for (DfDo dfDo : listOfDfDo) {
                DfSimpleDto dfSimpleDto = new DfSimpleDto();
                dfSimpleDto.setDfUid(dfDo.getUid());
                dfSimpleDto.setDfKey(dfDo.getKey());
                dfSimpleDto.setDfName(dfDo.getName());
                dfSimpleDto.setDfDescription(dfDo.getDescription());
                dfSimpleDto.setCreateTimestamp(dfDo.getCreateTimestamp());
                listOfDfSimpleDto.add(dfSimpleDto);

                listOfDfUid.add(dfDo.getUid());
            }

            Map<Long, List<String>> mapOfDfAndTags = new HashMap<>();
            List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByDfUidIn(listOfDfUid);
            if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
                for (DfTagDo dfTagDo : listOfDfTagDo) {
                    if (!mapOfDfAndTags.containsKey(dfTagDo.getDfUid())) {
                        mapOfDfAndTags.put(dfTagDo.getDfUid(), new ArrayList<>());
                    }
                    mapOfDfAndTags.get(dfTagDo.getDfUid()).add(dfTagDo.getTag());
                }
            }

            for (DfSimpleDto dfSimpleDto : listOfDfSimpleDto) {
                List<String> tags = mapOfDfAndTags.get(dfSimpleDto.getDfUid());
                dfSimpleDto.setTags(tags);
            }

            return listOfDfSimpleDto;
        }

        return null;
    }

    public List<DfSimpleDto> listAllDf(UserInfo operationUserInfo) throws ServiceException {
        List<Long> listOfDfUid = new ArrayList<>();
        List<DfSimpleDto> listOfDfSimpleDto = new ArrayList<>();
        this.dfRepository.findAll().forEach(dfDo -> {
            DfSimpleDto dfSimpleDto = new DfSimpleDto();
            dfSimpleDto.setDfUid(dfDo.getUid());
            dfSimpleDto.setDfKey(dfDo.getKey());
            dfSimpleDto.setDfName(dfDo.getName());
            dfSimpleDto.setDfDescription(dfDo.getDescription());
            dfSimpleDto.setCreateTimestamp(dfDo.getCreateTimestamp());
            listOfDfSimpleDto.add(dfSimpleDto);

            listOfDfUid.add(dfDo.getUid());
        });

        Map<Long, List<String>> mapOfDfAndTags = new HashMap<>();
        List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByDfUidIn(listOfDfUid);
        if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
            for (DfTagDo dfTagDo : listOfDfTagDo) {
                if (!mapOfDfAndTags.containsKey(dfTagDo.getDfUid())) {
                    mapOfDfAndTags.put(dfTagDo.getDfUid(), new ArrayList<>());
                }
                mapOfDfAndTags.get(dfTagDo.getDfUid()).add(dfTagDo.getTag());
            }
        }

        for (DfSimpleDto dfSimpleDto : listOfDfSimpleDto) {
            List<String> tags = mapOfDfAndTags.get(dfSimpleDto.getDfUid());
            dfSimpleDto.setTags(tags);
        }

        return listOfDfSimpleDto;
    }

    public List<DfSimpleDto> listDf(List<Long> listOfDfUid) throws ServiceException {
        List<DfDo> listOfDfDo = this.dfRepository.findByUidIn(listOfDfUid);
        if (CollectionUtils.isNotEmpty(listOfDfDo)) {
            List<DfSimpleDto> listOfDfSimpleDto = new ArrayList<>();
            for (DfDo dfDo : listOfDfDo) {
                DfSimpleDto dfSimpleDto = new DfSimpleDto();
                dfSimpleDto.setDfUid(dfDo.getUid());
                dfSimpleDto.setDfKey(dfDo.getKey());
                dfSimpleDto.setDfName(dfDo.getName());
                dfSimpleDto.setDfDescription(dfDo.getDescription());
                dfSimpleDto.setCreateTimestamp(dfDo.getCreateTimestamp());
                listOfDfSimpleDto.add(dfSimpleDto);
            }

            Map<Long, List<String>> mapOfDfAndTags = new HashMap<>();
            List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByDfUidIn(listOfDfUid);
            if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
                for (DfTagDo dfTagDo : listOfDfTagDo) {
                    if (!mapOfDfAndTags.containsKey(dfTagDo.getDfUid())) {
                        mapOfDfAndTags.put(dfTagDo.getDfUid(), new ArrayList<>());
                    }
                    mapOfDfAndTags.get(dfTagDo.getDfUid()).add(dfTagDo.getTag());
                }
            }

            for (DfSimpleDto dfSimpleDto : listOfDfSimpleDto) {
                List<String> tags = mapOfDfAndTags.get(dfSimpleDto.getDfUid());
                dfSimpleDto.setTags(tags);
            }

            return listOfDfSimpleDto;
        }

        return null;
    }

}
