package io.g740.pigeon.biz.service.handler.admin;

import com.google.common.base.Strings;
import io.g740.commons.constants.CsvExportStrategyEnum;
import io.g740.commons.exception.impl.ResourceConflictException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.object.dto.defaults.DefaultsCategoryDto;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.dictionary.DictionaryCategoryDto;
import io.g740.pigeon.biz.object.dto.ds.DataFieldDto;
import io.g740.pigeon.biz.object.dto.ds.IndexDto;
import io.g740.pigeon.biz.object.entity.df.*;
import io.g740.pigeon.biz.object.entity.ds.DsDataObjectDo;
import io.g740.pigeon.biz.object.entity.ds.DsDo;
import io.g740.pigeon.biz.persistence.jpa.df.*;
import io.g740.pigeon.biz.persistence.jpa.ds.DsDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.pigeon.biz.share.constants.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author bbottong
 */
@Handler
public class DfConfHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfConfHandler.class);

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

    @Autowired
    private DefaultsHandler defaultsHandler;

    public boolean existsDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        return this.dfConfDataFieldRepository.existsByDfUid(dfUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void initDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        DfDo dfDo = this.dfRepository.findByUid(dfUid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfUid);
        }

        // Step 1,
        // ???df?????????????????????/?????????
        // ???????????????/???????????????processing logic?????????
        // ?????????????????????ds????????????/????????????processing logic?????????
        // ??????????????????????????????????????????????????????????????????
        List<DfAvailableDataFieldDo> listOfDfAvailableDataFieldDo = loadDfAvailableDataFields(dfUid, operationUserInfo);
        if (listOfDfAvailableDataFieldDo == null) {
            return;
        }
        // ???????????????????????????????????????
        Map<String, List<IndexDto>> indexDtoMap = new HashMap<>();
        List<IndexDto> indexDtoList = loadIndexes(dfUid, operationUserInfo);
        if (CollectionUtils.isNotEmpty(indexDtoList)) {
            indexDtoList.forEach(indexDto -> {
                indexDto.getColumns().forEach(indexColumnDto -> {
                    if (!indexDtoMap.containsKey(indexColumnDto.getColumnName())) {
                        indexDtoMap.put(indexColumnDto.getColumnName(), new ArrayList<>());
                    }
                    indexDtoMap.get(indexColumnDto.getColumnName()).add(indexDto);
                });
            });
        }

        // ???????????????????????????????????????form element????????????????????????????????????????????????1/4
        int thresholdEnabledAsFormElement = listOfDfAvailableDataFieldDo.size() / 4;
        thresholdEnabledAsFormElement = (thresholdEnabledAsFormElement == 0 ? listOfDfAvailableDataFieldDo.size() : thresholdEnabledAsFormElement);
        int totalEnabledAsFormElement = 0;
        Date now = new Date();
        List<DfConfDataFieldDo> listOfDfConfDataFieldDo = new ArrayList<>();
        for (int i = 0; i < listOfDfAvailableDataFieldDo.size(); i++) {
            DfAvailableDataFieldDo dfAvailableDataFieldDo = listOfDfAvailableDataFieldDo.get(i);

            DfConfDataFieldDo dfConfDataFieldDo = transform(
                    dfAvailableDataFieldDo, dfUid, now, indexDtoMap, operationUserInfo);

            // ??????form element?????????
            boolean enabledAsFormElement = false;
            switch (dfAvailableDataFieldDo.getFieldType()) {
                case TEXT:
                    if (dfAvailableDataFieldDo.getFieldLength() <= 255) {
                        if (totalEnabledAsFormElement < thresholdEnabledAsFormElement) {
                            enabledAsFormElement = true;
                            totalEnabledAsFormElement++;
                        }
                    }
                    break;
                case DATE:
                case TIMESTAMP:
                    if (totalEnabledAsFormElement < thresholdEnabledAsFormElement) {
                        enabledAsFormElement = true;
                        totalEnabledAsFormElement++;
                    }
                    break;
            }
            dfConfDataFieldDo.setEnabledAsFormElement(enabledAsFormElement);

            listOfDfConfDataFieldDo.add(dfConfDataFieldDo);
        }
        this.dfConfDataFieldRepository.saveAll(listOfDfConfDataFieldDo);

        // Step 2, advanced
        boolean existsDfConfAdvanced = this.dfConfAdvancedRepository.existsByDfUid(dfUid);
        if (!existsDfConfAdvanced) {
            DfConfAdvancedDo dfConfAdvancedDo = new DfConfAdvancedDo();
            dfConfAdvancedDo.setQueryType(QueryTypeEnum.SYNCHRONOUS);
            dfConfAdvancedDo.setExportType(ExportTypeEnum.ASYNCHRONOUS);
            dfConfAdvancedDo.setEnabledPagination(true);
            dfConfAdvancedDo.setDefaultPageSize(100);
            dfConfAdvancedDo.setEnabledColumnNo(true);
            dfConfAdvancedDo.setDfUid(dfUid);
            dfConfAdvancedDo.setEnabledVerticalScrolling(true);
            dfConfAdvancedDo.setVerticalScrollingHeightThreshold(800);
            dfConfAdvancedDo.setEnabledFormFolding(true);
            dfConfAdvancedDo.setEnabledDefaultQuery(true);
            dfConfAdvancedDo.setEnabledDfNameDescription(true);
            dfConfAdvancedDo.setCsvExportStrategy(CsvExportStrategyEnum.DEFAULT);
            BaseDo.create(dfConfAdvancedDo, operationUserInfo.getUsername(), now);
            this.dfConfAdvancedRepository.save(dfConfAdvancedDo);
        }
    }

    private DfConfDataFieldDo transform(
            DfAvailableDataFieldDo dfAvailableDataFieldDo,
            Long dfUid,
            Date timestamp,
            Map<String, List<IndexDto>> indexDtoMap,
            UserInfo operationUserInfo) {
        DfConfDataFieldDo dfConfDataFieldDo = new DfConfDataFieldDo();
        dfConfDataFieldDo.setFieldName(dfAvailableDataFieldDo.getFieldName());
        dfConfDataFieldDo.setFieldType(dfAvailableDataFieldDo.getFieldType());
        dfConfDataFieldDo.setFieldLength(dfAvailableDataFieldDo.getFieldLength());
        dfConfDataFieldDo.setFieldDescription(dfAvailableDataFieldDo.getFieldDescription());
        // field label ???????????????field description, ???????????????field name
        if (!Strings.isNullOrEmpty(dfAvailableDataFieldDo.getFieldDescription())) {
            dfConfDataFieldDo.setFieldLabel(dfAvailableDataFieldDo.getFieldDescription());
        } else {
            dfConfDataFieldDo.setFieldLabel(dfAvailableDataFieldDo.getFieldName());
        }
        dfConfDataFieldDo.setOrdinalPosition(dfAvailableDataFieldDo.getOrdinalPosition());

        FormElementTypeEnum formElementType = null;
        switch (dfAvailableDataFieldDo.getFieldType()) {
            case DATE:
                formElementType = FormElementTypeEnum.DATE_RANGE;
                break;
            case TIMESTAMP:
                formElementType = FormElementTypeEnum.TIMESTAMP_RANGE;
                break;
            case TIME:
                formElementType = FormElementTypeEnum.TIME_RANGE;
                break;
            case DECIMAL:
            case INT:
            case LONG:
                formElementType = FormElementTypeEnum.NUMBER_RANGE;
                break;
            default:
                formElementType = FormElementTypeEnum.EXACT_TEXT;
                break;
        }
        dfConfDataFieldDo.setFormElementType(formElementType);
        dfConfDataFieldDo.setFormElementSequence(dfAvailableDataFieldDo.getOrdinalPosition());
        dfConfDataFieldDo.setListElementSequence(dfAvailableDataFieldDo.getOrdinalPosition());
        dfConfDataFieldDo.setEnabledAsListElement(true);
        switch (dfAvailableDataFieldDo.getFieldType()) {
            case TEXT:
                if (dfAvailableDataFieldDo.getFieldLength() <= 50) {
                    dfConfDataFieldDo.setListElementWidth(90);
                } else if (dfAvailableDataFieldDo.getFieldLength() <= 100) {
                    dfConfDataFieldDo.setListElementWidth(120);
                } else if (dfAvailableDataFieldDo.getFieldLength() <= 150) {
                    dfConfDataFieldDo.setListElementWidth(150);
                } else if (dfAvailableDataFieldDo.getFieldLength() <= 200) {
                    dfConfDataFieldDo.setListElementWidth(200);
                } else {
                    dfConfDataFieldDo.setListElementWidth(300);
                }
                break;
            case TIMESTAMP:
                dfConfDataFieldDo.setListElementWidth(150);
                break;
            case DATE:
            case TIME:
                dfConfDataFieldDo.setListElementWidth(120);
            case BOOLEAN:
                dfConfDataFieldDo.setListElementWidth(70);
                break;
            default:
                dfConfDataFieldDo.setListElementWidth(120);
                break;
        }

        // ??????sort
        if (indexDtoMap.containsKey(dfConfDataFieldDo.getFieldName())) {
            List<IndexDto> indexDtoList = indexDtoMap.get(dfConfDataFieldDo.getFieldName());

            for (IndexDto indexDto : indexDtoList) {
                if (Boolean.TRUE.equals(indexDto.getUnique())) {
                    dfConfDataFieldDo.setSortForced(true);
                    break;
                }
            }
            dfConfDataFieldDo.setEnabledAsSortElement(true);
            dfConfDataFieldDo.setSortDirection(Sort.Direction.ASC);
            dfConfDataFieldDo.setSortElementSequence(dfConfDataFieldDo.getListElementSequence());
        }

        // ??????group by
        switch (dfAvailableDataFieldDo.getFieldType()) {
            case TEXT:
                dfConfDataFieldDo.setRole(DataFieldRoleEnum.DIMENSION);
                dfConfDataFieldDo.setAggregationType(null);
                break;
            case INT:
            case LONG:
            case DECIMAL:
                dfConfDataFieldDo.setRole(DataFieldRoleEnum.KPI);
                dfConfDataFieldDo.setAggregationType(DataFieldAggregationTypeEnum.SUM);
                break;
            default:
                dfConfDataFieldDo.setRole(null);
                dfConfDataFieldDo.setAggregationType(null);
                break;
        }

        // ??????data permission
        dfConfDataFieldDo.setResourceCategoryUid(null);
        dfConfDataFieldDo.setResourceCategoryName(null);
        dfConfDataFieldDo.setResourceStructureUid(null);
        dfConfDataFieldDo.setResourceStructureName(null);

        dfConfDataFieldDo.setDfUid(dfUid);

        BaseDo.create(dfConfDataFieldDo, operationUserInfo.getUsername(), timestamp);

        return dfConfDataFieldDo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void reinitDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        DfDo dfDo = this.dfRepository.findByUid(dfUid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfUid);
        }

        // Step 1, ???basic??????
        List<DfConfDataFieldDo> listOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findByDfUid(dfUid);
        if (listOfDfConfDataFieldDo == null) {
            listOfDfConfDataFieldDo = new ArrayList<>();
        }

        // Step 2,
        // ???df?????????????????????/?????????
        // ???????????????/???????????????processing logic?????????
        // ?????????????????????ds????????????/????????????processing logic?????????
        // ??????????????????????????????????????????????????????????????????
        List<DfAvailableDataFieldDo> listOfDfAvailableDataFieldDo = loadDfAvailableDataFields(dfUid, operationUserInfo);
        if (listOfDfAvailableDataFieldDo == null) {
            listOfDfAvailableDataFieldDo = new ArrayList<>();
        }
        // ???indexes
        Map<String, List<IndexDto>> indexDtoMap = new HashMap<>();
        List<IndexDto> indexDtoList = loadIndexes(dfUid, operationUserInfo);
        if (CollectionUtils.isNotEmpty(indexDtoList)) {
            indexDtoList.forEach(indexDto -> {
                indexDto.getColumns().forEach(indexColumnDto -> {
                    if (!indexDtoMap.containsKey(indexColumnDto.getColumnName())) {
                        indexDtoMap.put(indexColumnDto.getColumnName(), new ArrayList<>());
                    }
                    indexDtoMap.get(indexColumnDto.getColumnName()).add(indexDto);
                });
            });
        }

        // Step 3, ????????????df????????????df????????????????????????????????????????????????"??????/??????/??????"???
        // 3????????????
        // 1???input??????existing???????????????DF?????????????????????????????????????????????/????????????
        // 2) input??????existing???????????????DF?????????????????????????????????????????????/????????????
        // 3) input?????????existing????????????DF????????????????????????????????????????????????/??????????????????????????????/?????????????????????

        Map<String, DfConfDataFieldDo> mapOfExisting = new HashedMap();
        listOfDfConfDataFieldDo.forEach(field -> {
            mapOfExisting.put(field.getFieldName(), field);
        });

        Map<String, DfAvailableDataFieldDo> mapOfInput = new HashedMap();
        listOfDfAvailableDataFieldDo.forEach(dfAvailableDataFieldDo -> {
            mapOfInput.put(dfAvailableDataFieldDo.getFieldName(), dfAvailableDataFieldDo);
        });

        Date now = new Date();

        List<DfConfDataFieldDo> toSaveListOfDfConfDataFieldDo = new ArrayList<>();

        for (Map.Entry<String, DfAvailableDataFieldDo> input : mapOfInput.entrySet()) {
            if (mapOfExisting.containsKey(input.getKey())) {
                // ???1????????????input??????existing??????
                DfAvailableDataFieldDo inputDataField = input.getValue();
                DfConfDataFieldDo existingDataField = mapOfExisting.get(input.getKey());
                boolean requiredToUpdate = false;
                if (inputDataField.getFieldLength() != null &&
                        !inputDataField.getFieldLength().equals(existingDataField.getFieldLength())) {
                    existingDataField.setFieldLength(inputDataField.getFieldLength());
                    requiredToUpdate = true;
                }

                if (inputDataField.getFieldType() != null &&
                        !inputDataField.getFieldType().equals(existingDataField.getFieldType())) {
                    existingDataField.setFieldType(inputDataField.getFieldType());
                    requiredToUpdate = true;
                }

                if (inputDataField.getFieldDescription() != null &&
                        !inputDataField.getFieldDescription().equals(existingDataField.getFieldDescription())) {
                    existingDataField.setFieldDescription(inputDataField.getFieldDescription());
                    requiredToUpdate = true;
                }

                // field label ??????????????? field description??????????????????field name
                if (inputDataField.getFieldDescription() != null) {
                    if (!inputDataField.getFieldDescription().equals(existingDataField.getFieldLabel())) {
                        existingDataField.setFieldLabel(inputDataField.getFieldDescription());
                        requiredToUpdate = true;
                    }
                } else {
                    if (!inputDataField.getFieldName().equalsIgnoreCase(existingDataField.getFieldLabel())) {
                        existingDataField.setFieldLabel(inputDataField.getFieldName());
                        requiredToUpdate = true;
                    }
                }

                // ??????sort
                if (indexDtoMap.containsKey(existingDataField.getFieldName())) {
                    List<IndexDto> fieldIndexDtoList = indexDtoMap.get(existingDataField.getFieldName());

                    for (IndexDto indexDto : fieldIndexDtoList) {
                        if (Boolean.TRUE.equals(indexDto.getUnique())) {
                            if (!Boolean.TRUE.equals(existingDataField.getSortForced())) {
                                existingDataField.setSortForced(true);
                                requiredToUpdate = true;
                            }
                            break;
                        }
                    }
                    if (!Boolean.TRUE.equals(existingDataField.getEnabledAsSortElement())) {
                        existingDataField.setEnabledAsSortElement(true);
                        existingDataField.setSortDirection(Sort.Direction.ASC);
                        existingDataField.setSortElementSequence(existingDataField.getListElementSequence());
                        requiredToUpdate = true;
                    }
                }

                if (requiredToUpdate) {
                    BaseDo.update(existingDataField, operationUserInfo.getUsername(), now);
                    toSaveListOfDfConfDataFieldDo.add(existingDataField);
                }
            } else {
                // ???2????????????input??????existing??????
                DfAvailableDataFieldDo dfAvailableDataFieldDo = input.getValue();
                DfConfDataFieldDo dfConfDataFieldDo = transform(dfAvailableDataFieldDo, dfUid, now, indexDtoMap, operationUserInfo);

                // ??????form element?????????
                boolean enabledAsFormElement = false;
                switch (dfAvailableDataFieldDo.getFieldType()) {
                    case TEXT:
                        if (dfAvailableDataFieldDo.getFieldLength() <= 255) {
                            enabledAsFormElement = true;
                        }
                        break;
                    case DATE:
                    case TIMESTAMP:
                        enabledAsFormElement = true;
                        break;
                }
                dfConfDataFieldDo.setEnabledAsFormElement(enabledAsFormElement);

                toSaveListOfDfConfDataFieldDo.add(dfConfDataFieldDo);
            }
        }

        for (Map.Entry<String, DfConfDataFieldDo> existing : mapOfExisting.entrySet()) {
            if (!mapOfInput.containsKey(existing.getKey())) {
                // ???3????????????input?????????existing???
                DfConfDataFieldDo dfConfDataFieldDo = existing.getValue();
                dfConfDataFieldDo.setDeleted(true);
                BaseDo.update(dfConfDataFieldDo, operationUserInfo.getUsername(), now);
                toSaveListOfDfConfDataFieldDo.add(dfConfDataFieldDo);
            }
        }

        if (CollectionUtils.isNotEmpty(toSaveListOfDfConfDataFieldDo)) {
            this.dfConfDataFieldRepository.saveAll(toSaveListOfDfConfDataFieldDo);
        }
    }

    public DfConfDto getDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        DfDo dfDo = this.dfRepository.findByUid(dfUid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfUid);
        }
        DfConfDto result = new DfConfDto();
        result.setDfUid(dfUid);
        result.setDfKey(dfDo.getKey());
        result.setBasic(new DfConfBasicDto());
        result.getBasic().setFields(new ArrayList<>());

        // Step 1, ??????df?????????
        // advanced??????
        DfConfAdvancedDo dfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(dfUid);
        if (dfConfAdvancedDo != null) {
            DfConfAdvancedDto dfConfAdvancedDto = new DfConfAdvancedDto();
            BeanUtils.copyProperties(dfConfAdvancedDo, dfConfAdvancedDto);
            result.setAdvanced(dfConfAdvancedDto);
        }
        // basic??????
        List<DfConfDataFieldDo> listOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findByDfUid(dfUid);
        if (listOfDfConfDataFieldDo == null) {
            listOfDfConfDataFieldDo = new ArrayList<>();
        }

        // Step 2,
        // ???df?????????????????????/?????????
        // ???????????????/???????????????processing logic?????????
        // ?????????????????????ds????????????/????????????processing logic?????????
        // ??????????????????????????????????????????????????????????????????
        List<DfAvailableDataFieldDo> listOfDfAvailableDataFieldDo = loadDfAvailableDataFields(dfUid, operationUserInfo);
        if (listOfDfAvailableDataFieldDo == null) {
            listOfDfAvailableDataFieldDo = new ArrayList<>();
        }

        // Step 3, ????????????df????????????df????????????????????????????????????????????????"??????/????????????????????????/??????"???
        // 3????????????
        // 1???input??????existing???????????????DF?????????????????????????????????????????????/????????????
        // 2) input??????existing???????????????DF?????????????????????????????????????????????/????????????
        // 3) input?????????existing????????????DF????????????????????????????????????????????????/??????????????????????????????/?????????????????????

        Map<String, DfConfDataFieldDo> mapOfExisting = new HashedMap();
        listOfDfConfDataFieldDo.forEach(field -> {
            mapOfExisting.put(field.getFieldName(), field);
        });

        Map<String, DfAvailableDataFieldDo> mapOfInput = new HashedMap();
        listOfDfAvailableDataFieldDo.forEach(dfAvailableDataFieldDo -> {
            mapOfInput.put(dfAvailableDataFieldDo.getFieldName(), dfAvailableDataFieldDo);
        });

        for (Map.Entry<String, DfAvailableDataFieldDo> input : mapOfInput.entrySet()) {
            DfConfDataFieldDto dfConfDataFieldDto = new DfConfDataFieldDto();

            if (mapOfExisting.containsKey(input.getKey())) {
                // ???1????????????input??????existing??????
                BeanUtils.copyProperties(mapOfExisting.get(input.getKey()), dfConfDataFieldDto);
                // ??????ordinal position
                if (dfConfDataFieldDto.getOrdinalPosition() == null) {
                    dfConfDataFieldDto.setOrdinalPosition(input.getValue().getOrdinalPosition());
                }

                // ??????dictionary category name
                if (mapOfExisting.get(input.getKey()).getDictionaryCategoryUid() != null) {
                    try {
                        DictionaryCategoryDto dictionaryCategoryDto =
                                this.dictionaryHandler.getDictionaryCategory(mapOfExisting.get(input.getKey()).getDictionaryCategoryUid());
                        dfConfDataFieldDto.setDictionaryCategoryName(dictionaryCategoryDto.getName());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
                // ??????defaults category name
                if (mapOfExisting.get(input.getKey()).getDefaultsCategoryUid() != null) {
                    try {
                        DefaultsCategoryDto defaultsCategoryDto = this.defaultsHandler.getDefaultsCategory(mapOfExisting.get(input.getKey()).getDefaultsCategoryUid());
                        dfConfDataFieldDto.setDefaultsCategoryName(defaultsCategoryDto.getName());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
                // ????????????????????????/?????????
                dfConfDataFieldDto.setEffective(true);
            } else {
                // ???2????????????input??????existing??????
                dfConfDataFieldDto.setFieldName(input.getValue().getFieldName());
                dfConfDataFieldDto.setFieldType(input.getValue().getFieldType());
                dfConfDataFieldDto.setFieldLength(input.getValue().getFieldLength());
                dfConfDataFieldDto.setFieldDescription(input.getValue().getFieldDescription());
                if (!Strings.isNullOrEmpty(dfConfDataFieldDto.getFieldDescription())) {
                    dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldDescription());
                } else {
                    dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldName());
                }

                // ????????????????????????/?????????
                dfConfDataFieldDto.setEffective(true);
                dfConfDataFieldDto.setOrdinalPosition(input.getValue().getOrdinalPosition());
            }

            result.getBasic().getFields().add(dfConfDataFieldDto);
        }

        for (Map.Entry<String, DfConfDataFieldDo> existing : mapOfExisting.entrySet()) {
            if (!mapOfInput.containsKey(existing.getKey())) {
                // ???3????????????input?????????existing???
                DfConfDataFieldDto dfConfDataFieldDto = new DfConfDataFieldDto();
                BeanUtils.copyProperties(existing.getValue(), dfConfDataFieldDto);

                // ??????dictionary category name
                if (existing.getValue().getDictionaryCategoryUid() != null) {
                    try {
                        DictionaryCategoryDto dictionaryCategoryDto =
                                this.dictionaryHandler.getDictionaryCategory(existing.getValue().getDictionaryCategoryUid());
                        dfConfDataFieldDto.setDictionaryCategoryName(dictionaryCategoryDto.getName());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
                // ??????defaults category name
                if (existing.getValue().getDefaultsCategoryUid() != null) {
                    try {
                        DefaultsCategoryDto defaultsCategoryDto = this.defaultsHandler.getDefaultsCategory(existing.getValue().getDefaultsCategoryUid());
                        dfConfDataFieldDto.setDefaultsCategoryName(defaultsCategoryDto.getName());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }
                // ???????????????????????????/?????????
                dfConfDataFieldDto.setEffective(false);
            }
        }

        // ???????????? data fields ??? ordinal position ???????????????????????? data fields ??? field name ???????????????
        result.getBasic().getFields().sort(new Comparator<DfConfDataFieldDto>() {
            @Override
            public int compare(DfConfDataFieldDto o1, DfConfDataFieldDto o2) {
                // return o1.getFieldName().compareTo(o2.getFieldName());
                if (o1.getOrdinalPosition() != null && o2.getOrdinalPosition() != null) {
                    return o1.getOrdinalPosition().compareTo(o2.getOrdinalPosition());
                }

                return o1.getFieldName().compareTo(o2.getFieldName());
            }
        });

        return result;
    }

    public PageResult<DfConfDataFieldDto> getDfConfBasic(
            Long dfUid, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        PageResult<DfConfDataFieldDto> pageResult = new PageResult<>();

        Page<DfAvailableDataFieldDo> pageOfDfDataFieldDo = this.dfAvailableDataFieldRepository.findByDfUid(dfUid, pageable);
        pageResult.setNumberOfElements(pageOfDfDataFieldDo.getNumberOfElements());
        pageResult.setPageNumber(pageOfDfDataFieldDo.getNumber());
        pageResult.setPageSize(pageOfDfDataFieldDo.getSize());
        pageResult.setTotalElements(pageOfDfDataFieldDo.getTotalElements());
        pageResult.setTotalPages(pageOfDfDataFieldDo.getTotalPages());

        if (pageOfDfDataFieldDo.hasContent()) {
            List<DfConfDataFieldDo> listOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findByDfUid(dfUid);
            if (CollectionUtils.isEmpty(listOfDfConfDataFieldDo)) {
                pageOfDfDataFieldDo.getContent().forEach(dfAvailableDataFieldDo -> {
                    DfConfDataFieldDto dfConfDataFieldDto = new DfConfDataFieldDto();
                    dfConfDataFieldDto.setFieldName(dfAvailableDataFieldDo.getFieldName());
                    dfConfDataFieldDto.setFieldType(dfAvailableDataFieldDo.getFieldType());
                    dfConfDataFieldDto.setFieldLength(dfAvailableDataFieldDo.getFieldLength());
                    dfConfDataFieldDto.setFieldDescription(dfAvailableDataFieldDo.getFieldDescription());

                    if (!Strings.isNullOrEmpty(dfConfDataFieldDto.getFieldDescription())) {
                        dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldDescription());
                    } else {
                        dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldName());
                    }

                    pageResult.getContent().add(dfConfDataFieldDto);
                });
            } else {
                Map<String, DfConfDataFieldDo> mapOfExisting = new HashedMap();
                listOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                    mapOfExisting.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
                });

                pageOfDfDataFieldDo.getContent().forEach(dfAvailableDataFieldDo -> {
                    DfConfDataFieldDto dfConfDataFieldDto = new DfConfDataFieldDto();
                    if (mapOfExisting.containsKey(dfAvailableDataFieldDo.getFieldName())) {
                        BeanUtils.copyProperties(mapOfExisting.get(dfAvailableDataFieldDo.getFieldName()), dfConfDataFieldDto);
                    } else {
                        dfConfDataFieldDto.setFieldName(dfAvailableDataFieldDo.getFieldName());
                        dfConfDataFieldDto.setFieldType(dfAvailableDataFieldDo.getFieldType());
                        dfConfDataFieldDto.setFieldLength(dfAvailableDataFieldDo.getFieldLength());
                        dfConfDataFieldDto.setFieldDescription(dfAvailableDataFieldDo.getFieldDescription());

                        if (!Strings.isNullOrEmpty(dfConfDataFieldDto.getFieldDescription())) {
                            dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldDescription());
                        } else {
                            dfConfDataFieldDto.setFieldLabel(dfConfDataFieldDto.getFieldName());
                        }
                    }
                    pageResult.getContent().add(dfConfDataFieldDto);
                });

            }
        }

        // ??? data fields ??? field name ???????????????
        pageResult.getContent().sort(new Comparator<DfConfDataFieldDto>() {
            @Override
            public int compare(DfConfDataFieldDto o1, DfConfDataFieldDto o2) {
                return o1.getFieldName().compareTo(o2.getFieldName());
            }
        });

        return pageResult;
    }

    public DfConfAdvancedDto getDfConfAdvanced(
            Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ???????????????df??????
        DfConfAdvancedDo dfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(dfUid);
        if (dfConfAdvancedDo != null) {
            DfConfAdvancedDto dfConfAdvancedDto = new DfConfAdvancedDto();
            BeanUtils.copyProperties(dfConfAdvancedDo, dfConfAdvancedDto);
            return dfConfAdvancedDto;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDfConf(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        this.dfConfAdvancedRepository.deleteByDfUid(dfUid);
        this.dfConfDataFieldRepository.deleteByDfUid(dfUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public DfRefreshResultDto initDfAvailableDataFields(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        return refreshDfAvailableDataFields(dfUid, operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public DfRefreshResultDto refreshDfAvailableDataFields(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        DfRefreshResultDto dfRefreshResultDto = new DfRefreshResultDto();
        dfRefreshResultDto.setDfUid(dfUid);

        // Step 1, ??????df??????
        DfDo dfDo = this.dfRepository.findByUid(dfUid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME);
        }

        // Step 2, ??????df????????????ds???primary data object??????
        List<DfDataObjectDo> listOfPrimaryDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidAndDataObjectDependencyType(dfUid,
                        DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(listOfPrimaryDfDataObjectDo)) {
            throw new ResourceNotFoundException(DfDataObjectDo.RESOURCE_NAME + ":" + dfUid);
        }
        DfDataObjectDo dfDataObjectDo = listOfPrimaryDfDataObjectDo.get(0);
        Long dataObjectUid = dfDataObjectDo.getDataObjectUid();
        DsDataObjectDo dsDataObjectDo = this.dsDataObjectRepository.findByUid(dataObjectUid);
        if (dsDataObjectDo == null) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + dataObjectUid);
        }
        Long dsUid = dsDataObjectDo.getDsUid();
        DsDo dsDo = this.dsRepository.findByUid(dsUid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsUid);
        }

        // Step 3, ???????????????primary data object???
        // ??????primary data object???processing?????????
        // ??????primary data object???secondary data object(s)???processing?????????
        // ???????????????????????????available data fields
        List<DataFieldDto> listOfAvailableDataFieldDto = null;

        if (Boolean.TRUE.equals(dfDo.getProcessingNeeded())) {
            // ??????processing
            if (!Strings.isNullOrEmpty(dfDo.getProcessingLogic())) {
                // ??????processing logic????????????N?????????????????????????????????
                try {
                    listOfAvailableDataFieldDto = this.dsConnectionHandler.loadDataFields(
                            dsDo.getType(),
                            dsDo.getConnectionProfileProps(),
                            dfDo.getProcessingLogic());
                } catch (Exception e) {
                    LOGGER.warn("", e);
                    dfRefreshResultDto.setSuccessful(false);
                    return dfRefreshResultDto;
                }
            } else {
                // data mismatch
                throw new ResourceConflictException(DsDo.RESOURCE_NAME + ":" + dfUid);
            }
        } else {
            // ?????????processing
            try {
                listOfAvailableDataFieldDto = this.dsConnectionHandler.loadDataFields(
                        dsDo.getType(),
                        dsDo.getConnectionProfileProps(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectType(),
                        dsDataObjectDo.getDataObjectName());
            } catch (Exception e) {
                LOGGER.warn("", e);
                dfRefreshResultDto.setSuccessful(false);
                return dfRefreshResultDto;
            }
        }

        // Step 4, ??????df available data fields
        if (CollectionUtils.isNotEmpty(listOfAvailableDataFieldDto)) {
            boolean existsDfAvailableDataField = this.dfAvailableDataFieldRepository.existsByDfUid(dfUid);
            if (existsDfAvailableDataField) {
                this.dfAvailableDataFieldRepository.deleteByDfUid(dfUid);
            }

            Date now = new Date();
            List<DfAvailableDataFieldDo> listOfDfAvailableDataFieldDo = new LinkedList<>();
            for (int i = 0; i < listOfAvailableDataFieldDto.size(); i++) {
                DataFieldDto dataFieldDto = listOfAvailableDataFieldDto.get(i);
                DfAvailableDataFieldDo dfAvailableDataFieldDo = new DfAvailableDataFieldDo();
                BeanUtils.copyProperties(dataFieldDto, dfAvailableDataFieldDo);
                dfAvailableDataFieldDo.setDfUid(dfUid);
                dfAvailableDataFieldDo.setOrdinalPosition(i);
                BaseDo.create(dfAvailableDataFieldDo, operationUserInfo.getUsername(), now);
                listOfDfAvailableDataFieldDo.add(dfAvailableDataFieldDo);
            }

            this.dfAvailableDataFieldRepository.saveAll(listOfDfAvailableDataFieldDo);
        }

        dfRefreshResultDto.setSuccessful(true);
        return dfRefreshResultDto;
    }

    public List<DfAvailableDataFieldDo> loadDfAvailableDataFields(
            Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        return this.dfAvailableDataFieldRepository.findByDfUidAndOrderByOrdinalPositionAsc(dfUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDfAvailableDataFields(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        this.dfAvailableDataFieldRepository.deleteByDfUid(dfUid);
    }

    @Transactional(rollbackFor = Exception.class)
    public DfConfDto createOrReplaceDfConf(
            CreateOrReplaceDfConfDto createOrReplaceDfConfDto,
            UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ????????????DF?????????all available data fields
        List<DfAvailableDataFieldDo> listOfDfAvailableDataFieldDo =
                loadDfAvailableDataFields(createOrReplaceDfConfDto.getDfUid(), operationUserInfo);
        if (CollectionUtils.isEmpty(listOfDfAvailableDataFieldDo)) {
            throw new ResourceNotFoundException(DfAvailableDataFieldDo.RESOURCE_NAME + ":" +
                    createOrReplaceDfConfDto.getDfUid());
        }
        // list to map, key - field name
        Map<String, DfAvailableDataFieldDo> mapOfDfAvailableDataFieldDo = new HashMap<>();
        listOfDfAvailableDataFieldDo.forEach(dfAvailableDataFieldDo -> {
            mapOfDfAvailableDataFieldDo.put(dfAvailableDataFieldDo.getFieldName(), dfAvailableDataFieldDo);
        });

        // Step 2, ??????DF???basic conf for each data field
        boolean existsDfConfDataField = this.dfConfDataFieldRepository.existsByDfUid(createOrReplaceDfConfDto.getDfUid());
        if (existsDfConfDataField) {
            this.dfConfDataFieldRepository.deleteByDfUid(createOrReplaceDfConfDto.getDfUid());
        }

        // Step 3, ??????input??????form sequence ??? table sequence ??????????????????
        Integer maximumFormElementSequence = null;
        Integer maximumTableElementSequence = null;
        for (CreateOrReplaceDfConfDataFieldDto dfConfDataFieldDto : createOrReplaceDfConfDto.getBasic().getFields()) {
            if (mapOfDfAvailableDataFieldDo.containsKey(dfConfDataFieldDto.getFieldName())) {
                if (dfConfDataFieldDto.getFormElementSequence() != null) {
                    if (maximumFormElementSequence == null) {
                        maximumFormElementSequence = dfConfDataFieldDto.getFormElementSequence();
                    } else {
                        if (maximumFormElementSequence < dfConfDataFieldDto.getFormElementSequence()) {
                            maximumFormElementSequence = dfConfDataFieldDto.getFormElementSequence();
                        }
                    }
                }

                if (dfConfDataFieldDto.getListElementSequence() != null) {
                    if (maximumTableElementSequence == null) {
                        maximumTableElementSequence = dfConfDataFieldDto.getListElementSequence();
                    } else {
                        if (maximumTableElementSequence < dfConfDataFieldDto.getListElementSequence()) {
                            maximumTableElementSequence = dfConfDataFieldDto.getListElementSequence();
                        }
                    }
                }
            }
        }

        Date now = new Date();
        List<DfConfDataFieldDo> listOfDfConfDataFieldDo = new ArrayList<>();
        int defaultFormElementSequenceStartFrom = (maximumFormElementSequence == null ? 1 : maximumFormElementSequence + 1);
        int defaultTableElementSequenceStartFrom = (maximumTableElementSequence == null ? 1 : maximumTableElementSequence + 1);
        for (CreateOrReplaceDfConfDataFieldDto dfConfDataFieldDto : createOrReplaceDfConfDto.getBasic().getFields()) {
            if (mapOfDfAvailableDataFieldDo.containsKey(dfConfDataFieldDto.getFieldName())) {
                // ???????????????
                DfConfDataFieldDo dfConfDataFieldDo = new DfConfDataFieldDo();
                BeanUtils.copyProperties(dfConfDataFieldDto, dfConfDataFieldDo);

                // ????????????

                // ??????default form element sequence
                if (Boolean.TRUE.equals(dfConfDataFieldDo.getEnabledAsFormElement())) {
                    if (dfConfDataFieldDo.getFormElementSequence() == null) {
                        dfConfDataFieldDo.setFormElementSequence(defaultFormElementSequenceStartFrom++);
                    }
                }
                // ??????default table element sequence
                if (Boolean.TRUE.equals(dfConfDataFieldDo.getEnabledAsListElement())) {
                    if (dfConfDataFieldDo.getListElementSequence() == null) {
                        dfConfDataFieldDo.setListElementSequence(defaultTableElementSequenceStartFrom++);
                    }
                }

                dfConfDataFieldDo.setFieldType(mapOfDfAvailableDataFieldDo.get(dfConfDataFieldDto.getFieldName()).getFieldType());
                dfConfDataFieldDo.setFieldLength(mapOfDfAvailableDataFieldDo.get(dfConfDataFieldDto.getFieldName()).getFieldLength());
                dfConfDataFieldDo.setFieldDescription(mapOfDfAvailableDataFieldDo.get(dfConfDataFieldDto.getFieldName()).getFieldDescription());
                dfConfDataFieldDo.setDfUid(createOrReplaceDfConfDto.getDfUid());

                // ??????default form element type
                if (Boolean.TRUE.equals(dfConfDataFieldDo.getEnabledAsFormElement())) {
                    if (dfConfDataFieldDo.getFormElementType() == null) {
                        switch (dfConfDataFieldDo.getFieldType()) {
                            case INT:
                            case LONG:
                            case DECIMAL:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.NUMBER_RANGE);
                                break;
                            case BOOLEAN:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.DROP_DOWN_LIST_SINGLE);
                                break;
                            case TIMESTAMP:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.TIMESTAMP_RANGE);
                                break;
                            case TIME:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.TIME_RANGE);
                                break;
                            case DATE:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.DATE_RANGE);
                                break;
                            case TEXT:
                                dfConfDataFieldDo.setFormElementType(FormElementTypeEnum.EXACT_TEXT);
                                break;
                        }
                    }
                }

                BaseDo.create(dfConfDataFieldDo, operationUserInfo.getUsername(), now);
                listOfDfConfDataFieldDo.add(dfConfDataFieldDo);
            }
        }

        // Step 4, basic conf validation
        // ??????parent & child?????????????????????????????????
        Map<String, String> mapOfParentAndChildInForm = new HashMap<>();
        createOrReplaceDfConfDto.getBasic().getFields().forEach(field -> {
            if (!Strings.isNullOrEmpty(field.getChildFieldName())) {
                String anotherChild = mapOfParentAndChildInForm.get(field.getChildFieldName());
                if (anotherChild != null && anotherChild.equalsIgnoreCase(field.getFieldName())) {
                    throw new ResourceConflictException("parent & child conf deadlock: " + createOrReplaceDfConfDto.getDfUid() + ": " + field.getFieldName() + ", " + field.getChildFieldName());
                }

                mapOfParentAndChildInForm.put(field.getFieldName(), field.getChildFieldName());
            }
        });


        // Step 5, save basic conf
        if (CollectionUtils.isNotEmpty(listOfDfConfDataFieldDo)) {
            this.dfConfDataFieldRepository.saveAll(listOfDfConfDataFieldDo);
        }

        // Step 6, ??????DF???advanced conf for overall
        boolean existsDfConfAdvanced = this.dfConfAdvancedRepository.existsByDfUid(createOrReplaceDfConfDto.getDfUid());
        if (existsDfConfAdvanced) {
            this.dfConfAdvancedRepository.deleteByDfUid(createOrReplaceDfConfDto.getDfUid());
        }
        DfConfAdvancedDo dfConfAdvancedDo = new DfConfAdvancedDo();
        BeanUtils.copyProperties(createOrReplaceDfConfDto.getAdvanced(), dfConfAdvancedDo);
        dfConfAdvancedDo.setDfUid(createOrReplaceDfConfDto.getDfUid());
        BaseDo.create(dfConfAdvancedDo, operationUserInfo.getUsername(), now);
        this.dfConfAdvancedRepository.save(dfConfAdvancedDo);

        // Step 7, ????????????
        return getDfConf(createOrReplaceDfConfDto.getDfUid(), operationUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void replicateDfConf(
            Long sourceDfUid, Long destinationDfUid, UserInfo operationUserInfo) throws ServiceException {
        boolean existsSourceDf = this.dfRepository.existsByUid(sourceDfUid);
        if (!existsSourceDf) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + sourceDfUid);
        }
        boolean existsDestinationDf = this.dfRepository.existsByUid(destinationDfUid);
        if (!existsDestinationDf) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + destinationDfUid);
        }

        List<DfConfDataFieldDo> sourceListOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findByDfUid(sourceDfUid);
        if (CollectionUtils.isEmpty(sourceListOfDfConfDataFieldDo)) {
            return;
        }

        Map<String, DfConfDataFieldDo> sourceMapOfDfConfDataFieldDo = new HashMap<>();
        sourceListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
            sourceMapOfDfConfDataFieldDo.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
        });

        List<DfAvailableDataFieldDo> destinationListOfDfAvailableDataFieldDo =
                this.dfAvailableDataFieldRepository.findByDfUidAndOrderByOrdinalPositionAsc(destinationDfUid);
        if (CollectionUtils.isEmpty(destinationListOfDfAvailableDataFieldDo)) {
            throw new ResourceNotFoundException(DfAvailableDataFieldDo.RESOURCE_NAME + " of " + DfDo.RESOURCE_NAME +
                    "," + destinationDfUid);
        }
        Map<String, DfAvailableDataFieldDo> destinationMapOfDfAvailableDataFieldDo = new HashMap<>();
        destinationListOfDfAvailableDataFieldDo.forEach(dfAvailableDataFieldDo -> {
            destinationMapOfDfAvailableDataFieldDo.put(dfAvailableDataFieldDo.getFieldName(), dfAvailableDataFieldDo);
        });

        List<DfConfDataFieldDo> destinationListOfDfConfDataFieldDo =
                this.dfConfDataFieldRepository.findByDfUid(destinationDfUid);
        if (CollectionUtils.isEmpty(destinationListOfDfConfDataFieldDo)) {
            destinationListOfDfConfDataFieldDo = new ArrayList<>();
        }
        Map<String, DfConfDataFieldDo> destinationMapOfDfConfDataFieldDo = new HashMap<>();
        destinationListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
            destinationMapOfDfConfDataFieldDo.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);
        });

        // ??????source df conf
        List<DfConfDataFieldDo> newListOfDfConfDataFieldDo = new ArrayList<>();
        for (DfConfDataFieldDo sourceDfConfDataFieldDo : sourceListOfDfConfDataFieldDo) {
            if (destinationMapOfDfAvailableDataFieldDo.containsKey(sourceDfConfDataFieldDo.getFieldName())) {
                if (sourceDfConfDataFieldDo.getFieldType().equals(
                        destinationMapOfDfAvailableDataFieldDo.get(sourceDfConfDataFieldDo.getFieldName()).getFieldType())) {
                    // ????????????????????????
                    DfConfDataFieldDo destinationDfConfDataFieldDo;
                    if (destinationMapOfDfConfDataFieldDo.containsKey(sourceDfConfDataFieldDo.getFieldName())) {
                        // ??????destination?????????????????????????????????source????????????????????????
                        destinationDfConfDataFieldDo =
                                destinationMapOfDfConfDataFieldDo.get(sourceDfConfDataFieldDo.getFieldName());
                        destinationDfConfDataFieldDo.setFieldName(sourceDfConfDataFieldDo.getFieldName());
                        destinationDfConfDataFieldDo.setFieldLabel(sourceDfConfDataFieldDo.getFieldLabel());
                        destinationDfConfDataFieldDo.setFieldDescription(sourceDfConfDataFieldDo.getFieldDescription());
                        destinationDfConfDataFieldDo.setFieldLength(sourceDfConfDataFieldDo.getFieldLength());
                        destinationDfConfDataFieldDo.setFieldType(sourceDfConfDataFieldDo.getFieldType());
                        destinationDfConfDataFieldDo.setDictionaryCategoryUid(sourceDfConfDataFieldDo.getDictionaryCategoryUid());
                        destinationDfConfDataFieldDo.setDefaultsCategoryUid(sourceDfConfDataFieldDo.getDefaultsCategoryUid());
                        destinationDfConfDataFieldDo.setEnabledAsFormElement(sourceDfConfDataFieldDo.getEnabledAsFormElement());
                        destinationDfConfDataFieldDo.setFormElementSequence(sourceDfConfDataFieldDo.getFormElementSequence());
                        destinationDfConfDataFieldDo.setFormElementType(sourceDfConfDataFieldDo.getFormElementType());
                        // group
                        destinationDfConfDataFieldDo.setRole(sourceDfConfDataFieldDo.getRole());
                        destinationDfConfDataFieldDo.setAggregationType(sourceDfConfDataFieldDo.getAggregationType());
                        // data permission
                        destinationDfConfDataFieldDo.setResourceCategoryUid(sourceDfConfDataFieldDo.getResourceCategoryUid());
                        destinationDfConfDataFieldDo.setResourceCategoryName(sourceDfConfDataFieldDo.getResourceCategoryName());
                        destinationDfConfDataFieldDo.setResourceStructureUid(sourceDfConfDataFieldDo.getResourceStructureUid());
                        destinationDfConfDataFieldDo.setResourceStructureName(sourceDfConfDataFieldDo.getResourceStructureName());
                        destinationDfConfDataFieldDo.setEnabledAsListElement(sourceDfConfDataFieldDo.getEnabledAsListElement());
                        destinationDfConfDataFieldDo.setListElementSequence(sourceDfConfDataFieldDo.getListElementSequence());
                        destinationDfConfDataFieldDo.setListElementWidth(sourceDfConfDataFieldDo.getListElementWidth());
                        // sort
                        destinationDfConfDataFieldDo.setEnabledAsSortElement(sourceDfConfDataFieldDo.getEnabledAsSortElement());
                        destinationDfConfDataFieldDo.setSortDirection(sourceDfConfDataFieldDo.getSortDirection());
                        destinationDfConfDataFieldDo.setSortElementSequence(sourceDfConfDataFieldDo.getSortElementSequence());
                        destinationDfConfDataFieldDo.setSortForced(sourceDfConfDataFieldDo.getSortForced());

                        if (!Strings.isNullOrEmpty(sourceDfConfDataFieldDo.getChildFieldName())) {
                            if (!destinationMapOfDfAvailableDataFieldDo.containsKey(sourceDfConfDataFieldDo.getChildFieldName())) {
                                destinationDfConfDataFieldDo.setChildFieldName(null);
                            } else {
                                destinationDfConfDataFieldDo.setChildFieldName(sourceDfConfDataFieldDo.getChildFieldName());
                            }
                        }

                        BaseDo.update(destinationDfConfDataFieldDo, operationUserInfo.getUsername(), new Date());
                    } else {
                        // ??????destination????????????????????????????????????source?????????
                        destinationDfConfDataFieldDo = new DfConfDataFieldDo();
                        destinationDfConfDataFieldDo.setDfUid(destinationDfUid);
                        destinationDfConfDataFieldDo.setFieldName(sourceDfConfDataFieldDo.getFieldName());
                        destinationDfConfDataFieldDo.setFieldLabel(sourceDfConfDataFieldDo.getFieldLabel());
                        destinationDfConfDataFieldDo.setFieldDescription(sourceDfConfDataFieldDo.getFieldDescription());
                        destinationDfConfDataFieldDo.setFieldLength(sourceDfConfDataFieldDo.getFieldLength());
                        destinationDfConfDataFieldDo.setFieldType(sourceDfConfDataFieldDo.getFieldType());
                        destinationDfConfDataFieldDo.setDictionaryCategoryUid(sourceDfConfDataFieldDo.getDictionaryCategoryUid());
                        destinationDfConfDataFieldDo.setDefaultsCategoryUid(sourceDfConfDataFieldDo.getDefaultsCategoryUid());
                        destinationDfConfDataFieldDo.setEnabledAsFormElement(sourceDfConfDataFieldDo.getEnabledAsFormElement());
                        destinationDfConfDataFieldDo.setFormElementSequence(sourceDfConfDataFieldDo.getFormElementSequence());
                        destinationDfConfDataFieldDo.setFormElementType(sourceDfConfDataFieldDo.getFormElementType());
                        // group
                        destinationDfConfDataFieldDo.setRole(sourceDfConfDataFieldDo.getRole());
                        destinationDfConfDataFieldDo.setAggregationType(sourceDfConfDataFieldDo.getAggregationType());
                        // data permission
                        destinationDfConfDataFieldDo.setResourceCategoryUid(sourceDfConfDataFieldDo.getResourceCategoryUid());
                        destinationDfConfDataFieldDo.setResourceCategoryName(sourceDfConfDataFieldDo.getResourceCategoryName());
                        destinationDfConfDataFieldDo.setResourceStructureUid(sourceDfConfDataFieldDo.getResourceStructureUid());
                        destinationDfConfDataFieldDo.setResourceStructureName(sourceDfConfDataFieldDo.getResourceStructureName());
                        destinationDfConfDataFieldDo.setEnabledAsListElement(sourceDfConfDataFieldDo.getEnabledAsListElement());
                        destinationDfConfDataFieldDo.setListElementSequence(sourceDfConfDataFieldDo.getListElementSequence());
                        destinationDfConfDataFieldDo.setListElementWidth(sourceDfConfDataFieldDo.getListElementWidth());
                        // sort
                        destinationDfConfDataFieldDo.setEnabledAsSortElement(sourceDfConfDataFieldDo.getEnabledAsSortElement());
                        destinationDfConfDataFieldDo.setSortDirection(sourceDfConfDataFieldDo.getSortDirection());
                        destinationDfConfDataFieldDo.setSortElementSequence(sourceDfConfDataFieldDo.getSortElementSequence());
                        destinationDfConfDataFieldDo.setSortForced(sourceDfConfDataFieldDo.getSortForced());

                        if (!Strings.isNullOrEmpty(sourceDfConfDataFieldDo.getChildFieldName())) {
                            if (!destinationMapOfDfAvailableDataFieldDo.containsKey(sourceDfConfDataFieldDo.getChildFieldName())) {
                                destinationDfConfDataFieldDo.setChildFieldName(null);
                            } else {
                                destinationDfConfDataFieldDo.setChildFieldName(sourceDfConfDataFieldDo.getChildFieldName());
                            }
                        }

                        BaseDo.create(destinationDfConfDataFieldDo, operationUserInfo.getUsername(), new Date());
                    }

                    newListOfDfConfDataFieldDo.add(destinationDfConfDataFieldDo);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(newListOfDfConfDataFieldDo)) {
            this.dfConfDataFieldRepository.saveAll(newListOfDfConfDataFieldDo);
        }

        DfConfAdvancedDo sourceDfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(sourceDfUid);
        if (sourceDfConfAdvancedDo != null) {
            DfConfAdvancedDo destinationDfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(destinationDfUid);
            if (destinationDfConfAdvancedDo == null) {
                destinationDfConfAdvancedDo = new DfConfAdvancedDo();
                BeanUtils.copyProperties(sourceDfConfAdvancedDo, destinationDfConfAdvancedDo);
                destinationDfConfAdvancedDo.setDfUid(destinationDfUid);
                BaseDo.create(destinationDfConfAdvancedDo, operationUserInfo.getUsername(), new Date());
            } else {
                destinationDfConfAdvancedDo.setDefaultPageSize(sourceDfConfAdvancedDo.getDefaultPageSize());
                destinationDfConfAdvancedDo.setEnabledColumnCheckbox(sourceDfConfAdvancedDo.getEnabledColumnCheckbox());
                destinationDfConfAdvancedDo.setEnabledColumnNo(sourceDfConfAdvancedDo.getEnabledColumnNo());
                destinationDfConfAdvancedDo.setEnabledColumnOperation(sourceDfConfAdvancedDo.getEnabledColumnOperation());
                destinationDfConfAdvancedDo.setEnabledFreezeLeftColumns(sourceDfConfAdvancedDo.getEnabledFreezeLeftColumns());
                destinationDfConfAdvancedDo.setEnabledFreezeRightColumns(sourceDfConfAdvancedDo.getEnabledFreezeRightColumns());
                destinationDfConfAdvancedDo.setEnabledFreezeTopRows(sourceDfConfAdvancedDo.getEnabledFreezeTopRows());
                destinationDfConfAdvancedDo.setEnabledGraph(sourceDfConfAdvancedDo.getEnabledGraph());
                destinationDfConfAdvancedDo.setEnabledFormFolding(sourceDfConfAdvancedDo.getEnabledFormFolding());
                destinationDfConfAdvancedDo.setEnabledGroupBy(sourceDfConfAdvancedDo.getEnabledGroupBy());
                destinationDfConfAdvancedDo.setEnabledPagination(sourceDfConfAdvancedDo.getEnabledPagination());
                destinationDfConfAdvancedDo.setEnabledVerticalScrolling(sourceDfConfAdvancedDo.getEnabledVerticalScrolling());
                destinationDfConfAdvancedDo.setExportType(sourceDfConfAdvancedDo.getExportType());
                destinationDfConfAdvancedDo.setInclusiveLeftColumns(sourceDfConfAdvancedDo.getInclusiveLeftColumns());
                destinationDfConfAdvancedDo.setInclusiveRightColumns(sourceDfConfAdvancedDo.getInclusiveRightColumns());
                destinationDfConfAdvancedDo.setInclusiveTopRows(sourceDfConfAdvancedDo.getInclusiveTopRows());
                destinationDfConfAdvancedDo.setQueryType(sourceDfConfAdvancedDo.getQueryType());
                destinationDfConfAdvancedDo.setVerticalScrollingHeightThreshold(sourceDfConfAdvancedDo.getVerticalScrollingHeightThreshold());
                destinationDfConfAdvancedDo.setCsvExportStrategy(sourceDfConfAdvancedDo.getCsvExportStrategy());
                BaseDo.update(destinationDfConfAdvancedDo, operationUserInfo.getUsername(), new Date());
            }
            this.dfConfAdvancedRepository.save(destinationDfConfAdvancedDo);
        }
    }

    public List<IndexDto> loadIndexes(Long dfUid, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, ??????df??????
        DfDo dfDo = this.dfRepository.findByUid(dfUid);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfUid);
        }

        // Step 2, ??????df????????????ds???primary data object??????
        List<DfDataObjectDo> listOfPrimaryDfDataObjectDo =
                this.dfDataObjectRepository.findByDfUidAndDataObjectDependencyType(dfUid,
                        DataObjectDependencyTypeEnum.PRIMARY);
        if (CollectionUtils.isEmpty(listOfPrimaryDfDataObjectDo)) {
            throw new ResourceNotFoundException(DfDataObjectDo.RESOURCE_NAME + ":" + dfUid);
        }
        DfDataObjectDo dfDataObjectDo = listOfPrimaryDfDataObjectDo.get(0);
        Long dataObjectUid = dfDataObjectDo.getDataObjectUid();
        DsDataObjectDo dsDataObjectDo = this.dsDataObjectRepository.findByUid(dataObjectUid);
        if (dsDataObjectDo == null) {
            throw new ResourceNotFoundException(DsDataObjectDo.RESOURCE_NAME + ":" + dataObjectUid);
        }
        Long dsUid = dsDataObjectDo.getDsUid();
        DsDo dsDo = this.dsRepository.findByUid(dsUid);
        if (dsDo == null) {
            throw new ResourceNotFoundException(DsDo.RESOURCE_NAME + ":" + dsUid);
        }

        if (DataObjectTypeEnum.TABLE.equals(dsDataObjectDo.getDataObjectType())) {
            try {
                return this.dsConnectionHandler.loadIndexes(
                        dsDo.getType(),
                        dsDo.getConnectionProfileProps(),
                        dsDataObjectDo.getDbName(),
                        dsDataObjectDo.getSchemaName(),
                        dsDataObjectDo.getDataObjectName());
            } catch (Exception e) {
                throw new ServiceException("failed to load indexes. " + e.getMessage(), e);
            }
        }

        return null;
    }
}
