package io.g740.pigeon.biz.service.handler.admin;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.g740.commons.exception.impl.*;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.SingleInstanceUidGenerator;
import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.dictionary.*;
import io.g740.pigeon.biz.object.dto.general.SqlBuildStrategyContentDto;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryBuildProcessDefDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryCategoryDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryContentDo;
import io.g740.pigeon.biz.object.entity.dictionary.DictionaryStructureDo;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryBuildProcessDefRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryCategoryRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryContentRepository;
import io.g740.pigeon.biz.persistence.jpa.dictionary.DictionaryStructureRepository;
import io.g740.pigeon.biz.share.constants.ScheduleTypeEnum;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * @author bbottong
 */
@Handler
public class DictionaryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryHandler.class);

    @Autowired
    private DictionaryCategoryRepository dictionaryCategoryRepository;

    @Autowired
    private DictionaryStructureRepository dictionaryStructureRepository;

    @Autowired
    private DictionaryContentRepository dictionaryContentRepository;

    @Autowired
    private DictionaryBuildProcessDefRepository dictionaryBuildProcessDefRepository;

    @Autowired
    private DictionaryBuildProcessHandler dictionaryBuildProcessHandler;

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private SingleInstanceUidGenerator singleInstanceUidGenerator;

    public DictionaryCategoryDto getDictionaryCategory(Long dictionaryCategoryUid) throws ServiceException {
        DictionaryCategoryDo dictionaryCategoryDo = this.dictionaryCategoryRepository.findByUid(dictionaryCategoryUid);

        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + dictionaryCategoryUid);
        }

        DictionaryCategoryDto dictionaryCategoryDto = new DictionaryCategoryDto();
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());
        dictionaryCategoryDto.setName(dictionaryCategoryDo.getName());
        dictionaryCategoryDto.setDescription(dictionaryCategoryDo.getDescription());
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());

        return dictionaryCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryCategoryDto createDictionaryCategory(CreateDictionaryCategoryDto createDictionaryCategoryDto,
                                                          UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ??????????????????
        // Step 1.1, ????????????name????????????
        boolean existsDuplicate = this.dictionaryCategoryRepository.existsByName(createDictionaryCategoryDto.getName());
        if (existsDuplicate) {
            throw new ResourceDuplicateException(DictionaryCategoryDo.RESOURCE_NAME + ":" +
                    "duplicate reason: " + createDictionaryCategoryDto.getName());
        }

        // Step 2, ????????????
        DictionaryCategoryDo dictionaryCategoryDo = new DictionaryCategoryDo();
        dictionaryCategoryDo.setUid(this.idHelper.getNextId(DictionaryCategoryDo.RESOURCE_NAME));
        dictionaryCategoryDo.setName(createDictionaryCategoryDto.getName());
        dictionaryCategoryDo.setDescription(createDictionaryCategoryDto.getDescription());
        BaseDo.create(dictionaryCategoryDo, operationUserInfo.getUsername(), new Date());
        this.dictionaryCategoryRepository.save(dictionaryCategoryDo);

        // Step 3, ??????????????????
        DictionaryCategoryDto dictionaryCategoryDto = new DictionaryCategoryDto();
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());
        dictionaryCategoryDto.setName(dictionaryCategoryDo.getName());
        dictionaryCategoryDto.setDescription(dictionaryCategoryDo.getDescription());
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());

        return dictionaryCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryCategoryDto updateDictionaryCategory(UpdateDictionaryCategoryDto updateDictionaryCategoryDto,
                                                          UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryCategoryDo dictionaryCategoryDo =
                this.dictionaryCategoryRepository.findByUid(updateDictionaryCategoryDto.getUid());
        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" +
                    updateDictionaryCategoryDto.getUid());
        }

        // Step 2, ??????????????????
        // Step 2.1, ??????????????????name????????????????????????name??????
        if (!Strings.isNullOrEmpty(updateDictionaryCategoryDto.getName()) &&
                !updateDictionaryCategoryDto.getName().equalsIgnoreCase(dictionaryCategoryDo.getName())) {
            boolean existsDuplicate = this.dictionaryCategoryRepository.existsByName(updateDictionaryCategoryDto.getName());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DictionaryCategoryDo.RESOURCE_NAME + ":" +
                        "duplicate reason: " + updateDictionaryCategoryDto.getName());
            }
        }

        // Step 3, ????????????
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDictionaryCategoryDto.getName()) &&
                !updateDictionaryCategoryDto.getName().equalsIgnoreCase(dictionaryCategoryDo.getName())) {
            dictionaryCategoryDo.setName(updateDictionaryCategoryDto.getName());
            requiredToUpdate = true;
        }
        if (!Strings.isNullOrEmpty(updateDictionaryCategoryDto.getDescription()) &&
                !updateDictionaryCategoryDto.getDescription().equalsIgnoreCase(dictionaryCategoryDo.getDescription())) {
            dictionaryCategoryDo.setDescription(updateDictionaryCategoryDto.getDescription());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(dictionaryCategoryDo, operationUserInfo.getUsername(), new Date());
            this.dictionaryCategoryRepository.save(dictionaryCategoryDo);
        }

        // Step 4, ??????????????????
        DictionaryCategoryDto dictionaryCategoryDto = new DictionaryCategoryDto();
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());
        dictionaryCategoryDto.setName(dictionaryCategoryDo.getName());
        dictionaryCategoryDto.setDescription(dictionaryCategoryDo.getDescription());
        dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());

        return dictionaryCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictionaryCategory(Long uid, UserInfo operationUserInfo) {
        // Step 1, ??????????????????
        boolean exists = this.dictionaryCategoryRepository.existsByUid(uid);
        if (!exists) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 2, ??????????????????????????????
        boolean existsUsage = this.dictionaryStructureRepository.existsByDictionaryCategoryUid(uid);
        if (existsUsage) {
            throw new ResourceInUseException(DictionaryCategoryDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 3, ????????????
        deleteDictionaryBuildProcessDef(uid, operationUserInfo);
        this.dictionaryCategoryRepository.deleteByUid(uid);
    }

    public List<DictionaryCategoryDto> listAllDictionaryCategories(UserInfo operationUserInfo) throws ServiceException {
        List<DictionaryCategoryDto> listOfDictionaryCategoryDto = new ArrayList<>();
        this.dictionaryCategoryRepository.findAll().forEach(dictionaryCategoryDo -> {
            DictionaryCategoryDto dictionaryCategoryDto = new DictionaryCategoryDto();
            dictionaryCategoryDto.setUidCode(dictionaryCategoryDo.getUid());
            dictionaryCategoryDto.setName(dictionaryCategoryDo.getName());
            dictionaryCategoryDto.setDescription(dictionaryCategoryDo.getDescription());

            listOfDictionaryCategoryDto.add(dictionaryCategoryDto);
        });
        return listOfDictionaryCategoryDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryStructureDto createDictionaryStructure(CreateDictionaryStructureDto createDictionaryStructureDto,
                                                            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ??????????????????
        // Step 1.1, ??????????????????dictionary category????????????????????????????????????1???
        boolean existsDuplicate;
        if (createDictionaryStructureDto.getParentUid() == null) {
            List<DictionaryStructureDo> listOfDictionaryStructureDo = this.dictionaryStructureRepository.findByDictionaryCategoryUidWithoutParentUid(
                    createDictionaryStructureDto.getDictionaryCategoryUid());
            if (CollectionUtils.isNotEmpty(listOfDictionaryStructureDo)) {
                throw new ResourceDuplicateException(DictionaryStructureDo.RESOURCE_NAME + ":" + createDictionaryStructureDto.getDictionaryCategoryUid());
            }
        } else {
            List<DictionaryStructureDo> listOfDictionaryStructureDo = this.dictionaryStructureRepository.findByDictionaryCategoryUidWithParentUid(
                    createDictionaryStructureDto.getDictionaryCategoryUid(),
                    createDictionaryStructureDto.getParentUid());
            if (CollectionUtils.isNotEmpty(listOfDictionaryStructureDo)) {
                throw new ResourceDuplicateException(DictionaryStructureDo.RESOURCE_NAME + ":" +
                        createDictionaryStructureDto.getDictionaryCategoryUid() + ", " +
                        createDictionaryStructureDto.getParentUid());
            }
        }

        // Step 2, ????????????
        DictionaryStructureDo dictionaryStructureDo = new DictionaryStructureDo();
        dictionaryStructureDo.setDictionaryCategoryUid(createDictionaryStructureDto.getDictionaryCategoryUid());
        dictionaryStructureDo.setParentUid(createDictionaryStructureDto.getParentUid());
        dictionaryStructureDo.setUid(this.idHelper.getNextId(DictionaryStructureDo.RESOURCE_NAME));
        dictionaryStructureDo.setName(createDictionaryStructureDto.getName());
        dictionaryStructureDo.setDescription(createDictionaryStructureDto.getDescription());
        BaseDo.create(dictionaryStructureDo, operationUserInfo.getUsername(), new Date());
        this.dictionaryStructureRepository.save(dictionaryStructureDo);

        // Step 3, ??????????????????
        DictionaryStructureDto dictionaryStructureDto = new DictionaryStructureDto();
        dictionaryStructureDto.setDictionaryCategoryUidCode(dictionaryStructureDo.getDictionaryCategoryUid());
        dictionaryStructureDto.setUidCode(dictionaryStructureDo.getUid());
        dictionaryStructureDto.setName(dictionaryStructureDo.getName());
        dictionaryStructureDto.setDescription(dictionaryStructureDo.getDescription());
        dictionaryStructureDto.setParentUidCode(dictionaryStructureDo.getParentUid());

        return dictionaryStructureDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryStructureDto updateDictionaryStructure(UpdateDictionaryStructureDto updateDictionaryStructureDto,
                                                            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryStructureDo dictionaryStructureDo =
                this.dictionaryStructureRepository.findByUid(updateDictionaryStructureDto.getUid());
        if (dictionaryStructureDo == null) {
            throw new ResourceNotFoundException(DictionaryStructureDo.RESOURCE_NAME + ":" +
                    updateDictionaryStructureDto.getUid());
        }

        // Step 2, ??????????????????
        // Step 2.1, ??????????????????name????????????????????????name??????
        boolean existsDuplicate;
        if (!Strings.isNullOrEmpty(updateDictionaryStructureDto.getName()) &&
                !updateDictionaryStructureDto.getName().equalsIgnoreCase(dictionaryStructureDo.getName())) {
            if (dictionaryStructureDo.getParentUid() == null) {
                existsDuplicate = this.dictionaryStructureRepository.existsByDictionaryCategoryUidAndName(
                        dictionaryStructureDo.getDictionaryCategoryUid(),
                        updateDictionaryStructureDto.getName());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryStructureDo.RESOURCE_NAME + ":" +
                            "duplicate reason: " + dictionaryStructureDo.getDictionaryCategoryUid() + ", " +
                            updateDictionaryStructureDto.getName());
                }
            } else {
                existsDuplicate = this.dictionaryStructureRepository.existsByDictionaryCategoryUidAndParentUidAndName(
                        dictionaryStructureDo.getDictionaryCategoryUid(),
                        dictionaryStructureDo.getParentUid(),
                        updateDictionaryStructureDto.getName());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryStructureDo.RESOURCE_NAME + ":" +
                            "duplicate reason: " + dictionaryStructureDo.getDictionaryCategoryUid() + ", " +
                            dictionaryStructureDo.getParentUid() + ", " +
                            updateDictionaryStructureDto.getName());
                }
            }
        }

        // Step 3, ????????????
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDictionaryStructureDto.getName()) &&
                !updateDictionaryStructureDto.getName().equalsIgnoreCase(dictionaryStructureDo.getName())) {
            dictionaryStructureDo.setName(updateDictionaryStructureDto.getName());
            requiredToUpdate = true;
        }
        if (!Strings.isNullOrEmpty(updateDictionaryStructureDto.getDescription()) &&
                !updateDictionaryStructureDto.getDescription().equalsIgnoreCase(dictionaryStructureDo.getDescription())) {
            dictionaryStructureDo.setDescription(updateDictionaryStructureDto.getDescription());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(dictionaryStructureDo, operationUserInfo.getUsername(), new Date());
            this.dictionaryStructureRepository.save(dictionaryStructureDo);
        }

        // Step 4, ??????????????????
        DictionaryStructureDto dictionaryStructureDto = new DictionaryStructureDto();
        dictionaryStructureDto.setDictionaryCategoryUidCode(dictionaryStructureDo.getDictionaryCategoryUid());
        dictionaryStructureDto.setUidCode(dictionaryStructureDo.getUid());
        dictionaryStructureDto.setName(dictionaryStructureDo.getName());
        dictionaryStructureDto.setDescription(dictionaryStructureDo.getDescription());
        dictionaryStructureDto.setParentUidCode(dictionaryStructureDo.getParentUid());

        return dictionaryStructureDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictionaryStructure(Long uid, UserInfo operationUserInfo) {
        // Step 1, ??????????????????
        boolean exists =
                this.dictionaryStructureRepository.existsByUid(uid);
        if (!exists) {
            throw new ResourceNotFoundException(DictionaryStructureDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 2, ??????????????????????????????
        boolean existsChildren = this.dictionaryStructureRepository.existsByParentUid(uid);
        if (existsChildren) {
            throw new ResourceInUseException(DictionaryStructureDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 3, ????????????
        this.dictionaryStructureRepository.deleteByUid(uid);
    }

    public SimpleTreeNode listAllDictionaryStructures(UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        int page = 0;
        int size = 10;
        int numberOfElements;
        do {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<DictionaryCategoryDo> pageResultOfDictionaryCategoryDo =
                    this.dictionaryCategoryRepository.findAll(pageRequest);
            numberOfElements = pageResultOfDictionaryCategoryDo.getNumberOfElements();
            if (numberOfElements > 0) {
                List<DictionaryCategoryDo> listOfDictionaryCategoryDo =
                        pageResultOfDictionaryCategoryDo.getContent();
                List<SimpleTreeNode> intermediateResult = buildDictionaryStructureTree(listOfDictionaryCategoryDo);

                result.getChildren().addAll(intermediateResult);
            }

            page++;
        } while (numberOfElements > 0);

        return result;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param listOfDictionaryCategoryDo
     * @return
     */
    private List<SimpleTreeNode> buildDictionaryStructureTree(List<DictionaryCategoryDo> listOfDictionaryCategoryDo) {
        List<SimpleTreeNode> result = new ArrayList<>();
        // Key - ????????????ID, Value - ????????????????????????
        Map<Long, SimpleTreeNode> mapOfResult = new HashMap<>(listOfDictionaryCategoryDo.size());

        //
        // Step 1, ???????????????1??????????????????
        //
        List<Long> listOfDictionaryCategoryUid = new ArrayList<>();
        for (DictionaryCategoryDo dictionaryCategoryDo : listOfDictionaryCategoryDo) {
            listOfDictionaryCategoryUid.add(dictionaryCategoryDo.getUid());
            SimpleTreeNode treeNode = new SimpleTreeNode();
            treeNode.setUidCode(dictionaryCategoryDo.getUid());
            treeNode.setName(dictionaryCategoryDo.getName());
            treeNode.setDescription(dictionaryCategoryDo.getDescription());
            treeNode.setType(DictionaryCategoryDo.TYPE);
            treeNode.setChildren(null);
            result.add(treeNode);

            mapOfResult.put(dictionaryCategoryDo.getUid(), treeNode);
        }

        //
        // Step 2, ???????????????2??????????????????????????????????????????????????????
        //
        List<DictionaryStructureDo> listOfDictionaryStructureDo =
                this.dictionaryStructureRepository.findByDictionaryCategoryUidIn(listOfDictionaryCategoryUid);
        if (CollectionUtils.isEmpty(listOfDictionaryStructureDo)) {
            return result;
        }
        // ??????parent uid??????????????????by dictionary category uid, ??????dictionary structure
        Map<Long, List<DictionaryStructureDo>> mapOfDictionaryStructureWithParent = new HashMap<>(10);
        // ??????parent uid??????????????????by dictionary category uid, ??????dictionary structure
        Map<Long, List<DictionaryStructureDo>> mapOfDictionaryStructureWithoutParent = new HashMap<>(10);
        for (DictionaryStructureDo dictionaryStructureDo : listOfDictionaryStructureDo) {
            Long dictionaryCategoryUid = dictionaryStructureDo.getDictionaryCategoryUid();
            Long parentId = dictionaryStructureDo.getParentUid();
            if (parentId == null) {
                if (!mapOfDictionaryStructureWithoutParent.containsKey(dictionaryCategoryUid)) {
                    mapOfDictionaryStructureWithoutParent.put(dictionaryCategoryUid, new ArrayList<>());
                }
                mapOfDictionaryStructureWithoutParent.get(dictionaryCategoryUid).add(dictionaryStructureDo);
            } else {
                if (!mapOfDictionaryStructureWithParent.containsKey(dictionaryCategoryUid)) {
                    mapOfDictionaryStructureWithParent.put(dictionaryCategoryUid, new ArrayList<>());
                }
                mapOfDictionaryStructureWithParent.get(dictionaryCategoryUid).add(dictionaryStructureDo);
            }
        }

        // ????????????????????????????????????????????????????????????key - dictionary structure uid
        Map<Long, SimpleTreeNode> mapOfDictionaryStructureTreeNode = new HashMap<>(10);

        //
        // Step 2.1, ???????????????2??????????????????????????????????????????????????????
        //
        for (Map.Entry<Long, List<DictionaryStructureDo>> entry : mapOfDictionaryStructureWithoutParent.entrySet()) {
            Long dictionaryCategoryUid = entry.getKey();

            SimpleTreeNode dictionaryCategoryTreeNode = mapOfResult.get(dictionaryCategoryUid);
            dictionaryCategoryTreeNode.setChildren(new ArrayList<>());

            for (DictionaryStructureDo dictionaryStructureDo : entry.getValue()) {
                SimpleTreeNode dictionaryStructureTreeNode = new SimpleTreeNode();
                dictionaryStructureTreeNode.setUidCode(dictionaryStructureDo.getUid());
                dictionaryStructureTreeNode.setName(dictionaryStructureDo.getName());
                dictionaryStructureTreeNode.setDescription(dictionaryStructureDo.getDescription());
                dictionaryStructureTreeNode.setType(DictionaryStructureDo.TYPE);
                dictionaryStructureTreeNode.setChildren(null);

                dictionaryCategoryTreeNode.getChildren().add(dictionaryStructureTreeNode);

                mapOfDictionaryStructureTreeNode.put(dictionaryStructureDo.getUid(), dictionaryStructureTreeNode);
            }
        }

        //
        // Step 2.2, ???????????????3?????????????????????????????????????????????????????????????????????
        //
        for (Map.Entry<Long, List<DictionaryStructureDo>> entry : mapOfDictionaryStructureWithParent.entrySet()) {
            for (DictionaryStructureDo dictionaryStructureDo : entry.getValue()) {
                //
                // Step 2.2.1?????????????????????????????????
                //
                SimpleTreeNode dictionaryStructureTreeNode;
                if (mapOfDictionaryStructureTreeNode.containsKey(dictionaryStructureDo.getUid())) {
                    // ????????????????????????????????????????????????children???????????????????????????????????????????????????????????????
                    // ??????????????????????????????????????????children?????????
                    dictionaryStructureTreeNode =
                            mapOfDictionaryStructureTreeNode.get(dictionaryStructureDo.getUid());
                    dictionaryStructureTreeNode.setName(dictionaryStructureDo.getName());
                    dictionaryStructureTreeNode.setDescription(dictionaryStructureDo.getDescription());
                    dictionaryStructureTreeNode.setType(DictionaryStructureDo.TYPE);
                } else {
                    dictionaryStructureTreeNode = new SimpleTreeNode();
                    dictionaryStructureTreeNode.setUidCode(dictionaryStructureDo.getUid());
                    dictionaryStructureTreeNode.setName(dictionaryStructureDo.getName());
                    dictionaryStructureTreeNode.setDescription(dictionaryStructureDo.getDescription());
                    dictionaryStructureTreeNode.setType(DictionaryStructureDo.TYPE);
                    dictionaryStructureTreeNode.setChildren(null);

                    mapOfDictionaryStructureTreeNode.put(dictionaryStructureTreeNode.getUidCode(),
                            dictionaryStructureTreeNode);
                }

                //
                // Step 2.2.2, ??????????????????????????????????????????
                //
                if (mapOfDictionaryStructureTreeNode.containsKey(dictionaryStructureDo.getParentUid())) {
                    // ??????????????????????????????
                    SimpleTreeNode parentDictionaryStructureTreeNode =
                            mapOfDictionaryStructureTreeNode.get(dictionaryStructureDo.getParentUid());
                    if (CollectionUtils.isEmpty(parentDictionaryStructureTreeNode.getChildren())) {
                        parentDictionaryStructureTreeNode.setChildren(new ArrayList<>());
                    }
                    parentDictionaryStructureTreeNode.getChildren().add(dictionaryStructureTreeNode);
                } else {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    SimpleTreeNode shadowDictionaryStructureTreeNode = new SimpleTreeNode();
                    shadowDictionaryStructureTreeNode.setUidCode(dictionaryStructureDo.getParentUid());
                    shadowDictionaryStructureTreeNode.setName(null);
                    shadowDictionaryStructureTreeNode.setDescription(null);
                    shadowDictionaryStructureTreeNode.setType(DictionaryStructureDo.TYPE);

                    mapOfDictionaryStructureTreeNode.put(shadowDictionaryStructureTreeNode.getUidCode(),
                            shadowDictionaryStructureTreeNode);

                    // ????????????????????????????????????
                    shadowDictionaryStructureTreeNode.setChildren(new ArrayList<>());
                    shadowDictionaryStructureTreeNode.getChildren().add(dictionaryStructureTreeNode);
                }
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryBuildProcessDefDto createDictionaryBuildProcessDef(
            CreateDictionaryBuildProcessDefDto createDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step1, ??????????????????
        Long dictionaryCategoryUid = createDictionaryBuildProcessDefDto.getDictionaryCategoryUid();
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (dictionaryBuildProcessDefDo != null) {
            throw new ResourceDuplicateException(DictionaryBuildProcessDefDo.RESOURCE_NAME + "::"
                    + "dictionary_category_uid:" + dictionaryCategoryUid);
        }

        // Step 2, ????????????
        dictionaryBuildProcessDefDo = new DictionaryBuildProcessDefDo();
        dictionaryBuildProcessDefDo.setUid(this.idHelper.getNextId(DictionaryBuildProcessDefDo.RESOURCE_NAME));
        dictionaryBuildProcessDefDo.setEnabled(createDictionaryBuildProcessDefDto.getEnabled());
        dictionaryBuildProcessDefDo.setScheduleType(createDictionaryBuildProcessDefDto.getScheduleType());
        dictionaryBuildProcessDefDo.setScheduleTypeExtDetails(createDictionaryBuildProcessDefDto.getScheduleTypeExtDetails());
        dictionaryBuildProcessDefDo.setBuildStrategyType(createDictionaryBuildProcessDefDto.getBuildStrategyType());

        switch (createDictionaryBuildProcessDefDto.getBuildStrategyType()) {
            case SQL:
                dictionaryBuildProcessDefDo.setBuildStrategyContent(JSON.toJSONString(createDictionaryBuildProcessDefDto.getSqlBuildStrategyContent()));
                break;
        }

        dictionaryBuildProcessDefDo.setDictionaryCategoryUid(createDictionaryBuildProcessDefDto.getDictionaryCategoryUid());
        BaseDo.create(dictionaryBuildProcessDefDo, operationUserInfo.getUsername(), new Date());
        this.dictionaryBuildProcessDefRepository.save(dictionaryBuildProcessDefDo);

        // Step 3, ??????process??????
        if (createDictionaryBuildProcessDefDto.getEnabled() != null &&
                Boolean.TRUE.equals(createDictionaryBuildProcessDefDto.getEnabled())) {
            if (ScheduleTypeEnum.PERIODIC.equals(createDictionaryBuildProcessDefDto.getScheduleType())) {
                try {
                    this.dictionaryBuildProcessHandler.initRegisterScheduling(
                            dictionaryBuildProcessDefDo.getUid(),
                            createDictionaryBuildProcessDefDto.getScheduleTypeExtDetails());
                } catch (Exception e) {
                    String errorMessage = "failed to register scheduling for dictionary build process def " +
                            dictionaryBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                    throw new ServiceException(errorMessage, e);
                }
            }
        }

        // Step 4, ??????????????????
        DictionaryBuildProcessDefDto dictionaryBuildProcessDefDto = new DictionaryBuildProcessDefDto();
        dictionaryBuildProcessDefDto.setUid(dictionaryBuildProcessDefDo.getUid());
        dictionaryBuildProcessDefDto.setEnabled(dictionaryBuildProcessDefDo.getEnabled());
        dictionaryBuildProcessDefDto.setScheduleType(dictionaryBuildProcessDefDo.getScheduleType());
        dictionaryBuildProcessDefDto.setScheduleTypeExtDetails(dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
        dictionaryBuildProcessDefDto.setBuildStrategyType(dictionaryBuildProcessDefDo.getBuildStrategyType());
        dictionaryBuildProcessDefDto.setSqlBuildStrategyContent(createDictionaryBuildProcessDefDto.getSqlBuildStrategyContent());
        dictionaryBuildProcessDefDto.setDictionaryCategoryUid(dictionaryBuildProcessDefDo.getDictionaryCategoryUid());

        return dictionaryBuildProcessDefDto;
    }

    public DictionaryBuildProcessDefDto queryDictionaryBuildProcessDef(
            Long dictionaryCategoryUid,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (dictionaryBuildProcessDefDo == null) {
            return null;
        }

        // Step 2, ??????????????????
        DictionaryBuildProcessDefDto dictionaryBuildProcessDefDto = new DictionaryBuildProcessDefDto();
        dictionaryBuildProcessDefDto.setUid(dictionaryBuildProcessDefDo.getUid());
        dictionaryBuildProcessDefDto.setEnabled(dictionaryBuildProcessDefDo.getEnabled());
        dictionaryBuildProcessDefDto.setScheduleType(dictionaryBuildProcessDefDo.getScheduleType());
        dictionaryBuildProcessDefDto.setScheduleTypeExtDetails(dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
        dictionaryBuildProcessDefDto.setBuildStrategyType(dictionaryBuildProcessDefDo.getBuildStrategyType());

        String buildStrategyContent = dictionaryBuildProcessDefDo.getBuildStrategyContent();
        switch (dictionaryBuildProcessDefDo.getBuildStrategyType()) {
            case SQL:
                SqlBuildStrategyContentDto sqlBuildStrategyContent = JSON.parseObject(buildStrategyContent, SqlBuildStrategyContentDto.class);
                dictionaryBuildProcessDefDto.setSqlBuildStrategyContent(sqlBuildStrategyContent);
                break;
        }

        dictionaryBuildProcessDefDto.setDictionaryCategoryUid(dictionaryBuildProcessDefDo.getDictionaryCategoryUid());

        return dictionaryBuildProcessDefDto;
    }

    public DictionaryBuildProcessDefDto getDictionaryBuildProcessDef(
            Long processDefUid,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByUid(processDefUid);
        if (dictionaryBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DictionaryBuildProcessDefDo.RESOURCE_NAME + ":" + processDefUid);
        }

        // Step 2, ??????????????????
        DictionaryBuildProcessDefDto dictionaryBuildProcessDefDto = new DictionaryBuildProcessDefDto();
        dictionaryBuildProcessDefDto.setUid(dictionaryBuildProcessDefDo.getUid());
        dictionaryBuildProcessDefDto.setEnabled(dictionaryBuildProcessDefDo.getEnabled());
        dictionaryBuildProcessDefDto.setScheduleType(dictionaryBuildProcessDefDo.getScheduleType());
        dictionaryBuildProcessDefDto.setScheduleTypeExtDetails(dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
        dictionaryBuildProcessDefDto.setBuildStrategyType(dictionaryBuildProcessDefDo.getBuildStrategyType());

        String buildStrategyContent = dictionaryBuildProcessDefDo.getBuildStrategyContent();
        switch (dictionaryBuildProcessDefDo.getBuildStrategyType()) {
            case SQL:
                SqlBuildStrategyContentDto sqlBuildStrategyContent = JSON.parseObject(buildStrategyContent, SqlBuildStrategyContentDto.class);
                dictionaryBuildProcessDefDto.setSqlBuildStrategyContent(sqlBuildStrategyContent);
                break;
        }

        dictionaryBuildProcessDefDto.setDictionaryCategoryUid(dictionaryBuildProcessDefDo.getDictionaryCategoryUid());

        return dictionaryBuildProcessDefDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryBuildProcessDefDto updateDictionaryBuildProcessDef(
            UpdateDictionaryBuildProcessDefDto updateDictionaryBuildProcessDefDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByUid(updateDictionaryBuildProcessDefDto.getUid());
        if (dictionaryBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DictionaryBuildProcessDefDo.RESOURCE_NAME + ":" +
                    updateDictionaryBuildProcessDefDto.getUid());
        }

        // Step 2, ??????????????????
        Long dictionaryCategoryUid = updateDictionaryBuildProcessDefDto.getDictionaryCategoryUid();
        dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (dictionaryBuildProcessDefDo == null) {
            throw new ResourceNotFoundException(DictionaryBuildProcessDefDo.RESOURCE_NAME + "::"
                    + "dictionary_category_uid:" + dictionaryCategoryUid);
        } else if (!dictionaryBuildProcessDefDo.getUid().equals(updateDictionaryBuildProcessDefDto.getUid())) {
            throw new ResourceDataIntegrityViolationException("found dictionary_build_process_def:"
                    + dictionaryBuildProcessDefDo.getUid() + " associated to"
                    + " dictionary_category_uid:" + dictionaryCategoryUid
                    + ", but incoming request trying to update dictionary_build_process_def:"
                    + updateDictionaryBuildProcessDefDto.getUid() + " w/ dictionary_category_uid:"
                    + updateDictionaryBuildProcessDefDto.getDictionaryCategoryUid());
        }

        // Step 3, ????????????
        boolean requiredToUpdate = false;
        // ????????????enabled, schedule type, schedule ext details??????????????????
        // ????????????enabled = true, schedule type = periodic??????scheduling?????????
        boolean alreadyRegisteredScheduling = false;
        boolean scheduleTypeExtDetailsChanged = false;
        if (dictionaryBuildProcessDefDo.getEnabled() != null &&
                Boolean.TRUE.equals(dictionaryBuildProcessDefDo.getEnabled()) &&
                ScheduleTypeEnum.PERIODIC.equals(dictionaryBuildProcessDefDo.getScheduleType())) {
            alreadyRegisteredScheduling = true;
        }
        if (updateDictionaryBuildProcessDefDto.getEnabled() != null
                && !updateDictionaryBuildProcessDefDto.getEnabled().equals(dictionaryBuildProcessDefDo.getEnabled())) {
            dictionaryBuildProcessDefDo.setEnabled(updateDictionaryBuildProcessDefDto.getEnabled());
            requiredToUpdate = true;
        }
        if (updateDictionaryBuildProcessDefDto.getScheduleType() != null
                && !updateDictionaryBuildProcessDefDto.getScheduleType().equals(dictionaryBuildProcessDefDo.getScheduleType())) {
            dictionaryBuildProcessDefDo.setScheduleType(updateDictionaryBuildProcessDefDto.getScheduleType());
            requiredToUpdate = true;
        }
        if (updateDictionaryBuildProcessDefDto.getScheduleTypeExtDetails() != null
                && !updateDictionaryBuildProcessDefDto.getScheduleTypeExtDetails().equals(dictionaryBuildProcessDefDo.getScheduleTypeExtDetails())) {
            dictionaryBuildProcessDefDo.setScheduleTypeExtDetails(updateDictionaryBuildProcessDefDto.getScheduleTypeExtDetails());
            requiredToUpdate = true;

            scheduleTypeExtDetailsChanged = true;
        }
        if (updateDictionaryBuildProcessDefDto.getBuildStrategyType() != null
                && !updateDictionaryBuildProcessDefDto.getBuildStrategyType().equals(dictionaryBuildProcessDefDo.getBuildStrategyType())) {
            dictionaryBuildProcessDefDo.setBuildStrategyType(updateDictionaryBuildProcessDefDto.getBuildStrategyType());
            requiredToUpdate = true;
        }

        if (updateDictionaryBuildProcessDefDto.getBuildStrategyType() != null) {
            switch (updateDictionaryBuildProcessDefDto.getBuildStrategyType()) {
                case SQL:
                    String updateBuildStrategyContent = JSON.toJSONString(updateDictionaryBuildProcessDefDto.getSqlBuildStrategyContent());
                    if (!updateBuildStrategyContent.equalsIgnoreCase(dictionaryBuildProcessDefDo.getBuildStrategyContent())) {
                        dictionaryBuildProcessDefDo.setBuildStrategyContent(updateBuildStrategyContent);
                        requiredToUpdate = true;
                    }
                    break;
            }
        } else {
            switch (dictionaryBuildProcessDefDo.getBuildStrategyType()) {
                case SQL:
                    String updateBuildStrategyContent = JSON.toJSONString(updateDictionaryBuildProcessDefDto.getSqlBuildStrategyContent());
                    if (!updateBuildStrategyContent.equalsIgnoreCase(dictionaryBuildProcessDefDo.getBuildStrategyContent())) {
                        dictionaryBuildProcessDefDo.setBuildStrategyContent(updateBuildStrategyContent);
                        requiredToUpdate = true;
                    }
                    break;
            }
        }

        if (updateDictionaryBuildProcessDefDto.getDictionaryCategoryUid() != null
                && !updateDictionaryBuildProcessDefDto.getDictionaryCategoryUid().equals(dictionaryBuildProcessDefDo.getDictionaryCategoryUid())) {
            dictionaryBuildProcessDefDo.setDictionaryCategoryUid(updateDictionaryBuildProcessDefDto.getDictionaryCategoryUid());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(dictionaryBuildProcessDefDo, operationUserInfo.getUsername(), new Date());
            this.dictionaryBuildProcessDefRepository.save(dictionaryBuildProcessDefDo);
        }

        // Step 4, ??????process??????
        // ?????????????????????enabled, schedule type, schedule type ext details?????????????????????????????????????????????????????????
        // register/revoke/update scheduling
        // ?????????????????????????????????register scheduling
        if (dictionaryBuildProcessDefDo.getEnabled() != null &&
                Boolean.TRUE.equals(dictionaryBuildProcessDefDo.getEnabled()) &&
                ScheduleTypeEnum.PERIODIC.equals(dictionaryBuildProcessDefDo.getScheduleType())) {
            // ???????????????????????????register scheduling
            if (alreadyRegisteredScheduling) {
                // ?????????register????????????????????????????????????schedule type ext details
                if (scheduleTypeExtDetailsChanged) {
                    // ext details?????????????????????revoke??????register
                    this.dictionaryBuildProcessHandler.revokeScheduling(dictionaryBuildProcessDefDo.getUid());
                    try {
                        this.dictionaryBuildProcessHandler.initRegisterScheduling(dictionaryBuildProcessDefDo.getUid(),
                                dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
                    } catch (Exception e) {
                        String errorMessage = "failed to register scheduling for dictionary build process def " +
                                dictionaryBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                        throw new ServiceException(errorMessage, e);
                    }
                } else {
                    // ext details???????????????DO NOTHING
                }
            } else {
                // ????????????register?????????register
                try {
                    this.dictionaryBuildProcessHandler.initRegisterScheduling(dictionaryBuildProcessDefDo.getUid(),
                            dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
                } catch (Exception e) {
                    String errorMessage = "failed to register scheduling for dictionary build process def " +
                            dictionaryBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                    throw new ServiceException(errorMessage, e);
                }
            }
        } else {
            // ??????????????????????????????register scheduling
            if (alreadyRegisteredScheduling) {
                // ?????????register??????????????????????????????revoke
                this.dictionaryBuildProcessHandler.revokeScheduling(dictionaryBuildProcessDefDo.getUid());
            } else {
                // ????????????register???DO NOTHING
            }
        }

        // Step 5, ??????????????????
        DictionaryBuildProcessDefDto dictionaryBuildProcessDefDto = new DictionaryBuildProcessDefDto();
        dictionaryBuildProcessDefDto.setUid(dictionaryBuildProcessDefDo.getUid());
        dictionaryBuildProcessDefDto.setEnabled(dictionaryBuildProcessDefDo.getEnabled());
        dictionaryBuildProcessDefDto.setScheduleType(dictionaryBuildProcessDefDo.getScheduleType());
        dictionaryBuildProcessDefDto.setScheduleTypeExtDetails(dictionaryBuildProcessDefDo.getScheduleTypeExtDetails());
        dictionaryBuildProcessDefDto.setBuildStrategyType(dictionaryBuildProcessDefDo.getBuildStrategyType());
        dictionaryBuildProcessDefDto.setSqlBuildStrategyContent(updateDictionaryBuildProcessDefDto.getSqlBuildStrategyContent());
        dictionaryBuildProcessDefDto.setDictionaryCategoryUid(dictionaryBuildProcessDefDo.getDictionaryCategoryUid());

        return dictionaryBuildProcessDefDto;
    }

    private void deleteDictionaryBuildProcessDef(
            Long dictionaryCategoryUid,
            UserInfo operationUserInfo) throws ServiceException {
        DictionaryBuildProcessDefDo dictionaryBuildProcessDefDo =
                this.dictionaryBuildProcessDefRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (dictionaryBuildProcessDefDo != null) {
            if (Boolean.TRUE.equals(dictionaryBuildProcessDefDo.getEnabled())) {
                if (ScheduleTypeEnum.PERIODIC.equals(dictionaryBuildProcessDefDo.getScheduleType())) {
                    try {
                        this.dictionaryBuildProcessHandler.revokeScheduling(dictionaryBuildProcessDefDo.getUid());
                    } catch (Exception e) {
                        String errorMessage = "failed to revoke scheduling for dictionary build process def " +
                                dictionaryBuildProcessDefDo.getUid() + ". More info: " + e.getMessage();
                        throw new ServiceException(errorMessage, e);
                    }
                }
            }

            dictionaryBuildProcessDefDo.setDeleted(Boolean.TRUE);
            BaseDo.update(dictionaryBuildProcessDefDo, operationUserInfo.getUsername(), new java.util.Date());
            this.dictionaryBuildProcessDefRepository.save(dictionaryBuildProcessDefDo);
        }

    }

    public SimpleTreeNode getDictionaryContent(Long dictionaryCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        return buildDictionaryContentTree(dictionaryCategoryUid);
    }

    private SimpleTreeNode buildDictionaryContentTree(Long dictionaryCategoryUid) {
        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();
        rootTreeNode.setChildren(new ArrayList<>());

        //
        // Step 2, ???????????????2??????????????????????????????????????????????????????
        //
        List<DictionaryContentDo> listOfDictionaryContentDo =
                this.dictionaryContentRepository.findByDictionaryCategoryUid(dictionaryCategoryUid);
        if (CollectionUtils.isEmpty(listOfDictionaryContentDo)) {
            return rootTreeNode;
        }
        // ??????parent uid???????????????
        List<DictionaryContentDo> listOfDictionaryContentWithParent = new ArrayList<>(10);
        // ??????parent uid???????????????
        List<DictionaryContentDo> listOfDictionaryContentWithoutParent = new ArrayList<>(10);
        for (DictionaryContentDo dictionaryContentDo : listOfDictionaryContentDo) {
            Long parentId = dictionaryContentDo.getParentUid();
            if (parentId == null) {
                listOfDictionaryContentWithoutParent.add(dictionaryContentDo);
            } else {
                listOfDictionaryContentWithParent.add(dictionaryContentDo);
            }
        }

        // ????????????????????????????????????????????????????????????key - dictionary content uid
        Map<Long, SimpleTreeNode> mapOfDictionaryContentTreeNode = new HashMap<>(10);

        //
        // Step 2.1, ???????????????2??????????????????????????????????????????????????????
        //
        for (DictionaryContentDo dictionaryContentDo : listOfDictionaryContentWithoutParent) {
            SimpleTreeNode dictionaryContentTreeNode = new SimpleTreeNode();
            dictionaryContentTreeNode.setUidCode(dictionaryContentDo.getUid());
            dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
            dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
            dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
            dictionaryContentTreeNode.setChildren(null);

            rootTreeNode.getChildren().add(dictionaryContentTreeNode);

            mapOfDictionaryContentTreeNode.put(dictionaryContentDo.getUid(), dictionaryContentTreeNode);
        }

        //
        // Step 2.2, ???????????????3?????????????????????????????????????????????????????????????????????
        //
        for (DictionaryContentDo dictionaryContentDo : listOfDictionaryContentWithParent) {
            //
            // Step 2.2.1?????????????????????????????????
            //
            SimpleTreeNode dictionaryContentTreeNode;
            if (mapOfDictionaryContentTreeNode.containsKey(dictionaryContentDo.getUid())) {
                // ????????????????????????????????????????????????children???????????????????????????????????????????????????????????????
                // ??????????????????????????????????????????children?????????
                dictionaryContentTreeNode =
                        mapOfDictionaryContentTreeNode.get(dictionaryContentDo.getUid());
                dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
            } else {
                dictionaryContentTreeNode = new SimpleTreeNode();
                dictionaryContentTreeNode.setUidCode(dictionaryContentDo.getUid());
                dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
                dictionaryContentTreeNode.setChildren(null);

                mapOfDictionaryContentTreeNode.put(dictionaryContentTreeNode.getUidCode(),
                        dictionaryContentTreeNode);
            }

            //
            // Step 2.2.2, ??????????????????????????????????????????
            //
            if (mapOfDictionaryContentTreeNode.containsKey(dictionaryContentDo.getParentUid())) {
                // ??????????????????????????????
                SimpleTreeNode parentDictionaryContentTreeNode =
                        mapOfDictionaryContentTreeNode.get(dictionaryContentDo.getParentUid());
                if (CollectionUtils.isEmpty(parentDictionaryContentTreeNode.getChildren())) {
                    parentDictionaryContentTreeNode.setChildren(new ArrayList<>());
                }
                parentDictionaryContentTreeNode.getChildren().add(dictionaryContentTreeNode);
            } else {
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                SimpleTreeNode shadowDictionaryContentTreeNode = new SimpleTreeNode();
                shadowDictionaryContentTreeNode.setUidCode(dictionaryContentDo.getParentUid());
                shadowDictionaryContentTreeNode.setName(null);
                shadowDictionaryContentTreeNode.setDescription(null);
                shadowDictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);

                mapOfDictionaryContentTreeNode.put(shadowDictionaryContentTreeNode.getUidCode(),
                        shadowDictionaryContentTreeNode);

                // ????????????????????????????????????
                shadowDictionaryContentTreeNode.setChildren(new ArrayList<>());
                shadowDictionaryContentTreeNode.getChildren().add(dictionaryContentTreeNode);
            }
        }

        return rootTreeNode;
    }


    public SimpleTreeNode listAllDictionaryContent(UserInfo operationUserInfo) throws ServiceException {
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        int page = 0;
        int size = 10;
        int numberOfElements;
        do {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<DictionaryCategoryDo> pageResultOfDictionaryCategoryDo =
                    this.dictionaryCategoryRepository.findAll(pageRequest);
            numberOfElements = pageResultOfDictionaryCategoryDo.getNumberOfElements();
            if (numberOfElements > 0) {
                List<DictionaryCategoryDo> listOfDictionaryCategoryDo =
                        pageResultOfDictionaryCategoryDo.getContent();
                List<SimpleTreeNode> intermediateResult = buildDictionaryContentTree(listOfDictionaryCategoryDo);

                result.getChildren().addAll(intermediateResult);
            }

            page++;
        } while (numberOfElements > 0);

        return result;
    }

    public SimpleTreeNode getDictionaryContentByDictionaryCategory(
            Long dictionaryCategoryUid, UserInfo operationUserInfo) throws ServiceException {
        DictionaryCategoryDo dictionaryCategoryDo =
                this.dictionaryCategoryRepository.findByUid(dictionaryCategoryUid);
        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + dictionaryCategoryUid);
        }

        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();
        List<DictionaryCategoryDo> listOfDictionaryCategoryDo = new ArrayList<>();
        listOfDictionaryCategoryDo.add(dictionaryCategoryDo);
        List<SimpleTreeNode> intermediateResult = buildDictionaryContentTree(listOfDictionaryCategoryDo);
        result.getChildren().addAll(intermediateResult);

        return result;
    }

    public SimpleTreeNode listDictionaryContentByDictionaryCategoryWithSelectedDictionaryContent(
            Long dictionaryCategoryUid, List<Long> listOfSelectedDictionaryContentUid,
            UserInfo operationUserInfo) throws ServiceException {
        DictionaryCategoryDo dictionaryCategoryDo =
                this.dictionaryCategoryRepository.findByUid(dictionaryCategoryUid);
        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + dictionaryCategoryUid);
        }

        // ??????????????????
        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();
        List<DictionaryCategoryDo> listOfDictionaryCategoryDo = new ArrayList<>();
        listOfDictionaryCategoryDo.add(dictionaryCategoryDo);
        List<SimpleTreeNode> intermediateResult = buildDictionaryContentTree(listOfDictionaryCategoryDo);
        if (CollectionUtils.isNotEmpty(intermediateResult)) {
            // ??????dictionary category
            SimpleTreeNode treeNode = intermediateResult.get(0);
            // ?????????dictionary category????????????????????????????????????
            if (CollectionUtils.isNotEmpty(treeNode.getChildren())) {
                result.getChildren().addAll(treeNode.getChildren());
            }
        }

        //
        if (CollectionUtils.isEmpty(listOfSelectedDictionaryContentUid)) {
            return result;
        }

        // TODO ??????

        return result;
    }

    public SimpleTreeNode listNextLevelDictionaryContent(
            Long dictionaryCategoryUid, Long dictionaryContentUid, UserInfo operationUserInfo) throws ServiceException {
        DictionaryCategoryDo dictionaryCategoryDo =
                this.dictionaryCategoryRepository.findByUid(dictionaryCategoryUid);
        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + dictionaryCategoryUid);
        }

        List<DictionaryContentDo> listOfDictionaryContentDo;
        if (dictionaryContentUid == null) {
            listOfDictionaryContentDo = this.dictionaryContentRepository.findFirstLevelDictionaryContentByDictionaryCategoryUid(dictionaryCategoryUid);
        } else {
            boolean exists = this.dictionaryContentRepository.existsByUid(dictionaryContentUid);
            if (!exists) {
                throw new ResourceNotFoundException(DictionaryContentDo.RESOURCE_NAME + ":" + dictionaryContentUid);
            }
            listOfDictionaryContentDo = this.dictionaryContentRepository.findFirstLevelDictionaryContentByDictionaryCategoryUid(dictionaryCategoryUid, dictionaryContentUid);
        }

        SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();

        if (CollectionUtils.isNotEmpty(listOfDictionaryContentDo)) {
            listOfDictionaryContentDo.forEach(dictionaryContentDo -> {
                SimpleTreeNode dictionaryContentTreeNode = new SimpleTreeNode();
                dictionaryContentTreeNode.setUidCode(dictionaryContentDo.getUid());
                dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
                dictionaryContentTreeNode.setChildren(null);

                result.getChildren().add(dictionaryContentTreeNode);
            });
        }

        return result;
    }

    public SimpleTreeNode queryDictionaryContent(
            Long dictionaryCategoryUid, String label, UserInfo operationUserInfo) throws ServiceException {
        DictionaryCategoryDo dictionaryCategoryDo =
                this.dictionaryCategoryRepository.findByUid(dictionaryCategoryUid);
        if (dictionaryCategoryDo == null) {
            throw new ResourceNotFoundException(DictionaryCategoryDo.RESOURCE_NAME + ":" + dictionaryCategoryUid);
        }

        if (Strings.isNullOrEmpty(label)) {
            return listNextLevelDictionaryContent(dictionaryCategoryUid, null, operationUserInfo);
        } else {
            // ??????????????????
            SimpleTreeNode result = SimpleTreeNode.buildRootTreeNode();
            List<DictionaryCategoryDo> listOfDictionaryCategoryDo = new ArrayList<>();
            listOfDictionaryCategoryDo.add(dictionaryCategoryDo);
            List<SimpleTreeNode> intermediateResult = buildDictionaryContentTree(listOfDictionaryCategoryDo);
            if (CollectionUtils.isNotEmpty(intermediateResult)) {
                // ??????dictionary category
                SimpleTreeNode treeNode = intermediateResult.get(0);
                // ?????????dictionary category????????????????????????????????????
                if (CollectionUtils.isNotEmpty(treeNode.getChildren())) {
                    result.getChildren().addAll(treeNode.getChildren());

                    // ?????????
                    recurseCutUnmatched(result, label, false, true);
                }
            }

            return result;
        }
    }

    /**
     * ????????????????????????????????????labelInLowerCase???????????????
     *
     * @param treeNode
     * @param label              ?????????????????????
     * @param requiredExactMatch ????????????????????????(????????????????????????????????????requiredIgnoreCase??????)???true - ???????????????false - ????????????
     * @param requiredIgnoreCase ??????????????????????????????true - ??????????????????false - ???????????????
     * @return
     */
    private boolean recurseCutUnmatched(SimpleTreeNode treeNode, String label, boolean requiredExactMatch, boolean requiredIgnoreCase) {
        if (treeNode != null && CollectionUtils.isNotEmpty(treeNode.getChildren())) {
            for (int i = 0; i < treeNode.getChildren().size(); i++) {
                SimpleTreeNode childTreeNode = treeNode.getChildren().get(i);
                boolean shouldBeRemoved = recurseCutUnmatched(childTreeNode, label, requiredExactMatch, requiredIgnoreCase);
                if (shouldBeRemoved) {
                    // ??????
                    treeNode.getChildren().remove(childTreeNode);
                    i--;
                }
            }
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
            if (CollectionUtils.isEmpty(treeNode.getChildren())) {
                if (requiredExactMatch) {
                    if (requiredIgnoreCase) {
                        return !treeNode.getDescription().equalsIgnoreCase(label);
                    } else {
                        return !treeNode.getDescription().equals(label);
                    }
                } else {
                    if (requiredIgnoreCase) {
                        return treeNode.getDescription().toLowerCase().contains(label.toLowerCase());
                    } else {
                        return treeNode.getDescription().contains(label);
                    }
                }
            }
        } else if (CollectionUtils.isEmpty(treeNode.getChildren())) {
            if (requiredExactMatch) {
                if (requiredIgnoreCase) {
                    return !treeNode.getDescription().equalsIgnoreCase(label);
                } else {
                    return !treeNode.getDescription().equals(label);
                }
            } else {
                if (requiredIgnoreCase) {
                    return treeNode.getDescription().toLowerCase().contains(label.toLowerCase());
                } else {
                    return treeNode.getDescription().contains(label);
                }
            }
        }

        return false;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param listOfDictionaryCategoryDo
     * @return
     */
    private List<SimpleTreeNode> buildDictionaryContentTree(List<DictionaryCategoryDo> listOfDictionaryCategoryDo) {
        List<SimpleTreeNode> result = new ArrayList<>();
        // Key - ????????????ID, Value - ????????????????????????
        Map<Long, SimpleTreeNode> mapOfResult = new HashMap<>(listOfDictionaryCategoryDo.size());

        //
        // Step 1, ???????????????1????????????????????????
        //
        List<Long> listOfDictionaryCategoryUid = new ArrayList<>();
        for (DictionaryCategoryDo dictionaryCategoryDo : listOfDictionaryCategoryDo) {
            listOfDictionaryCategoryUid.add(dictionaryCategoryDo.getUid());
            SimpleTreeNode treeNode = new SimpleTreeNode();
            treeNode.setUidCode(dictionaryCategoryDo.getUid());
            treeNode.setName(dictionaryCategoryDo.getName());
            treeNode.setDescription(dictionaryCategoryDo.getDescription());
            treeNode.setType(DictionaryCategoryDo.TYPE);
            treeNode.setChildren(null);
            result.add(treeNode);

            mapOfResult.put(dictionaryCategoryDo.getUid(), treeNode);
        }

        //
        // Step 2, ???????????????2??????????????????????????????????????????????????????
        //
        // ?????????????????????dictionary category?????????dictionary content??????????????????dictionary content???????????????
        List<DictionaryContentDo> listOfDictionaryContentDo =
                this.dictionaryContentRepository.findByDictionaryCategoryUidIn(listOfDictionaryCategoryUid);
        if (CollectionUtils.isEmpty(listOfDictionaryContentDo)) {
            return result;
        }
        // ??????parent uid??????????????????by dictionary category uid, ??????dictionary content
        Map<Long, List<DictionaryContentDo>> mapOfDictionaryContentWithParent = new HashMap<>(50);
        // ??????parent uid??????????????????by dictionary category uid, ??????dictionary content
        Map<Long, List<DictionaryContentDo>> mapOfDictionaryContentWithoutParent = new HashMap<>(300);
        for (DictionaryContentDo dictionaryContentDo : listOfDictionaryContentDo) {
            Long dictionaryCategoryUid = dictionaryContentDo.getDictionaryCategoryUid();
            Long parentId = dictionaryContentDo.getParentUid();
            if (parentId == null) {
                if (!mapOfDictionaryContentWithoutParent.containsKey(dictionaryCategoryUid)) {
                    mapOfDictionaryContentWithoutParent.put(dictionaryCategoryUid, new ArrayList<>());
                }
                mapOfDictionaryContentWithoutParent.get(dictionaryCategoryUid).add(dictionaryContentDo);
            } else {
                if (!mapOfDictionaryContentWithParent.containsKey(dictionaryCategoryUid)) {
                    mapOfDictionaryContentWithParent.put(dictionaryCategoryUid, new ArrayList<>());
                }
                mapOfDictionaryContentWithParent.get(dictionaryCategoryUid).add(dictionaryContentDo);
            }
        }

        // ????????????????????????????????????????????????????????????key - dictionary content uid
        Map<Long, SimpleTreeNode> mapOfDictionaryContentTreeNode = new HashMap<>(1000);

        //
        // Step 2.1, ???????????????2??????????????????????????????????????????????????????
        //
        for (Map.Entry<Long, List<DictionaryContentDo>> entry : mapOfDictionaryContentWithoutParent.entrySet()) {
            Long dictionaryCategoryUid = entry.getKey();

            SimpleTreeNode dictionaryCategoryTreeNode = mapOfResult.get(dictionaryCategoryUid);
            dictionaryCategoryTreeNode.setChildren(new ArrayList<>());

            for (DictionaryContentDo dictionaryContentDo : entry.getValue()) {
                SimpleTreeNode dictionaryContentTreeNode = new SimpleTreeNode();
                dictionaryContentTreeNode.setUidCode(dictionaryContentDo.getUid());
                dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
                dictionaryContentTreeNode.setChildren(null);

                dictionaryCategoryTreeNode.getChildren().add(dictionaryContentTreeNode);

                mapOfDictionaryContentTreeNode.put(dictionaryContentDo.getUid(), dictionaryContentTreeNode);
            }
        }

        //
        // Step 2.2, ???????????????3?????????????????????????????????????????????????????????????????????
        //
        for (Map.Entry<Long, List<DictionaryContentDo>> entry : mapOfDictionaryContentWithParent.entrySet()) {
            for (DictionaryContentDo dictionaryContentDo : entry.getValue()) {
                //
                // Step 2.2.1?????????????????????????????????
                //
                SimpleTreeNode dictionaryContentTreeNode;
                if (mapOfDictionaryContentTreeNode.containsKey(dictionaryContentDo.getUid())) {
                    // ????????????????????????????????????????????????children???????????????????????????????????????????????????????????????
                    // ??????????????????????????????????????????children?????????
                    dictionaryContentTreeNode =
                            mapOfDictionaryContentTreeNode.get(dictionaryContentDo.getUid());
                    dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                    dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                    dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
                } else {
                    // ?????????????????????????????????????????????
                    dictionaryContentTreeNode = new SimpleTreeNode();
                    dictionaryContentTreeNode.setUidCode(dictionaryContentDo.getUid());
                    dictionaryContentTreeNode.setName(dictionaryContentDo.getValue());
                    dictionaryContentTreeNode.setDescription(dictionaryContentDo.getLabel());
                    dictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);
                    dictionaryContentTreeNode.setChildren(null);

                    mapOfDictionaryContentTreeNode.put(dictionaryContentTreeNode.getUidCode(),
                            dictionaryContentTreeNode);
                }

                //
                // Step 2.2.2, ??????????????????????????????????????????
                //
                if (mapOfDictionaryContentTreeNode.containsKey(dictionaryContentDo.getParentUid())) {
                    // ??????????????????????????????
                    SimpleTreeNode parentDictionaryContentTreeNode =
                            mapOfDictionaryContentTreeNode.get(dictionaryContentDo.getParentUid());
                    if (CollectionUtils.isEmpty(parentDictionaryContentTreeNode.getChildren())) {
                        parentDictionaryContentTreeNode.setChildren(new ArrayList<>());
                    }
                    parentDictionaryContentTreeNode.getChildren().add(dictionaryContentTreeNode);
                } else {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    SimpleTreeNode shadowParentDictionaryContentTreeNode = new SimpleTreeNode();
                    shadowParentDictionaryContentTreeNode.setUidCode(dictionaryContentDo.getParentUid());
                    // ??????????????????uid?????????????????????????????????
                    shadowParentDictionaryContentTreeNode.setName(null);
                    // ??????????????????uid?????????????????????????????????
                    shadowParentDictionaryContentTreeNode.setDescription(null);
                    shadowParentDictionaryContentTreeNode.setType(DictionaryContentDo.TYPE);

                    mapOfDictionaryContentTreeNode.put(shadowParentDictionaryContentTreeNode.getUidCode(),
                            shadowParentDictionaryContentTreeNode);

                    // ????????????????????????????????????
                    shadowParentDictionaryContentTreeNode.setChildren(new ArrayList<>());
                    shadowParentDictionaryContentTreeNode.getChildren().add(dictionaryContentTreeNode);
                }
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryContentDto createDictionaryContent(
            CreateDictionaryContentDto createDictionaryContentDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ??????????????????
        // Step 1.1, ??????????????????dictionary category????????????????????????name????????????
        boolean existsDuplicate;
        if (createDictionaryContentDto.getParentUid() == null) {
            existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndValue(
                    createDictionaryContentDto.getDictionaryCategoryUid(),
                    createDictionaryContentDto.getValue());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                        createDictionaryContentDto.getDictionaryCategoryUid() + ", " +
                        createDictionaryContentDto.getValue());
            }
            if (!Strings.isNullOrEmpty(createDictionaryContentDto.getLabel())) {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndLabel(
                        createDictionaryContentDto.getDictionaryCategoryUid(),
                        createDictionaryContentDto.getLabel());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            createDictionaryContentDto.getDictionaryCategoryUid() + ", " +
                            createDictionaryContentDto.getLabel());
                }
            }
        } else {
            existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndParentUidAndValue(
                    createDictionaryContentDto.getDictionaryCategoryUid(),
                    createDictionaryContentDto.getParentUid(),
                    createDictionaryContentDto.getValue());
            if (existsDuplicate) {
                throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                        createDictionaryContentDto.getDictionaryCategoryUid() + ", " +
                        createDictionaryContentDto.getParentUid() + ", " +
                        createDictionaryContentDto.getValue());
            }
            if (!Strings.isNullOrEmpty(createDictionaryContentDto.getLabel())) {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndParentUidAndLabel(
                        createDictionaryContentDto.getDictionaryCategoryUid(),
                        createDictionaryContentDto.getParentUid(),
                        createDictionaryContentDto.getLabel());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            createDictionaryContentDto.getDictionaryCategoryUid() + ", " +
                            createDictionaryContentDto.getParentUid() + ", " +
                            createDictionaryContentDto.getLabel());
                }
            }
        }

        // Step 2, ????????????
        DictionaryContentDo dictionaryContentDo = new DictionaryContentDo();
        dictionaryContentDo.setDictionaryCategoryUid(createDictionaryContentDto.getDictionaryCategoryUid());
        dictionaryContentDo.setParentUid(createDictionaryContentDto.getParentUid());
        dictionaryContentDo.setUid(this.singleInstanceUidGenerator.generateUid(DictionaryContentDo.RESOURCE_NAME));
        dictionaryContentDo.setValue(createDictionaryContentDto.getValue());
        if (Strings.isNullOrEmpty(createDictionaryContentDto.getLabel())) {
            dictionaryContentDo.setLabel(createDictionaryContentDto.getValue());
        } else {
            dictionaryContentDo.setLabel(createDictionaryContentDto.getLabel());
        }
        BaseDo.create(dictionaryContentDo, operationUserInfo.getUsername(), new Date());
        this.dictionaryContentRepository.save(dictionaryContentDo);

        // Step 3, ??????????????????
        DictionaryContentDto dictionaryContentDto = new DictionaryContentDto();
        dictionaryContentDto.setDictionaryStructureUidCode(dictionaryContentDo.getDictionaryStructureUid());
        dictionaryContentDto.setDictionaryCategoryUidCode(dictionaryContentDo.getDictionaryCategoryUid());
        dictionaryContentDto.setUidCode(dictionaryContentDo.getUid());
        dictionaryContentDto.setValue(dictionaryContentDo.getValue());
        dictionaryContentDto.setLabel(dictionaryContentDo.getLabel());
        dictionaryContentDto.setParentUidCode(dictionaryContentDo.getParentUid());

        return dictionaryContentDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public DictionaryContentDto updateDictionaryContent(
            UpdateDictionaryContentDto updateDictionaryContentDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????
        DictionaryContentDo dictionaryContentDo =
                this.dictionaryContentRepository.findByUid(updateDictionaryContentDto.getUid());
        if (dictionaryContentDo == null) {
            throw new ResourceNotFoundException(DictionaryContentDo.RESOURCE_NAME + ":" +
                    updateDictionaryContentDto.getUid());
        }

        // Step 2, ??????????????????
        // Step 2.1, ??????????????????value/label????????????????????????value/label??????
        boolean existsDuplicate;
        if (!Strings.isNullOrEmpty(updateDictionaryContentDto.getValue()) &&
                !updateDictionaryContentDto.getValue().equalsIgnoreCase(dictionaryContentDo.getValue())) {
            if (dictionaryContentDo.getParentUid() == null) {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndValue(
                        dictionaryContentDo.getDictionaryCategoryUid(),
                        updateDictionaryContentDto.getValue());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            dictionaryContentDo.getDictionaryCategoryUid() + ", " +
                            updateDictionaryContentDto.getValue());
                }
            } else {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndParentUidAndValue(
                        dictionaryContentDo.getDictionaryCategoryUid(),
                        dictionaryContentDo.getParentUid(),
                        updateDictionaryContentDto.getValue());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            dictionaryContentDo.getDictionaryCategoryUid() + ", " +
                            dictionaryContentDo.getParentUid() + ", " +
                            updateDictionaryContentDto.getValue());
                }
            }
        }
        if (!Strings.isNullOrEmpty(updateDictionaryContentDto.getLabel()) &&
                !updateDictionaryContentDto.getLabel().equalsIgnoreCase(dictionaryContentDo.getLabel())) {
            if (dictionaryContentDo.getParentUid() == null) {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndLabel(
                        dictionaryContentDo.getDictionaryCategoryUid(),
                        updateDictionaryContentDto.getLabel());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            dictionaryContentDo.getDictionaryCategoryUid() + ", " +
                            updateDictionaryContentDto.getLabel());
                }
            } else {
                existsDuplicate = this.dictionaryContentRepository.existsByDictionaryCategoryUidAndParentUidAndLabel(
                        dictionaryContentDo.getDictionaryCategoryUid(),
                        dictionaryContentDo.getParentUid(),
                        updateDictionaryContentDto.getLabel());
                if (existsDuplicate) {
                    throw new ResourceDuplicateException(DictionaryContentDo.RESOURCE_NAME + ":" +
                            dictionaryContentDo.getDictionaryCategoryUid() + ", " +
                            dictionaryContentDo.getParentUid() + ", " +
                            updateDictionaryContentDto.getLabel());
                }
            }
        }

        // Step 3, ????????????
        boolean requiredToUpdate = false;
        if (!Strings.isNullOrEmpty(updateDictionaryContentDto.getValue()) &&
                !updateDictionaryContentDto.getValue().equalsIgnoreCase(dictionaryContentDo.getValue())) {
            dictionaryContentDo.setValue(updateDictionaryContentDto.getValue());
            requiredToUpdate = true;
        }
        if (!Strings.isNullOrEmpty(updateDictionaryContentDto.getLabel()) &&
                !updateDictionaryContentDto.getLabel().equalsIgnoreCase(dictionaryContentDo.getLabel())) {
            dictionaryContentDo.setLabel(updateDictionaryContentDto.getLabel());
            requiredToUpdate = true;
        }
        if (requiredToUpdate) {
            BaseDo.update(dictionaryContentDo, operationUserInfo.getUsername(), new Date());
            this.dictionaryContentRepository.save(dictionaryContentDo);
        }

        // Step 4, ??????????????????
        DictionaryContentDto dictionaryContentDto = new DictionaryContentDto();
        dictionaryContentDto.setDictionaryStructureUidCode(dictionaryContentDo.getDictionaryStructureUid());
        dictionaryContentDto.setDictionaryCategoryUidCode(dictionaryContentDo.getDictionaryCategoryUid());
        dictionaryContentDto.setUidCode(dictionaryContentDo.getUid());
        dictionaryContentDto.setValue(dictionaryContentDo.getValue());
        dictionaryContentDto.setLabel(dictionaryContentDo.getLabel());
        dictionaryContentDto.setParentUidCode(dictionaryContentDo.getParentUid());

        return dictionaryContentDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictionaryContent(Long uid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ??????????????????
        boolean exists =
                this.dictionaryContentRepository.existsByUid(uid);
        if (!exists) {
            // work around: ?????????????????????dictionary content ???build????????????????????????????????????????????????????????????????????????????????????dictionary content????????????????????????????????????
            return;
            // throw new ResourceNotFoundException(DictionaryContentDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 2, ??????????????????????????????
        boolean existsChildren = this.dictionaryContentRepository.existsByParentUid(uid);
        if (existsChildren) {
            throw new ResourceInUseException(DictionaryContentDo.RESOURCE_NAME + ":" + uid);
        }

        // Step 3, ????????????
        this.dictionaryContentRepository.deleteByUid(uid);
    }
}
