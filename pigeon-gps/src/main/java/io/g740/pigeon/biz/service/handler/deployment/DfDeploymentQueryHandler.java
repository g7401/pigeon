package io.g740.pigeon.biz.service.handler.deployment;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.g740.commons.exception.impl.IllegalParameterException;
import io.g740.commons.exception.impl.ResourceNotFoundException;
import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.PageResult;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.defaults.DefaultsContentDto;
import io.g740.pigeon.biz.object.dto.defaults.SimpleDefaultsDto;
import io.g740.pigeon.biz.object.dto.df.*;
import io.g740.pigeon.biz.object.dto.dictionary.DictionaryCategoryDto;
import io.g740.pigeon.biz.object.entity.df.DfConfAdvancedDo;
import io.g740.pigeon.biz.object.entity.df.DfConfDataFieldDo;
import io.g740.pigeon.biz.object.entity.df.DfDo;
import io.g740.pigeon.biz.object.entity.df.DfTagDo;
import io.g740.pigeon.biz.persistence.jdbc.NativeRepository;
import io.g740.pigeon.biz.persistence.jpa.df.*;
import io.g740.pigeon.biz.persistence.jpa.ds.DsDataObjectRepository;
import io.g740.pigeon.biz.persistence.jpa.ds.DsRepository;
import io.g740.pigeon.biz.persistence.jpa.export.AsyncExportJobRepository;
import io.g740.pigeon.biz.service.handler.admin.DefaultsHandler;
import io.g740.pigeon.biz.service.handler.admin.DictionaryHandler;
import io.g740.pigeon.biz.service.handler.ds.SqlTranslationHandler;
import io.g740.pigeon.biz.service.handler.general.DfContentQueryHandler;
import io.g740.pigeon.biz.service.handler.ds.DsConnectionHandler;
import io.g740.components.job.JobHandler;
import io.g740.pigeon.biz.service.interfaces.admin.AppService;
import io.g740.pigeon.biz.share.constants.DataFieldRoleEnum;
import io.g740.pigeon.biz.share.constants.MembershipConstants;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Handler
public class DfDeploymentQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DfDeploymentQueryHandler.class);

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

    @Autowired
    private DefaultsHandler defaultsHandler;

    @Autowired
    private SqlTranslationHandler sqlTranslationHandler;

    @Autowired
    private AsyncExportJobRepository asyncExportJobRepository;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    @Value("${application.dir.general.project.export}")
    private String projectExportPath;

    @Autowired
    private JobHandler jobHandler;

    @Autowired
    private DfContentQueryHandler dfContentQueryHandler;

    @Autowired
    private NativeRepository nativeRepository;

    @Autowired
    private AppService appService;

    public DfConfForPageGenerationDto getDfConfForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        DfConfForPageGenerationDto dfConfForPageGenerationDto = new DfConfForPageGenerationDto();

        // Step 1, 获取df基本信息
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }
        dfConfForPageGenerationDto.setDfUid(dfDo.getUid());
        dfConfForPageGenerationDto.setDfKey(dfDo.getKey());
        dfConfForPageGenerationDto.setDfName(dfDo.getName());
        dfConfForPageGenerationDto.setDfDescription(dfDo.getDescription());
        List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByDfUid(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
            List<String> dfTags = new ArrayList<>();
            listOfDfTagDo.forEach(dfTagDo -> {
                dfTags.add(dfTagDo.getTag());
            });
            dfConfForPageGenerationDto.setDfTags(dfTags);
        }

        // Step 2, 获取df的basic配置中的form配置
        List<DfConfDataFieldDo> formListOfDfConfDataFieldDo =
                this.dfConfDataFieldRepository.findFormDataFieldsByDfUidAndOrderByFormElementSequenceAsc(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(formListOfDfConfDataFieldDo)) {
            List<DfConfFormFieldDto> form = new ArrayList<>();
            dfConfForPageGenerationDto.setForm(form);

            // 存储 list to map
            Map<String, DfConfDataFieldDo> mapOfDataFieldEnabledAsFormElement = new HashMap<>(20);
            // 存储直接父子关系
            Map<String, String> mapOfParentAndChildEnabledAsFormElement = new HashMap<>(10);
            // 存储直接子父关系
            Map<String, String> mapOfChildAndParentEnabledAsFormElement = new HashMap<>(10);
            // Step 2.1 list to map
            formListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                // 收集所有enabled as form element
                mapOfDataFieldEnabledAsFormElement.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo);

                // 收集所有填了child field的字段
                if (!Strings.isNullOrEmpty(dfConfDataFieldDo.getChildFieldName())) {
                    if (mapOfParentAndChildEnabledAsFormElement.containsKey(dfConfDataFieldDo.getFieldName())) {
                        throw new IllegalParameterException("configured more than 1 child fields");
                    }
                    mapOfParentAndChildEnabledAsFormElement.put(dfConfDataFieldDo.getFieldName(), dfConfDataFieldDo.getChildFieldName());

                    if (mapOfChildAndParentEnabledAsFormElement.containsKey(dfConfDataFieldDo.getChildFieldName())) {
                        throw new IllegalParameterException("configured more than 1 parent fields");
                    }
                    mapOfChildAndParentEnabledAsFormElement.put(dfConfDataFieldDo.getChildFieldName(), dfConfDataFieldDo.getFieldName());
                }
            });
            // 检查所有的parent & child是否都是enabled as form element
            for (Map.Entry<String, String> entry : mapOfParentAndChildEnabledAsFormElement.entrySet()) {
                if (!mapOfDataFieldEnabledAsFormElement.containsKey(entry.getKey())) {
                    throw new IllegalParameterException(entry.getKey() +
                            " is not enabled as form element, but it is configured a child field " + entry.getValue());
                }
                if (!mapOfDataFieldEnabledAsFormElement.containsKey(entry.getValue())) {
                    throw new IllegalParameterException(entry.getValue() +
                            " is not enabled as form element, but it is configured as a child field of " + entry.getKey());
                }
            }

            // 处理有级联关系的字段，找出0段或多段级联关系
            List<SimpleTreeNode> treeNodeOfHierarchyStructure =
                    buildTreeFromMapOfParentAndChild(mapOfParentAndChildEnabledAsFormElement,
                            mapOfChildAndParentEnabledAsFormElement);
            treeNodeOfHierarchyStructure.forEach(rootTreeNode -> {
                DfConfDataFieldDo dfConfDataFieldDo = mapOfDataFieldEnabledAsFormElement.get(rootTreeNode.getName());
                DfConfFormFieldDto dfConfFormFieldDto = new DfConfFormFieldDto();
                dfConfFormFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                dfConfFormFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                dfConfFormFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                dfConfFormFieldDto.setFormElementSequence(dfConfDataFieldDo.getFormElementSequence());
                dfConfFormFieldDto.setFormElementType(dfConfDataFieldDo.getFormElementType());

                // 设置field hierarchy structure
                DfHierarchyFormFieldDto fieldHierarchyStructure = buildDfHierarchyFormField(rootTreeNode, mapOfDataFieldEnabledAsFormElement, 0);
                dfConfFormFieldDto.setFieldHierarchyStructure(fieldHierarchyStructure);

                // 设置field hierarchy content
                Long dictionaryCategoryUid = dfConfDataFieldDo.getDictionaryCategoryUid();
                if (dictionaryCategoryUid != null) {
                    // 20210222, 加载field hierarchy content导致性能下降严重，因此，决定取消一次性加载hierarchy content的方式。
                    // SimpleTreeNode fieldHierarchyContent = this.dictionaryHandler.getDictionaryContent(dictionaryCategoryUid, operationUserInfo);
                    // dfConfFormFieldDto.setFieldHierarchyContent(fieldHierarchyContent);
                    dfConfFormFieldDto.setDictionaryCategoryUid(dictionaryCategoryUid);
                    try {
                        DictionaryCategoryDto dictionaryCategoryDto = this.dictionaryHandler.getDictionaryCategory(dictionaryCategoryUid);
                        dfConfFormFieldDto.setDictionaryCategoryName(dictionaryCategoryDto.getName());
                    } catch (Exception e) {
                        LOGGER.warn("cannot find dictionary category " + dictionaryCategoryUid);

                        dfConfFormFieldDto.setDictionaryCategoryName(dfConfDataFieldDo.getFieldLabel());
                    }
                } else {
                    LOGGER.warn("df conf data field " + dfConfDataFieldDo.getFieldName() + " is configured hierarchy, but not configure dictionary category");
                }

                // 设置field default values
                if (dfConfDataFieldDo.getDefaultsCategoryUid() != null) {
                    List<DefaultsContentDto> listOfDefaultsContentDto =
                            this.defaultsHandler.getDefaultsContent(dfConfDataFieldDo.getDefaultsCategoryUid(), operationUserInfo);
                    if (CollectionUtils.isNotEmpty(listOfDefaultsContentDto)) {
                        dfConfFormFieldDto.setFieldDefaultValues(new ArrayList<>());
                        listOfDefaultsContentDto.forEach(defaultsContentDto -> {
                            SimpleDefaultsDto simpleDefaultsDto = new SimpleDefaultsDto();
                            simpleDefaultsDto.setUidCode(defaultsContentDto.getUidCode());
                            simpleDefaultsDto.setValue(defaultsContentDto.getValue());
                            simpleDefaultsDto.setLabel(defaultsContentDto.getLabel());
                            dfConfFormFieldDto.getFieldDefaultValues().add(simpleDefaultsDto);
                        });
                        // 处理range但又只有一个defaults content的情况, 复制一个
                        if (listOfDefaultsContentDto.size() == 1) {
                            switch (dfConfDataFieldDo.getFormElementType()) {
                                case NUMBER_RANGE:
                                case TIMESTAMP_RANGE:
                                case DATE_RANGE:
                                case TIME_RANGE:
                                    dfConfFormFieldDto.getFieldDefaultValues().add(dfConfFormFieldDto.getFieldDefaultValues().get(0));
                                    break;
                            }
                        }
                    }
                }

                form.add(dfConfFormFieldDto);
            });

            // 处理没有级联关系的字段
            formListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                if (!mapOfParentAndChildEnabledAsFormElement.containsKey(dfConfDataFieldDo.getFieldName()) &&
                        !mapOfChildAndParentEnabledAsFormElement.containsKey(dfConfDataFieldDo.getFieldName())) {
                    DfConfFormFieldDto dfConfFormFieldDto = new DfConfFormFieldDto();
                    dfConfFormFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                    dfConfFormFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                    dfConfFormFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                    dfConfFormFieldDto.setFormElementSequence(dfConfDataFieldDo.getFormElementSequence());
                    dfConfFormFieldDto.setFormElementType(dfConfDataFieldDo.getFormElementType());

                    // 设置field optional values
                    Long dictionaryCategoryUid = dfConfDataFieldDo.getDictionaryCategoryUid();
                    if (dictionaryCategoryUid != null) {
                        SimpleTreeNode treeNode = this.dictionaryHandler.getDictionaryContent(dfConfDataFieldDo.getDictionaryCategoryUid(), operationUserInfo);
                        dfConfFormFieldDto.setFieldOptionalValues(treeNode);
                        dfConfFormFieldDto.setDictionaryCategoryUid(dictionaryCategoryUid);
                        try {
                            DictionaryCategoryDto dictionaryCategoryDto = this.dictionaryHandler.getDictionaryCategory(dictionaryCategoryUid);
                            dfConfFormFieldDto.setDictionaryCategoryName(dictionaryCategoryDto.getName());
                        } catch (Exception e) {
                            LOGGER.warn("cannot find dictionary category " + dictionaryCategoryUid);

                            dfConfFormFieldDto.setDictionaryCategoryName(dfConfDataFieldDo.getFieldLabel());
                        }
                    }

                    // 设置field default values
                    if (dfConfDataFieldDo.getDefaultsCategoryUid() != null) {
                        List<DefaultsContentDto> listOfDefaultsContentDto =
                                this.defaultsHandler.getDefaultsContent(dfConfDataFieldDo.getDefaultsCategoryUid(), operationUserInfo);
                        if (CollectionUtils.isNotEmpty(listOfDefaultsContentDto)) {
                            dfConfFormFieldDto.setFieldDefaultValues(new ArrayList<>());
                            listOfDefaultsContentDto.forEach(defaultsContentDto -> {
                                SimpleDefaultsDto simpleDefaultsDto = new SimpleDefaultsDto();
                                simpleDefaultsDto.setUidCode(defaultsContentDto.getUidCode());
                                simpleDefaultsDto.setValue(defaultsContentDto.getValue());
                                simpleDefaultsDto.setLabel(defaultsContentDto.getLabel());
                                dfConfFormFieldDto.getFieldDefaultValues().add(simpleDefaultsDto);
                            });
                            // 处理range但又只有一个defaults content的情况, 复制一个
                            if (listOfDefaultsContentDto.size() == 1) {
                                switch (dfConfDataFieldDo.getFormElementType()) {
                                    case NUMBER_RANGE:
                                    case TIMESTAMP_RANGE:
                                    case DATE_RANGE:
                                    case TIME_RANGE:
                                        dfConfFormFieldDto.getFieldDefaultValues().add(dfConfFormFieldDto.getFieldDefaultValues().get(0));
                                        break;
                                }
                            }
                        }
                    }

                    form.add(dfConfFormFieldDto);
                }
            });
        }

        // Step 3, 获取df的basic配置中的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDo =
                this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDo)) {
            List<DfConfTableFieldDto> table = new ArrayList<>();
            dfConfForPageGenerationDto.setTable(table);

            tableListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                DfConfTableFieldDto dfConfTableFieldDto = new DfConfTableFieldDto();
                dfConfTableFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                dfConfTableFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                dfConfTableFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                dfConfTableFieldDto.setFieldType(dfConfDataFieldDo.getFieldType());
                // List
                dfConfTableFieldDto.setListElementSequence(dfConfDataFieldDo.getListElementSequence());
                dfConfTableFieldDto.setListElementWidth(dfConfDataFieldDo.getListElementWidth());
                // Sort
                dfConfTableFieldDto.setEnabledAsSortElement(dfConfDataFieldDo.getEnabledAsSortElement());
                dfConfTableFieldDto.setSortDirection(dfConfDataFieldDo.getSortDirection());
                dfConfTableFieldDto.setSortElementSequence(dfConfDataFieldDo.getSortElementSequence());
                dfConfTableFieldDto.setSortForced(dfConfDataFieldDo.getSortForced());
                // Group
                dfConfTableFieldDto.setRole(dfConfDataFieldDo.getRole());
                dfConfTableFieldDto.setAggregationType(dfConfDataFieldDo.getAggregationType());
                // Data Permission
                dfConfTableFieldDto.setResourceCategoryUid(dfConfDataFieldDo.getResourceCategoryUid());
                dfConfTableFieldDto.setResourceCategoryName(dfConfDataFieldDo.getResourceCategoryName());
                dfConfTableFieldDto.setResourceStructureUid(dfConfDataFieldDo.getResourceStructureUid());
                dfConfTableFieldDto.setResourceStructureName(dfConfDataFieldDo.getResourceStructureName());

                table.add(dfConfTableFieldDto);
            });
        }

        // Step 4, 获取df的advanced配置
        DfConfAdvancedDo dfConfAdvancedDo = this.dfConfAdvancedRepository.findByDfUid(dfDo.getUid());
        if (dfConfAdvancedDo != null) {
            DfConfAdvancedDto dfConfAdvancedDto = new DfConfAdvancedDto();
            dfConfForPageGenerationDto.setAdvanced(dfConfAdvancedDto);

            BeanUtils.copyProperties(dfConfAdvancedDo, dfConfAdvancedDto);
        }

        return dfConfForPageGenerationDto;
    }

    public List<DfConfTableFieldDto> getDfConfTableFieldsForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取df基本信息
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }

        // Step 2, 获取df的basic配置中的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDo)) {
            List<DfConfTableFieldDto> table = new ArrayList<>();
            tableListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                DfConfTableFieldDto dfConfTableFieldDto = new DfConfTableFieldDto();
                dfConfTableFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                dfConfTableFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                dfConfTableFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                dfConfTableFieldDto.setFieldType(dfConfDataFieldDo.getFieldType());
                // List
                dfConfTableFieldDto.setListElementSequence(dfConfDataFieldDo.getListElementSequence());
                dfConfTableFieldDto.setListElementWidth(dfConfDataFieldDo.getListElementWidth());
                // Sort
                dfConfTableFieldDto.setEnabledAsSortElement(dfConfDataFieldDo.getEnabledAsSortElement());
                dfConfTableFieldDto.setSortDirection(dfConfDataFieldDo.getSortDirection());
                dfConfTableFieldDto.setSortElementSequence(dfConfDataFieldDo.getSortElementSequence());
                dfConfTableFieldDto.setSortForced(dfConfDataFieldDo.getSortForced());
                // Group
                dfConfTableFieldDto.setRole(dfConfDataFieldDo.getRole());
                dfConfTableFieldDto.setAggregationType(dfConfDataFieldDo.getAggregationType());
                // Data Permission
                dfConfTableFieldDto.setResourceCategoryUid(dfConfDataFieldDo.getResourceCategoryUid());
                dfConfTableFieldDto.setResourceCategoryName(dfConfDataFieldDo.getResourceCategoryName());
                dfConfTableFieldDto.setResourceStructureUid(dfConfDataFieldDo.getResourceStructureUid());
                dfConfTableFieldDto.setResourceStructureName(dfConfDataFieldDo.getResourceStructureName());

                table.add(dfConfTableFieldDto);
            });

            return table;
        }

        return null;
    }

    public List<DfConfTableFieldDto> getDfConfTableFieldsWithResourceCategoryForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取df基本信息
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }

        // Step 2, 获取df的basic配置中的table配置
        List<DfConfDataFieldDo> tableListOfDfConfDataFieldDo = this.dfConfDataFieldRepository.findTableDataFieldsByDfUidAndOrderByListElementSequenceAsc(dfDo.getUid());
        if (CollectionUtils.isNotEmpty(tableListOfDfConfDataFieldDo)) {
            List<DfConfTableFieldDto> table = new ArrayList<>();
            tableListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {

                if (dfConfDataFieldDo.getResourceCategoryUid() != null) {
                    DfConfTableFieldDto dfConfTableFieldDto = new DfConfTableFieldDto();
                    dfConfTableFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                    dfConfTableFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                    dfConfTableFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                    dfConfTableFieldDto.setFieldType(dfConfDataFieldDo.getFieldType());
                    // List
                    dfConfTableFieldDto.setListElementSequence(dfConfDataFieldDo.getListElementSequence());
                    dfConfTableFieldDto.setListElementWidth(dfConfDataFieldDo.getListElementWidth());
                    // Sort
                    dfConfTableFieldDto.setEnabledAsSortElement(dfConfDataFieldDo.getEnabledAsSortElement());
                    dfConfTableFieldDto.setSortDirection(dfConfDataFieldDo.getSortDirection());
                    dfConfTableFieldDto.setSortElementSequence(dfConfDataFieldDo.getSortElementSequence());
                    dfConfTableFieldDto.setSortForced(dfConfDataFieldDo.getSortForced());
                    // Group
                    dfConfTableFieldDto.setRole(dfConfDataFieldDo.getRole());
                    dfConfTableFieldDto.setAggregationType(dfConfDataFieldDo.getAggregationType());
                    // Data Permission
                    dfConfTableFieldDto.setResourceCategoryUid(dfConfDataFieldDo.getResourceCategoryUid());
                    dfConfTableFieldDto.setResourceCategoryName(dfConfDataFieldDo.getResourceCategoryName());
                    dfConfTableFieldDto.setResourceStructureUid(dfConfDataFieldDo.getResourceStructureUid());
                    dfConfTableFieldDto.setResourceStructureName(dfConfDataFieldDo.getResourceStructureName());

                    table.add(dfConfTableFieldDto);
                }
            });

            return table;
        }

        return null;
    }

    public List<DfConfTableFieldDto> getDfConfTableFieldsEnabledGroupByForPageGeneration(
            String dfKey, UserInfo operationUserInfo) throws ServiceException {
        // Step 1, 获取df基本信息
        DfDo dfDo = this.dfRepository.findByKey(dfKey);
        if (dfDo == null) {
            throw new ResourceNotFoundException(DfDo.RESOURCE_NAME + ":" + dfKey);
        }

        // Step 2, 获取df的basic配置中的group配置，且要求role为维度
        List<DfConfDataFieldDo> groupListOfDfConfDataFieldDo =
                this.dfConfDataFieldRepository.findGroupDataFieldsByDfUidAndRole(dfDo.getUid(), DataFieldRoleEnum.DIMENSION);
        if (CollectionUtils.isNotEmpty(groupListOfDfConfDataFieldDo)) {
            List<DfConfTableFieldDto> table = new ArrayList<>();
            groupListOfDfConfDataFieldDo.forEach(dfConfDataFieldDo -> {
                DfConfTableFieldDto dfConfTableFieldDto = new DfConfTableFieldDto();
                dfConfTableFieldDto.setFieldName(dfConfDataFieldDo.getFieldName());
                dfConfTableFieldDto.setFieldLabel(dfConfDataFieldDo.getFieldLabel());
                dfConfTableFieldDto.setFieldDescription(dfConfDataFieldDo.getFieldDescription());
                dfConfTableFieldDto.setFieldType(dfConfDataFieldDo.getFieldType());
                // List
                dfConfTableFieldDto.setListElementSequence(dfConfDataFieldDo.getListElementSequence());
                dfConfTableFieldDto.setListElementWidth(dfConfDataFieldDo.getListElementWidth());
                // Sort
                dfConfTableFieldDto.setEnabledAsSortElement(dfConfDataFieldDo.getEnabledAsSortElement());
                dfConfTableFieldDto.setSortDirection(dfConfDataFieldDo.getSortDirection());
                dfConfTableFieldDto.setSortElementSequence(dfConfDataFieldDo.getSortElementSequence());
                dfConfTableFieldDto.setSortForced(dfConfDataFieldDo.getSortForced());
                // Group
                dfConfTableFieldDto.setRole(dfConfDataFieldDo.getRole());
                dfConfTableFieldDto.setAggregationType(dfConfDataFieldDo.getAggregationType());
                // Data Permission
                dfConfTableFieldDto.setResourceCategoryUid(dfConfDataFieldDo.getResourceCategoryUid());
                dfConfTableFieldDto.setResourceCategoryName(dfConfDataFieldDo.getResourceCategoryName());
                dfConfTableFieldDto.setResourceStructureUid(dfConfDataFieldDo.getResourceStructureUid());
                dfConfTableFieldDto.setResourceStructureName(dfConfDataFieldDo.getResourceStructureName());

                table.add(dfConfTableFieldDto);
            });

            return table;
        }

        return null;
    }

    private List<SimpleTreeNode> buildTreeFromMapOfParentAndChild(
            Map<String, String> mapOfParentAndChild, Map<String, String> mapOfChildAndParent) {
        List<SimpleTreeNode> result = new ArrayList<>();

        Map<String, SimpleTreeNode> mapOfTreeNode = new HashMap<>(10);

        // 理清tree node之间的关系
        for (Map.Entry<String, String> entry : mapOfParentAndChild.entrySet()) {
            SimpleTreeNode parentTreeNode;
            SimpleTreeNode childTreeNode;

            // parent tree node
            if (mapOfTreeNode.containsKey(entry.getKey())) {
                parentTreeNode = mapOfTreeNode.get(entry.getKey());
            } else {
                parentTreeNode = new SimpleTreeNode();
                parentTreeNode.setName(entry.getKey());
                mapOfTreeNode.put(entry.getKey(), parentTreeNode);
            }

            // child tree node
            if (mapOfTreeNode.containsKey(entry.getValue())) {
                childTreeNode = mapOfTreeNode.get(entry.getValue());
            } else {
                childTreeNode = new SimpleTreeNode();
                childTreeNode.setName(entry.getValue());
                mapOfTreeNode.put(entry.getValue(), childTreeNode);
            }

            if (CollectionUtils.isEmpty(parentTreeNode.getChildren())) {
                parentTreeNode.setChildren(new ArrayList<>());
            }
            parentTreeNode.getChildren().add(childTreeNode);
        }

        // 找到N棵树各自的root tree node
        // 遍历父子关系，key - 父，value - 子
        for (Map.Entry<String, String> entry : mapOfParentAndChild.entrySet()) {
            // 继续判断：父有没有作为别人的子
            if (!mapOfChildAndParent.containsKey(entry.getKey())) {
                // 没有，也就是找到一个棵树的根节点
                SimpleTreeNode rootTreeNode = mapOfTreeNode.get(entry.getKey());
                if (rootTreeNode != null) {
                    result.add(rootTreeNode);
                } else {
                    LOGGER.error("cannot find " + entry.getKey());
                }
            }
        }

        return result;
    }

    private DfHierarchyFormFieldDto buildDfHierarchyFormField(SimpleTreeNode treeNode,
                                                              Map<String, DfConfDataFieldDo> mapOfDataField, int cycles) {
        SimpleTreeNode childTreeNode = treeNode.getChildren().get(0);

        DfHierarchyFormFieldDto dfHierarchyFormFieldDto = new DfHierarchyFormFieldDto();
        dfHierarchyFormFieldDto.setFieldName(mapOfDataField.get(childTreeNode.getName()).getFieldName());
        dfHierarchyFormFieldDto.setFieldDescription(mapOfDataField.get(childTreeNode.getName()).getFieldDescription());
        dfHierarchyFormFieldDto.setFieldLabel(mapOfDataField.get(childTreeNode.getName()).getFieldLabel());

        if (CollectionUtils.isNotEmpty(childTreeNode.getChildren())) {
            if (cycles < 10) {
                DfHierarchyFormFieldDto childDfHierarchyFormFieldDto = buildDfHierarchyFormField(childTreeNode, mapOfDataField, ++cycles);
                dfHierarchyFormFieldDto.setChild(childDfHierarchyFormFieldDto);
            } else {
                LOGGER.warn("recurse {} times, may be in a dead loop", cycles);
            }
        }

        return dfHierarchyFormFieldDto;
    }

    public PageResult<DfDto> queryDfForPageGeneration(
            List<String> tags, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        PageResult<DfDto> pageResult = new PageResult<>();

        // 根据tag(s)搜索df uid(s)
        if (CollectionUtils.isNotEmpty(tags)) {
            List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByTagInAndNameAndOrderByDfUidAsc(tags);
            if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
                List<Long> listOfDfUid = new ArrayList<>();
                listOfDfTagDo.forEach(dfTagDo -> {
                    if (!listOfDfUid.contains(dfTagDo.getDfUid())) {
                        listOfDfUid.add(dfTagDo.getDfUid());
                    }
                });
                Page<DfDo> page = this.dfRepository.findByUidIn(listOfDfUid, pageable);
                pageResult.setTotalPages(page.getTotalPages());
                pageResult.setTotalElements(page.getTotalElements());
                pageResult.setPageSize(page.getSize());
                pageResult.setPageNumber(page.getNumber());
                pageResult.setNumberOfElements(page.getNumberOfElements());
                if (page.hasContent()) {
                    pageResult.setContent(new ArrayList<>());
                    page.getContent().forEach(dfDo -> {
                        DfDto dfDto = new DfDto();
                        BeanUtils.copyProperties(dfDo, dfDto);
                        pageResult.getContent().add(dfDto);
                    });
                }
            } else {
                pageResult.setNumberOfElements(0);
                pageResult.setPageNumber(pageable.getPageNumber());
                pageResult.setPageSize(pageable.getPageSize());
                pageResult.setTotalElements(0L);
                pageResult.setTotalPages(0);
            }
        } else {
            Page<DfDo> page = this.dfRepository.findAll(pageable);
            pageResult.setTotalPages(page.getTotalPages());
            pageResult.setTotalElements(page.getTotalElements());
            pageResult.setPageSize(page.getSize());
            pageResult.setPageNumber(page.getNumber());
            pageResult.setNumberOfElements(page.getNumberOfElements());
            if (page.hasContent()) {
                pageResult.setContent(new ArrayList<>());
                page.getContent().forEach(dfDo -> {
                    DfDto dfDto = new DfDto();
                    BeanUtils.copyProperties(dfDo, dfDto);
                    pageResult.getContent().add(dfDto);
                });
            }
        }
        return pageResult;
    }

    public PageResult<DfSimpleDto> queryDfForPageGeneration(
            List<String> tags, String name, Pageable pageable, UserInfo operationUserInfo) throws ServiceException {
        List<Long> listOfAvailableDfUid = null;
        String createBy = null;

        // DEVELOPER可以创建DF，查询的时候应该默认包含自己所创建的DF
        if (MembershipConstants.MEMBERSHIP_DEVELOPER.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            createBy = operationUserInfo.getUsername();
        }

        // 非ADMIN，最大的查询范围是其所获授权的所有App所包含的所有的DF
        if (!MembershipConstants.MEMBERSHIP_ADMIN.equalsIgnoreCase(operationUserInfo.getRoleName())) {
            listOfAvailableDfUid = this.appService.getAuthorizedDfOfUser(operationUserInfo);

            if (CollectionUtils.isEmpty(listOfAvailableDfUid)) {
                // 没有找到授权的DF，又不是DEVELOPER（也就是没有机会自己创建DF）
                if (!MembershipConstants.MEMBERSHIP_DEVELOPER.equalsIgnoreCase(operationUserInfo.getRoleName())) {
                    PageResult<DfSimpleDto> pageResult = new PageResult<>();
                    pageResult.setNumberOfElements(0);
                    pageResult.setPageNumber(pageable.getPageNumber());
                    pageResult.setPageSize(pageable.getPageSize());
                    pageResult.setTotalElements(0L);
                    pageResult.setTotalPages(0);

                    return pageResult;
                }
            }
        }
        try {
            return this.nativeRepository.queryDfByTagsAndDfNameWithinLimitRange(
                    tags, name, listOfAvailableDfUid, createBy, pageable);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<DfDto> listDfForPageGeneration(List<String> tags, UserInfo operationUserInfo) throws ServiceException {
        List<DfDto> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(tags)) {
            List<DfTagDo> listOfDfTagDo = this.dfTagRepository.findByTagInAndNameAndOrderByDfUidAsc(tags);
            if (CollectionUtils.isNotEmpty(listOfDfTagDo)) {
                List<Long> listOfDfUid = new ArrayList<>();
                listOfDfTagDo.forEach(dfTagDo -> {
                    if (!listOfDfUid.contains(dfTagDo.getDfUid())) {
                        listOfDfUid.add(dfTagDo.getDfUid());
                    }
                });
                List<DfDo> dfDoList = this.dfRepository.findByUidIn(listOfDfUid);
                if (CollectionUtils.isNotEmpty(dfDoList)) {
                    dfDoList.forEach(dfDo -> {
                        DfDto dfDto = new DfDto();
                        BeanUtils.copyProperties(dfDo, dfDto);
                        result.add(dfDto);
                    });
                }
            }
        }

        return result;
    }

    public PageResult<JSONObject> queryDfContent(
            String dfKey,
            Map<String, String[]> parameterMap,
            DfAdvancedQueryDto dfAdvancedQueryDto,
            Pageable pageable,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfContentQueryHandler.queryDfContentAndReturnJSONObjectAsRow(
                dfKey, parameterMap, dfAdvancedQueryDto, pageable, operationUserInfo);
    }

    public List<JSONObject> queryDfContentForGraph(
            String dfKey,
            List<String> dimensionFieldNames,
            String kpiFieldName,
            Map<String, String[]> parameterMap,
            Sort sort,
            UserInfo operationUserInfo) throws ServiceException {
        return this.dfContentQueryHandler.groupByQueryDfContentAndReturnJSONObjectAsRow(
                dfKey, dimensionFieldNames, kpiFieldName, parameterMap, sort, operationUserInfo);
    }
}
