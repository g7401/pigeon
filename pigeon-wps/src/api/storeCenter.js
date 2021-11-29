import qs from 'qs'
export const api = {
  dataSource: '/inks-dis/ds/def/ds',
  dataSourceDetail: '/inks-dis/ds/def/ds/details',
  dataSourceType: '/inks-dis/ds/def/ds_type',
  getDsProfile: '/inks-dis/ds/conf/ds/connection_profile_details',
  getDsTypeProfileList: '/inks-dis/ds/conf/ds/connection_profile_list',
  dsConnectionProfile: '/inks-dis/ds/conf/ds/ds_connection_profile',
  createDsTypeProfile: '/inks-dis/ds/conf/ds/connection_profile',
  getProcessorDetail: '/inks-dis/ds/def/processor/details',

  getDaqProcessDefs: '/inks-dis/ds/def/daq/process/query',
  daqProcessDef: '/inks-dis/ds/def/daq/process',
  dapConfSchedule: '/inks-dis/ds/conf/daq/process/schedule',
  getDapTaskConfig: '/inks-dis/ds/conf/daq/process/details',
  dapTaskConfig: '/inks-dis/ds/conf/daq/task',

  getProcessorList: '/inks-dis/processors/query',
  createProcessor: '/inks-dis/processors/',
  parseProcessor: '/inks-dis/processors/processor_parsings',

  dataTarget: '/inks-dis/ds/def/dt',
  dataTargetList: '/inks-dis/ds/def/dt/query',
  getDataSourceSds: '/inks-dis/ds/def/sds',
  getDataTarget: '/inks-dis/ds/def/dt/query',
  getDtDefDetail: '/inks-dis/ds/def/dt/details',

  getDtrDefList: '/inks-dis/ds/def/dtr/process/query',
  dtrProcessDef: '/inks-dis/ds/def/dtr/process',
  dtrProcessDefDetails: '/inks-dis/ds/def/dtr/process/details',
  getDtrConfDetail: '/inks-dis/ds/conf/dtr/process/details',
  dtrConfSchedule: '/inks-dis/ds/conf/dtr/process/schedule',
  dtrTaskConfig: '/inks-dis/ds/conf/dtr/task',
  // getDtrTaskConfig: '/inks-dis/ds/conf/dtr/process/details',

  getAvailableDS: '/inks-ebs/entity/def/available_ds',
  inUseDS: '/inks-ebs/entity/def/in_use_ds',
  getDSAtrrs: '/inks-ebs/entity/def/latest_src_attrs_merged_in_use_info',
  srcTargetAttrs: '/inks-ebs/entity/def/target_attrs_built_from_src',
  targetAttrs: '/inks-ebs/entity/def/target_attrs',
  targetAttr: '/inks-ebs/entity/def/target_attrs_built_from_zero',
  changeTargetStatus: '/inks-ebs/entity/def/target_attrs/status',

  getDaqExecList: '/inks-dis/ds/exec/daq/process/query',
  deleteDaqProcessInst: '/inks-dis/ds/exec/daq/process_instance',
  getDaqConnectDetail: '/inks-dis/ds/exec/daq/task/connect',
  getDaqExtractDetail: '/inks-dis/ds/exec/daq/task/extract',
  daqProcessInst: '/inks-dis/ds/exec/daq/process',
  getDaqLoadDetail: '/inks-dis/ds/exec/daq/task/load',
  getDaqPublishDetail: '/inks-dis/ds/exec/daq/task/publish',
  getDtrTransformDetail: '/inks-dis/ds/exec/dtr/task/transform',
  getDaqOutTableData: '/inks-dis/ds/exec/daq/task/query_output_data',
  getDtrOutTableData: '/inks-dis/ds/exec/dtr/task/query_output_data',
  getDqrExecList: '/inks-dis/ds/exec/dtr/process/query',
  deleteDtrProcessInst: '/inks-dis/ds/exec/dtr/process_instance',
  getEbExecList: '/inks-ebs/entity/exec/eb/process_inst/list',
  getEbExecutionDetail: '/inks-ebs/entity/exec/eb/process_inst/detail',

  ebPrimarySrc: '/inks-ebs/entity/conf/eb/primary_src',
  ebSecondarySrc: '/inks-ebs/entity/conf/eb/secondary_src_ia',
  ebSchedule: '/inks-ebs/entity/conf/eb/schedule',
  getTargetAttrsConfigs: '/inks-ebs/entity/conf/eb/target_attrs_conf_summary',
  ebTask: '/inks-ebs/entity/conf/eb/task',
  getEbTaskSrcAttrs: '/inks-ebs/entity/conf/eb/task/src_attrs',
  getEbTaskTargetAttrs: '/inks-ebs/entity/conf/eb/task/target_attrs',

  getEntityQuerySetting: '/swan-cis/query/entity/setting',
  getEntityQueryData: '/swan-cis/query/entity/data',

  getDaqProcessConfigGraph: '/inks-dis/ds/conf/daq/process/flow_diagram',
  getDtrProcessConfigGraph: '/inks-dis/ds/conf/dtr/process/flow_diagram',

  getEbProcessDefImg: '/inks-ebs/entity/conf/eb/process/img',
  getEbTaskDefImg: '/inks-ebs/entity/conf/eb/task/img',

  validateExpression: '/inks-ebs/processors/expression_validation',
  getAttrMappingSrc: '/inks-ebs/entity/conf/eb/task/attr_mapping_src',

  getExportStatus: '/swan-cis/query/entity/data/export',
  getExportFile: '/swan-cis/query/entity/data/export/task',

  getNationalGeography: '/lark-efs/base/dict/Hierarchy/geography',
  getNationalGeographyByName: '/lark-efs/base/dict/Hierarchy/geography_by_name',
  getMarsGeography: '/lark-efs/base/dict/Hierarchy/marsgeohier',
  getStoreChannel: '/lark-efs/base/dict/Hierarchy/storeChannel',
  getStoreChain: '/lark-efs/base/dict/Hierarchy/storeChain',
  getStoreChainByName: '/lark-efs/base/dict/Hierarchy/storechain_by_name',

  getGeograogyStores: '/lark-efs/base/dict/Hierarchy/geography/list',
  getMarsGeograogyStores: '/lark-efs/base/dict/Hierarchy/marsgeohier/list',
  getChannelStores: '/lark-efs/base/dict/Hierarchy/storeChannel/list',
  getChainStores: '/lark-efs/base/dict/Hierarchy/storeChain/list',
  getChains: '/lark-efs/base/dict/Hierarchy/storeChainList',

  // template
  testProcessorConnection: '/inks-dis/general/test_connection_of_connect_processor',
  parseDsFromExcelApi: '/inks-dis/ds/templates/parse_ds_template_from_excel',
  parseDsFromCsvApi: '/inks-dis/ds/templates/parse_ds_template_from_csv',
  getDataTargetDetail: '/inks-dis/general/dt/details',
  getDtSrcDataFields: '/inks-dis/general/dt/src/data_fields',
  validateTransformSql: '/inks-dis/general/validate_sql_of_sql_transform_processor',
  getDtSrcSdsList: '/inks-dis/general/dt/src/sds',
  getDtSrcDtList: '/inks-dis/general/dt/src/dt',
  getSrcIntermediate: '/inks-dis/general/dt/src/intermediate',
  getDsTableStructure: '/inks-dis/general/ds/table_structure',

  // 规格
  createCustomUom: '/swan-efs/material/create',
  updateCustomUom: '/swan-efs/material/update/un_native',
  getMaterialRatios: '/swan-efs/material/get/material_code/ratio',
  updateNativeUom: '/swan-efs/material/update',
  delCustomUom: '/swan-efs/material/delete',
  getAllMaterials: '/swan-efs/material/get_all_materials',
  getSelectedMeterials: '/swan-efs/material/get_db_all_materials',

  // app
  app: '/swan-cis/app/manager',
  appFields: '/swan-cis/app/manager/api_fields_query_detail',

  // 综合查询
  getIdentifyTypeOptions: '/swan-cis/product/id/checkbox/id',
  getTypeOfCodeOptions: '/swan-cis/product/id/checkbox/query_type_code',
  getAdvancedRulesOptions: '/swan-cis/product/id/checkbox/query_rule_name',
  getIdentifyTableHeader: '/swan-cis/product/id/table_titile',
  getIdentifyTableData: '/swan-cis/product/id',
  getSpecificationData: '/swan-cis/product/id/specification',

  // 选择来源配置
  getUomDsOptions: '/swan-efs/sys/conf/available_inuse_ds',
  saveSelectedUomDs: '/swan-efs/sys/conf/inuse_ds'
}

const config = {
  getDataSourceTree: {
    url: api.dataSource,
    method: 'get',
    mock: true,
    cache: true
  },
  getDataSourceDetail: {
    url: api.dataSourceDetail,
    method: 'get',
    paramsFields: ['uid'],
    mock: true
  },
  createDataSource: {
    url: api.dataSource,
    method: 'post',
    mock: true,
    dataFields: ['name', 'description', 'ds_type_uid', 'parent_uid', 'processor_data_props']
  },
  updateDataSource: {
    url: api.dataSource,
    method: 'patch',
    mock: true,
    dataFields: ['uid', 'name', 'description', 'ds_type_uid', 'parent_uid', 'processor_data_props']
  },
  deleteDataSource: {
    url: api.dataSource,
    method: 'delete',
    paramsFields: ['uid'],
    mock: true
  },
  getDataSourceType: {
    url: api.dataSourceType,
    method: 'get',
    mock: true,
    cache: true
  },
  getDsProfile: {
    url: api.getDsProfile,
    method: 'get',
    paramsFields: ['ds_uid'],
    mock: true
  },
  getDsTypeProfileList: {
    url: api.getDsTypeProfileList,
    method: 'get',
    paramsFields: ['ds_type_uid'],
    mock: true
  },
  createDsProfile: {
    url: api.dsConnectionProfile,
    method: 'post',
    mock: true,
    dataFields: ['ds_uid', 'connection_profile_uid']
  },
  updateDsProfile: {
    url: api.dsConnectionProfile,
    method: 'patch',
    mock: true,
    dataFields: ['uid', 'name', 'description', 'ds_type_uid', 'processor_data_props']
  },
  createDsTypeProfile: {
    url: api.createDsTypeProfile,
    method: 'post',
    mock: true,
    dataFields: ['name', 'description', 'ds_type_uid', 'processor_data_props']
  },
  getProcessorDetail: {
    url: api.getProcessorDetail,
    method: 'get',
    paramsFields: ['processor_uid'],
    mock: true,
    cache: true
  },
  // 数据采集流程
  getDaqProcessDefs: {
    url: api.getDaqProcessDefs,
    method: 'get',
    paramsFields: ['ds_name'],
    mock: true
  },
  deleteDaqProcessDef: {
    url: api.daqProcessDef,
    method: 'delete',
    mock: true,
    paramsFields: ['daq_process_def_uid']
  },
  createDaqProcessDef: {
    url: api.daqProcessDef,
    method: 'post',
    mock: true,
    dataFields: ['ds_uid', 'name', 'description']
  },
  updateDaqProcessDef: {
    url: api.daqProcessDef,
    method: 'patch',
    mock: true,
    dataFields: ['daq_process_def_uid', 'name', 'description']
  },
  createDapConfSchedule: {
    url: api.dapConfSchedule,
    method: 'post',
    mock: true,
    dataFields: ['daq_process_def_uid']
  },
  updateDapConfSchedule: {
    url: api.dapConfSchedule,
    method: 'patch',
    mock: true,
    dataFields: ['daq_process_def_uid']
    // dataFields: ['daq_process_def_uid', 'enabled', 'schedule_type', 'schedule_type_ext_details']
  },
  getDapTaskConfig: {
    url: api.getDapTaskConfig,
    method: 'get',
    paramsFields: ['daq_process_def_uid'],
    mock: true
  },
  createDapTaskConfig: {
    url: api.dapTaskConfig,
    method: 'post',
    mock: true,
    dataFields: ['daq_task_def_uid', 'processor_uid', 'processor_data_props', 'processor_static_data_props']
  },
  updateDapTaskConfig: {
    url: api.dapTaskConfig,
    method: 'patch',
    mock: true,
    dataFields: ['daq_task_def_uid', 'processor_uid', 'processor_data_props', 'processor_static_data_props']
  },
  // processor
  getProcessorList: {
    url: api.getProcessorList,
    method: 'get',
    mock: true,
    paramsFields: ['task_type']
  },
  createProcessor: {
    url: api.createProcessor,
    method: 'post',
    mock: true,
    dataFields: ['task_type', 'program_package_file_id']
  },
  parseProcessor: {
    url: api.parseProcessor,
    method: 'post',
    mock: true,
    dataFields: ['task_type', 'program_package_file_id']
  },
  // data target
  getDataTargetList: {
    url: api.dataTargetList,
    method: 'get',
    mock: true
  },
  createDataTarget: {
    url: api.dataTarget,
    method: 'post',
    mock: true,
    dataFields: ['parent_uid', 'name', 'description', 'srcs']
  },
  updateDataTarget: {
    url: api.dataTarget,
    method: 'patch',
    mock: true,
    dataFields: ['uid', 'name', 'description', 'srcs']
  },
  deleteDataTarget: {
    url: api.dataTarget,
    method: 'delete',
    mock: true,
    paramsFields: ['uid']
  },
  getDataSourceSds: {
    url: api.getDataSourceSds,
    method: 'get',
    mock: true,
    paramsFields: ['ds_uid']
  },
  getDataSourceDt: {
    url: api.getDataTarget,
    method: 'get',
    cache: true
  },
  getDataTarget: {
    url: api.getDataTarget,
    method: 'get'
  },
  getDtDefDetail: {
    url: api.getDtDefDetail,
    method: 'get',
    mock: true,
    paramsFields: ['uid']
  },
  // dtr process
  getDtrDefList: {
    url: api.getDtrDefList,
    method: 'get',
    paramsFields: ['dt_name'],
    mock: true
  },
  createDtrProcessDef: {
    url: api.dtrProcessDef,
    method: 'post',
    mock: true,
    dataFields: ['dt_uid', 'name', 'description', 'task_nodes']
  },
  deleteDtrProcessDef: {
    url: api.dtrProcessDef,
    method: 'delete',
    mock: true,
    paramsFields: ['dtr_process_def_uid']
  },
  getDtrDefDetail: {
    url: api.dtrProcessDefDetails,
    method: 'get',
    paramsFields: ['dtr_process_def_uid'],
    mock: true
  },
  getDtrConfDetail: {
    url: api.getDtrConfDetail,
    method: 'get',
    paramsFields: ['dtr_process_def_uid'],
    mock: true
  },
  updateDtrProcessDef: {
    url: api.dtrProcessDef,
    method: 'patch',
    mock: true,
    dataFields: ['uid', 'name', 'description', 'task_nodes']
  },
  createDtrConfSchedule: {
    url: api.dtrConfSchedule,
    method: 'post',
    mock: true,
    dataFields: ['dtr_process_def_uid']
  },
  updateDtrConfSchedule: {
    url: api.dtrConfSchedule,
    method: 'patch',
    mock: true,
    dataFields: ['dtr_process_def_uid']
  },
  getDtrTaskConfig: {
    url: api.dtrTaskConfig,
    method: 'get',
    paramsFields: ['dtr_task_def_uid'],
    mock: true
  },
  createDtrTaskConfig: {
    url: api.dtrTaskConfig,
    method: 'post',
    mock: true,
    dataFields: ['dtr_task_def_uid', 'processor_uid', 'processor_data_props', 'processor_static_data_props']
  },
  updateDtrTaskConfig: {
    url: api.dtrTaskConfig,
    method: 'patch',
    mock: true,
    dataFields: ['dtr_task_def_uid', 'processor_uid', 'processor_data_props', 'processor_static_data_props']
  },
  // attribute
  getAvailableDS: {
    url: api.getAvailableDS,
    method: 'get',
    mock: true
  },
  getInUseDS: {
    url: api.inUseDS,
    method: 'get',
    mock: true
  },
  saveInUseDS: {
    url: api.inUseDS,
    method: 'post',
    dataFields: ['ds_dtos'],
    mock: true
  },
  getDSAtrrs: {
    url: api.getDSAtrrs,
    method: 'get',
    paramsFields: ['ds_uid'],
    mock: true
  },
  getSrcTargetAtrrs: {
    url: api.srcTargetAttrs,
    method: 'get',
    mock: true
  },
  saveSrcTargetAtrrs: {
    url: api.srcTargetAttrs,
    method: 'post',
    dataFields: ['target_attrs'],
    mock: true
  },
  getTargetAtrrs: {
    url: api.targetAttrs,
    method: 'get',
    mock: true
  },
  createTargetAtrr: {
    url: api.targetAttr,
    method: 'post',
    mock: true
  },
  updateTargetAtrr: {
    url: api.targetAttr,
    method: 'patch',
    dataFields: ['target_attr_uid'],
    mock: true
  },
  deleteTargetAtrr: {
    url: api.targetAttr,
    method: 'delete',
    dataFields: ['target_attr_uid'],
    mock: true
  },
  changeTargetStatus: {
    url: api.changeTargetStatus,
    method: 'patch',
    dataFields: ['target_attr_uid', 'target_attr_status'],
    mock: true
  },

  // daq execution
  getDaqExecList: {
    url: api.getDaqExecList,
    method: 'get',
    mock: true,
    paramsSerializer (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  },
  deleteDaqProcessInst: {
    url: api.deleteDaqProcessInst,
    method: 'delete',
    paramsFields: ['daq_process_inst_uid']
  },
  getDaqConnectDetail: {
    url: api.getDaqConnectDetail,
    method: 'get',
    paramsFields: ['daq_process_inst_uid'],
    mock: true
  },
  getDaqExtractDetail: {
    url: api.getDaqExtractDetail,
    method: 'get',
    paramsFields: ['daq_process_inst_uid'],
    mock: true
  },
  getDaqLoadDetail: {
    url: api.getDaqLoadDetail,
    method: 'get',
    paramsFields: ['daq_process_inst_uid'],
    mock: true
  },
  getDaqPublishDetail: {
    url: api.getDaqPublishDetail,
    method: 'get',
    paramsFields: ['daq_process_inst_uid'],
    mock: true
  },
  getDaqOutTableData: {
    url: api.getDaqOutTableData,
    method: 'get',
    paramsFields: ['daq_task_inst_uid', 'output_table_name'],
    mock: true
  },
  getDtrOutTableData: {
    url: api.getDtrOutTableData,
    method: 'get',
    paramsFields: ['dtr_task_inst_uid', 'output_table_name'],
    mock: true
  },
  getDqrExecList: {
    url: api.getDqrExecList,
    method: 'get',
    mock: true,
    paramsSerializer (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  },
  deleteDtrProcessInst: {
    url: api.deleteDtrProcessInst,
    method: 'delete',
    paramsFields: ['dtr_process_inst_uid']
  },
  getDtrTransformDetail: {
    url: api.getDtrTransformDetail,
    method: 'get',
    paramsFields: ['dtr_process_inst_uid', 'dtr_task_def_uid'],
    mock: true
  },
  getEbExecList: {
    url: api.getEbExecList,
    method: 'get',
    mock: true
  },
  getEbExecutionDetail: {
    url: api.getEbExecutionDetail,
    method: 'get',
    paramsFields: ['eb_process_inst_uid'],
    mock: true
  },
  createDaqProcessInst: {
    url: api.daqProcessInst,
    method: 'post',
    dataFields: ['ds_uid', 'params'],
    mock: true
  },

  getEbPrimarySrc: {
    url: api.ebPrimarySrc,
    method: 'get',
    paramsFields: ['eb_process_def_Uid'],
    mock: true
  },
  saveEbPrimarySrc: {
    url: api.ebPrimarySrc,
    method: 'post',
    dataFields: ['eb_process_def_uid', 'primary_ds_uid', 'list_of_identity_attr_uid_of_primary_ds'],
    mock: true
  },
  getEbSecondarySrc: {
    url: api.ebSecondarySrc,
    method: 'get',
    paramsFields: ['eb_process_def_Uid'],
    mock: true
  },
  saveEbSecondarySrc: {
    url: api.ebSecondarySrc,
    method: 'post',
    dataFields: ['eb_process_def_uid', 'identity_attrs_of_secondary_ds'],
    mock: true
  },
  getEbSchedule: {
    url: api.ebSchedule,
    method: 'get',
    paramsFields: ['eb_process_def_Uid'],
    mock: true
  },
  saveEbSchedule: {
    url: api.ebSchedule,
    method: 'post',
    dataFields: ['eb_process_def_uid'],
    mock: true
  },
  getTargetAttrsConfigs: {
    url: api.getTargetAttrsConfigs,
    method: 'get',
    paramsFields: ['ebProcessDefUid'],
    mock: true
  },
  getEbTask: {
    url: api.ebTask,
    method: 'get',
    paramsFields: ['eb_task_def_uid'],
    mock: true
  },
  saveEbTask: {
    url: api.ebTask,
    method: 'post',
    mock: true
  },
  getEbTaskSrcAttrs: {
    url: api.getEbTaskSrcAttrs,
    method: 'get',
    paramsFields: ['eb_task_def_uid'],
    mock: true
  },
  getEbTaskTargetAttrs: {
    url: api.getEbTaskTargetAttrs,
    method: 'get',
    paramsFields: ['eb_task_def_uid'],
    mock: true
  },

  getEntityQuerySetting: {
    url: api.getEntityQuerySetting,
    method: 'get',
    mock: true
  },
  getEntityQueryData: {
    url: api.getEntityQueryData,
    method: 'get',
    mock: true,
    paramsSerializer (params) {
      return qs.stringify(params, { arrayFormat: 'repeat' })
    }
  },

  getDaqProcessConfigGraph: {
    url: api.getDaqProcessConfigGraph,
    method: 'get',
    paramsFields: ['daq_process_def_uid'],
    mock: true
  },
  getDtrProcessConfigGraph: {
    url: api.getDtrProcessConfigGraph,
    method: 'get',
    paramsFields: ['dtr_process_def_uid'],
    mock: true
  },
  getEbProcessDefImg: {
    url: api.getEbProcessDefImg,
    method: 'get',
    paramsFields: ['eb_process_def_uid'],
    mock: true
  },
  getEbTaskDefImg: {
    url: api.getEbTaskDefImg,
    method: 'get',
    paramsFields: ['eb_task_def_uid'],
    mock: true
  },
  validateExpression: {
    url: api.validateExpression,
    method: 'post',
    dataFields: ['program']
  },
  getAttrMappingSrc: {
    url: api.getAttrMappingSrc,
    method: 'get',
    paramsFields: ['eb_task_def_uid']
  },

  getExportStatus: {
    url: api.getExportStatus,
    method: 'post',
    argsMode: 'paramsAndData',
    dataFields: ['entity_uid']
  },
  getExportFile: {
    url: api.getExportFile,
    method: 'get',
    paramsFields: ['export_task_uid']
  },

  getNationalGeography: {
    url: api.getNationalGeography,
    method: 'get',
    paramsFields: ['dict_item_id']
  },
  getNationalGeographyByName: {
    url: api.getNationalGeographyByName,
    method: 'get',
    paramsFields: ['name']
  },
  getMarsGeography: {
    url: api.getMarsGeography,
    method: 'get'
  },
  getStoreChannel: {
    url: api.getStoreChannel,
    method: 'get'
  },
  getStoreChain: {
    url: api.getStoreChain,
    method: 'get',
    paramsFields: ['dict_item_id']
  },
  getStoreChainByName: {
    url: api.getStoreChainByName,
    method: 'get',
    paramsFields: ['name']
  },
  getGeograogyStores: {
    url: api.getGeograogyStores,
    method: 'get'
  },
  getMarsGeograogyStores: {
    url: api.getMarsGeograogyStores,
    method: 'get'
  },
  getChannelStores: {
    url: api.getChannelStores,
    method: 'get'
  },
  getChainStores: {
    url: api.getChainStores,
    method: 'get'
  },
  getChains: {
    url: api.getChains,
    method: 'get'
  },
  // template
  parseDsFromExcelApi: {
    url: api.parseDsFromExcelApi,
    method: 'post',
    argsMode: 'paramsAndData'
  },
  parseDsFromCsvApi: {
    url: api.parseDsFromCsvApi,
    method: 'post',
    argsMode: 'paramsAndData'
  },
  testProcessorConnection: {
    url: api.testProcessorConnection,
    method: 'post',
    dataFields: ['processor_uid', 'processor_data_props']
  },
  getDataTargetDetail: {
    url: api.getDataTargetDetail,
    method: 'get',
    paramsFields: ['dt_uid']
  },
  getDtSrcDataFields: {
    url: api.getDtSrcDataFields,
    method: 'get',
    paramsFields: ['src_type', 'src_uid']
  },
  validateTransformSql: {
    url: api.validateTransformSql,
    method: 'post',
    dataFields: ['sql']
  },
  getDtSrcSdsList: {
    url: api.getDtSrcSdsList,
    method: 'get',
    paramsFields: ['dt_uid']
  },
  getDtSrcDtList: {
    url: api.getDtSrcDtList,
    method: 'get',
    paramsFields: ['dt_uid']
  },
  getSrcIntermediate: {
    url: api.getSrcIntermediate,
    method: 'get',
    paramsFields: ['dt_uid', 'dtr_task_def_uid']
  },
  getDsTableStructure: {
    url: api.getDsTableStructure,
    method: 'get',
    paramsFields: ['ds_uid']
  },

  createCustomUom: {
    url: api.createCustomUom,
    method: 'post'
  },
  getMaterialRatios: {
    url: api.getMaterialRatios,
    method: 'get',
    paramsFields: ['material_code', 'numerator', 'denominator']
  },
  updateCustomUom: {
    url: api.updateCustomUom,
    method: 'patch',
    dataFields: ['id']
  },
  updateNativeUom: {
    url: api.updateNativeUom,
    method: 'patch',
    dataFields: ['id']
  },
  delCustomUom: {
    url: api.delCustomUom,
    method: 'delete',
    paramsFields: ['id']
  },
  getAllMaterials: {
    url: api.getAllMaterials,
    method: 'get',
    paramsFields: ['material_code'],
    cache: true
  },
  getSelectedMeterials: {
    url: api.getSelectedMeterials,
    method: 'get',
    paramsFields: ['id']
  },

  // app
  createApp: {
    url: api.app,
    method: 'post'
  },
  updateApp: {
    url: api.app,
    method: 'put'
  },
  delApp: {
    url: api.app,
    method: 'delete',
    paramsFields: ['app_id']
  },
  getAppDetail: {
    url: api.app,
    method: 'get',
    paramsFields: ['app_id']
  },
  getAppFields: {
    url: api.appFields,
    method: 'get',
    paramsFields: ['app_id', 'api_key']
  },
  saveAppFields: {
    url: api.appFields,
    method: 'post',
    paramsFields: ['app_id', 'api_key']
  },

  // identify query
  getIdentifyTypeOptions: {
    url: api.getIdentifyTypeOptions,
    method: 'get',
    cache: true
  },
  getTypeOfCodeOptions: {
    url: api.getTypeOfCodeOptions,
    method: 'get',
    paramsFields: ['product_id_key'],
    cache: true
  },
  getAdvancedRulesOptions: {
    url: api.getAdvancedRulesOptions,
    method: 'get',
    paramsFields: ['product_id_key', 'query_type_code'],
    cache: true
  },
  getIdentifyTableHeader: {
    url: api.getIdentifyTableHeader,
    method: 'get',
    cache: true
  },
  getIdentifyTableData: {
    url: api.getIdentifyTableData,
    method: 'post',
    argsMode: 'paramsAndData',
    dataFields: ['code']
  },
  getSpecificationData: {
    url: api.getSpecificationData,
    method: 'get'
  },

  getUomDsOptions: {
    url: api.getUomDsOptions,
    method: 'get',
    paramsFields: ['applicant_type'],
    mock: true
  },
  saveSelectedUomDs: {
    url: api.saveSelectedUomDs,
    method: 'post',
    paramsFields: ['applicant_type'],
    argsMode: 'paramsAndData'
  }
}

export default config
