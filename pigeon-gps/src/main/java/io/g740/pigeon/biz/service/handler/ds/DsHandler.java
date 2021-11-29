package io.g740.pigeon.biz.service.handler.ds;

import com.google.common.base.Strings;
import io.g740.commons.exception.impl.*;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.ds.*;
import io.g740.pigeon.biz.object.entity.df.DfDataObjectDo;
import io.g740.pigeon.biz.object.entity.ds.DsDataObjectDo;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.persistence.jdbc.NativeRepository;
import io.g740.pigeon.biz.persistence.jpa.df.DfDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleQueryResult;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class DsHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DsHandler.class);

    @Autowired
    private DsRepository dsRepository;

    @Autowired
    private NativeRepository nativeRepository;

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private DsDataObjectRepository dsDataObjectRepository;

    @Autowired
    private DsConnectionHandler dsConnectionHandler;

    @Autowired
    private DfDataObjectRepository dfDataObjectRepository;

    @Transactional(rollbackFor = Exception.class)
    public DsDto createDs(CreateDsDto createDsDto,
                          UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源重复
        // Step 1.1, 按规则：name不能重复
        boolean existsDuplicate = this.dsRepository.existsByName(createDsDto.getName());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DsDo.RESOURCE_NAME + ":" + createDsDto.getName());
        }
        // Step 1.2, 按规则：连接信息不能重复
        existsDuplicate = this.dsRepository.existsByConnectionProfileProps(createDsDto.getConnectionProfileProps());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DsDo.RESOURCE_NAME + ":" + createDsDto.getConnectionProfileProps());
        }

        // Step 2, 创建资源
        DsDo dsDo = new DsDo();
        dsDo.setUid(this.idHelper.getNextId(DsDo.RESOURCE_NAME));
        dsDo.setName(createDsDto.getName());
        dsDo.setType(createDsDto.getType());
        dsDo.setConnectionProfileProps(createDsDto.getConnectionProfileProps());
        BaseDo.create(dsDo, operationUserInfo.getUsername(), new Date());
        this.dsRepository.save(dsDo);

        // Step 3, initial load data objects for this ds
        refreshDs(dsDo.getUid(), operationUserInfo);

        // Step 4, 构建返回对象
        DsDto dsDto = new DsDto();
        dsDto.setUid(dsDo.getUid());
        dsDto.setName(dsDo.getName());
        dsDto.setType(dsDo.getType());
        dsDto.setConnectionProfileProps(dsDo.getConnectionProfileProps());

        return dsDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DsDto updateDs(UpdateDsDto updateDsDto,
                          UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取资源
        DsDo dsDo = this.dsRepository.findByUid(updateDsDto.getUid());
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + updateDsDto.getUid());
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dsDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 检查资源重复
        if (!Strings.isNullOrEmpty(updateDsDto.getName()) &&
                !updateDsDto.getName().equalsIgnoreCase(dsDo.getName())) {
            boolean existsDuplicate = this.dsRepository.existsByName(updateDsDto.getName());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DsDo.RESOURCE_NAME + ":" +
                        "duplicate reason: " + updateDsDto.getName());
            }
        }
        if (!Strings.isNullOrEmpty(updateDsDto.getConnectionProfileProps()) &&
                !updateDsDto.getConnectionProfileProps().equalsIgnoreCase(dsDo.getConnectionProfileProps())) {
            boolean existsDuplicate = this.dsRepository.existsByConnectionProfileProps(updateDsDto.getConnectionProfileProps());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DsDo.RESOURCE_NAME + ":" + updateDsDto.getConnectionProfileProps());
            }
        }

        // Step 3, 更新资源
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDsDto.getName()) &&
                !updateDsDto.getName().equalsIgnoreCase(dsDo.getName())) {
            dsDo.setName(updateDsDto.getName());
            requiredToUpdate = true;
        }
        if (updateDsDto.getType() != null &&
                !updateDsDto.getType().equals(dsDo.getType())) {
            dsDo.setType(updateDsDto.getType());
            requiredToUpdate = true;
        }
        if (!Strings.isNullOrEmpty(updateDsDto.getConnectionProfileProps()) &&
                !updateDsDto.getConnectionProfileProps().equalsIgnoreCase(dsDo.getConnectionProfileProps())) {
            dsDo.setConnectionProfileProps(updateDsDto.getConnectionProfileProps());
            requiredToUpdate = true;

            // Step 3.1, refresh data objects for this ds
            refreshDs(dsDo.getUid(), operationUserInfo);
        }
        if (requiredToUpdate) {
            BaseDo.update(dsDo, operationUserInfo.getUsername(), new Date());
            this.dsRepository.save(dsDo);
        }

        // Step 4, 构建返回对象
        DsDto dsDto = new DsDto();
        dsDto.setUid(dsDo.getUid());
        dsDto.setName(dsDo.getName());
        dsDto.setType(dsDo.getType());
        dsDto.setConnectionProfileProps(dsDo.getConnectionProfileProps());

        return dsDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDs(Long uid,
                         UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 检查资源存在
        DsDo dsDo = this.dsRepository.findByUid(uid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dsDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        // Step 2, 检查资源是否正在使用
        boolean existsInUse = false;
        List<DsDataObjectDo> listOfDsDataObjectDo = this.dsDataObjectRepository.findByDsUid(uid);
        if (CollectionUtils.isNotEmpty(listOfDsDataObjectDo)) {
            List<Long> listOfDataObjectUid = new ArrayList<>();
            listOfDsDataObjectDo.forEach(dataObjectDo -> {
                listOfDataObjectUid.add(dataObjectDo.getUid());
            });
            existsInUse = this.dfDataObjectRepository.existsByDataObjectUidIn(listOfDataObjectUid);
        }
        if (existsInUse) {
            throw new ResourceInUseException(DfDataObjectDo.RESOURCE_NAME + ":" + DsDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 3, 删除关联资源
        this.dsDataObjectRepository.deleteByDsUid(uid);

        // Step 4, 删除自己
        this.dsRepository.deleteByUid(uid);
    }

    public DsDto getDs(Long uid, UserInfo operationUserInfo) throws ServiceException {
        DsDo dsDo = this.dsRepository.findByUid(uid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + uid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dsDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        DsDto dsDto = new DsDto();
        dsDto.setUid(dsDo.getUid());
        dsDto.setName(dsDo.getName());
        dsDto.setType(dsDo.getType());
        dsDto.setConnectionProfileProps(dsDo.getConnectionProfileProps());

        return dsDto;
    }

    public List<DsDto> listAllDs(UserInfo operationUserInfo) throws ServiceException {
        List<DsDto> listOfDsDto = new LinkedList<>();
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            this.dsRepository.findAll().forEach(dsDo -> {
                DsDto dsDto = new DsDto();
                dsDto.setUid(dsDo.getUid());
                dsDto.setName(dsDo.getName());
                dsDto.setType(dsDo.getType());
                dsDto.setConnectionProfileProps(dsDo.getConnectionProfileProps());

                listOfDsDto.add(dsDto);
            });
        } else {
            List<DsDo> listOfDsDo = this.dsRepository.findByCreateBy(operationUserInfo.getUsername());
            if (CollectionUtils.isNotEmpty(listOfDsDo)) {
                listOfDsDo.forEach(dsDo -> {
                    DsDto dsDto = new DsDto();
                    dsDto.setUid(dsDo.getUid());
                    dsDto.setName(dsDo.getName());
                    dsDto.setType(dsDo.getType());
                    dsDto.setConnectionProfileProps(dsDo.getConnectionProfileProps());

                    listOfDsDto.add(dsDto);
                });
            }
        }

        return listOfDsDto;
    }

    public Boolean testDsConnection(TestDsConnectionDto testDsConnectionDto,
                                    UserInfo operationUserInfo) throws ServiceException {
        return this.dsConnectionHandler.testDsConnection(testDsConnectionDto);
    }

    public SimpleQueryResult testQueryStatement(
            TestQueryStatementDto testQueryStatementDto, UserInfo operationUserInfo) throws ServiceException {
        DsDo dsDo = this.dsRepository.findByUid(testQueryStatementDto.getDsUid());
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + testQueryStatementDto.getDsUid());
        }

        try {
            return this.dsConnectionHandler.testQuery(dsDo.getType(), dsDo.getConnectionProfileProps(),
                    testQueryStatementDto.getQueryStatement(), 0, 10);
        } catch (Exception e) {
            throw new ServiceException("failed to execute query: " +
                    dsDo.getUid() + ", " + testQueryStatementDto.getQueryStatement(), e);
        }
    }

    /**
     * 列出指定ds的所有data objects
     *
     * @param dsUid
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public List<DsDataObjectDo> listAllDataObjects(Long dsUid, UserInfo operationUserInfo) throws ServiceException {
        DsDo dsDo = this.dsRepository.findByUid(dsUid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dsDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        return this.dsDataObjectRepository.findByDsUid(dsUid);
    }

    /**
     * 列出所有ds的所有data objects
     *
     * @param operationUserInfo
     * @return
     * @throws ServiceException
     */
    public List<DsDataObjectDo> listAllDataObjects(UserInfo operationUserInfo) throws ServiceException {
        // 授权
        List<Long> listOfDsUid = new ArrayList<>();
        if (MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            this.dsRepository.findAll().forEach(dsDo -> {
                listOfDsUid.add(dsDo.getUid());
            });
        } else {
            List<DsDo> listOfDsDo = this.dsRepository.findByCreateBy(operationUserInfo.getUsername());
            if (CollectionUtils.isNotEmpty(listOfDsDo)) {
                listOfDsDo.forEach(dsDo -> {
                    listOfDsUid.add(dsDo.getUid());
                });
            }
        }

        if (CollectionUtils.isEmpty(listOfDsUid)) {
            return null;
        }

        return this.dsDataObjectRepository.findByDsUidIn(listOfDsUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public DsRefreshResultDto refreshDs(Long dsUid, UserInfo operationUserInfo) throws ServiceException {
        DsRefreshResultDto dsRefreshResultDto = new DsRefreshResultDto();
        dsRefreshResultDto.setDsUid(dsUid);

        DsDo dsDo = this.dsRepository.findByUid(dsUid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsUid);
        }

        // 授权
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            if (!operationUserInfo.getUsername().equalsIgnoreCase(dsDo.getCreateBy())) {
                throw new AuthUnauthorizedException(operationUserInfo.getUsername());
            }
        }

        List<DataObjectDto> inputDataObjectDtoList;
        try {
            inputDataObjectDtoList = this.dsConnectionHandler.loadAllDataObjects(
                    dsDo.getType(), dsDo.getConnectionProfileProps());
        } catch (Exception e) {
            LOGGER.warn("failed to load all data objects from ds " + dsUid + ", " + dsDo.getName(), e);

            dsRefreshResultDto.setSuccessful(false);
            return dsRefreshResultDto;
        }

        List<DsDataObjectDo> existingDsDataObjectDoList = listAllDataObjects(dsUid, operationUserInfo);
        if (existingDsDataObjectDoList == null) {
            existingDsDataObjectDoList = new ArrayList<>();
        }

        // 比较input和existing，可能出现的情况：
        // 1）input有，existing有，不用做什么
        // 2) input有，existing没有，增加
        // 3) input没有，existing有，删除
        List<DsDataObjectDo> toAddListOfDsDataObjectDo = new ArrayList<>();
        List<DsDataObjectDo> toDeleteListOfDsDataObjectDo = new ArrayList<>();

        // input list to map
        Map<String, DataObjectDto> mapOfInput = new HashMap<>();
        inputDataObjectDtoList.forEach(input -> {
            String key = input.getDbName() + "*#*##*" +
                    (input.getSchemaName() == null ? "" : input.getSchemaName()) + "*#*##*" +
                    input.getDataObjectName();
            mapOfInput.put(key, input);
        });
        // existing list to map
        Map<String, DsDataObjectDo> mapOfExisting = new HashMap<>();
        existingDsDataObjectDoList.forEach(existing -> {
            String key = existing.getDbName() + "*#*##*" +
                    (existing.getSchemaName() == null ? "" : existing.getSchemaName()) + "*#*##*" +
                    existing.getDataObjectName();
            mapOfExisting.put(key, existing);
        });

        for (Map.Entry<String, DataObjectDto> inputEntry : mapOfInput.entrySet()) {
            if (mapOfExisting.containsKey(inputEntry.getKey())) {
                // 第1种情况，input有，existing有
                // DO NOTHING
            } else {
                // 第2种情况，input有，existing没有
                DsDataObjectDo dsDataObjectDo = new DsDataObjectDo();
                dsDataObjectDo.setUid(this.idHelper.getNextId(DsDataObjectDo.RESOURCE_NAME));
                dsDataObjectDo.setDsUid(dsUid);
                dsDataObjectDo.setDbName(inputEntry.getValue().getDbName());
                dsDataObjectDo.setSchemaName(inputEntry.getValue().getSchemaName());
                dsDataObjectDo.setDataObjectType(inputEntry.getValue().getDataObjectType());
                dsDataObjectDo.setDataObjectName(inputEntry.getValue().getDataObjectName());
                dsDataObjectDo.setDataObjectDescription(inputEntry.getValue().getDataObjectDescription());
                BaseDo.create(dsDataObjectDo, operationUserInfo.getUsername(), new Date());
                toAddListOfDsDataObjectDo.add(dsDataObjectDo);
            }
        }
        for (Map.Entry<String, DsDataObjectDo> existingEntry : mapOfExisting.entrySet()) {
            if (!mapOfInput.containsKey(existingEntry.getKey())) {
                // 第3种情况，input没有，existing有
                BaseDo.delete(existingEntry.getValue(), operationUserInfo.getUsername(), new Date());
                toDeleteListOfDsDataObjectDo.add(existingEntry.getValue());
            }
        }

        if (CollectionUtils.isNotEmpty(toAddListOfDsDataObjectDo)) {
            this.dsDataObjectRepository.saveAll(toAddListOfDsDataObjectDo);
        }
        if (CollectionUtils.isNotEmpty(toDeleteListOfDsDataObjectDo)) {
            this.dsDataObjectRepository.saveAll(toDeleteListOfDsDataObjectDo);
        }

        dsRefreshResultDto.setSuccessful(true);

        return dsRefreshResultDto;
    }

}