import Gateway from '@/api/gateway'
const d1AdminServer = ''

const getDictStrategyRecordsJsonDef = {
  content: {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        uid: {
          type: 'string'
        },
        status: {
          type: 'string'
        },
        start_time: {
          type: 'string',
          formater: (val) => new Date(val)
        },
        done_time: {
          type: 'string',
          formater: (val) => new Date(val)
        },
        failed_time: {
          type: 'string',
          formater: (val) => new Date(val)
        }
      }
    }
  },
  total: {
    type: 'number',
    property: 'total_elements'
  }
}

const dataSourceTreeJsonDef = {
  label: {
    type: 'string',
    property: 'name'
  },
  id: {
    type: 'string',
    property: 'uid'
  },
  children: {
    type: 'array',
    property: 'children',
    defaultValue: [],
    items: 'tree'
  }
}

const dictCatListJsonDef = {
  id: {
    type: 'number',
    property: 'uid'
  },
  name: {
    type: 'string',
    defaultValue: 'ROOT'
  },
  type: {
    type: 'string',
    defaultValue: 'root'
  },
  children: {
    type: 'array',
    items: {
      type: 'object',
      properties: {
        id: {
          type: 'number',
          property: 'uid'
        },
        name: {
          type: 'string'
        },
        description: {
          type: 'string'
        },
        type: {
          type: 'string',
          defaultValue: 'category'
        },
        children: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              id: {
                type: 'number',
                property: 'uid'
              },
              name: {
                type: 'string'
              },
              description: {
                type: 'string'
              },
              type: {
                type: 'string'
              },
              children: {
                type: 'array',
                items: 'tree'
              }
            }
          }
        }
      }
    }
  }
}

const dictCatJsonDef = {
  reqBody: {
    id: {
      type: 'number',
      property: 'uid'
    },
    name: {
      type: 'string'
    },
    description: {
      type: 'string'
    }
  }
}

const baseDictJsonDef = {
  id: {
    type: 'number',
    property: 'uid'
  },
  name: {
    type: 'string'
  },
  description: {
    type: 'string'
  },
  parentId: {
    type: 'number',
    property: 'parent_uid'
  },
  categoryId: {
    type: 'number',
    property: 'dictionary_category_uid'
  }
}

const dictStructureJsonDef = {
  reqBody: baseDictJsonDef,
  response: baseDictJsonDef
}

const baseDictValueJsonDef = {
  id: {
    type: 'number',
    property: 'uid'
  },
  name: {
    type: 'string',
    property: 'value'
  },
  description: {
    type: 'string',
    property: 'label'
  },
  parentId: {
    type: 'number',
    property: 'parent_uid'
  },
  categoryId: {
    type: 'number',
    property: 'dictionary_category_uid'
  }
}

const saveDictValueJsonDef = {
  reqBody: baseDictValueJsonDef,
  response: baseDictValueJsonDef
}

const baseDictStrategy = {
  build_strategy_type: {
    type: 'string'
  },
  dictionary_category_uid: {
    type: 'number'
  },
  enabled: {
    type: 'boolean'
  },
  schedule_type: {
    type: 'string'
  },
  schedule_type_ext_details: {
    type: 'string'
  },
  ds_uid: {
    type: 'number',
    property: 'sql_build_strategy_content.ds_uid'
  },
  sql_statement: {
    type: 'number',
    property: 'sql_build_strategy_content.sql_statement'
  },
  fixed_value: {
    type: 'string',
    property: 'fixed_value_build_strategy_content.fixed_value'
  }
}

const saveDictStrategyJsonDef = {
  reqBody: baseDictStrategy
}

const testDictStrategyJsonDef = {
  reqBody: {
    build_strategy_type: {
      type: 'string'
    },
    ds_uid: {
      type: 'number',
      property: 'sql_build_strategy_content.ds_uid'
    },
    sql_statement: {
      type: 'number',
      property: 'sql_build_strategy_content.sql_statement'
    }
  }
}

const baseDefaultValContentJsonDef = {
  id: {
    type: 'number',
    property: 'uid'
  },
  name: {
    type: 'string',
    property: 'value'
  },
  description: {
    type: 'string',
    property: 'label'
  },
  parentId: {
    type: 'number',
    property: 'parent_uid'
  },
  categoryId: {
    type: 'number',
    property: 'defaults_category_uid'
  }
}

const saveDefaultValContentJsonDef = {
  reqBody: baseDefaultValContentJsonDef,
  response: baseDefaultValContentJsonDef
}

const getDictStrategyJsonDef = baseDictStrategy

const dfConfig = {
  advanced: {
    type: 'object'
  }
}

export const api = {
  getDataSourceList: `${d1AdminServer}/admin/df/def/list`,
  dataSource: '/admin/ds',
  // getDataSource: `${d1AdminServer}/d1/datasource/select-property`,
  // addDataSource: `${d1AdminServer}/d1/datasource/add`,
  // editDataSource: `${d1AdminServer}/d1/datasource/edit-property`,
  refreshDataSource: `${d1AdminServer}/admin/ds/refresh_one`,
  refreshAllDataSource: `${d1AdminServer}/admin/ds/refresh_all`,
  // deleteD1DataSource: `${d1AdminServer}/d1/datasource/delete`,
  testDataSourceConnection: `${d1AdminServer}/admin/ds/test_connection`,
  // dfk
  dataFacet: '/admin/df/def',
  // addDfk: `${d1AdminServer}/d1/datasource/add-dfkey`,
  // updateDfk: `${d1AdminServer}/d1/datasource/update-dfkey`,
  // deleteDfk: `${d1AdminServer}/d1/datasource/delete-dfkey`,
  // getDfk: `${d1AdminServer}/d1/datasource/basic-dfkey-info`,
  getFormTableSetting: `${d1AdminServer}/admin/df/conf`,
  refreshFormTableSetting: `${d1AdminServer}/admin/df/conf/refresh_df_available_data_fields`,
  saveFormTableSetting: `${d1AdminServer}/admin/df/conf`,
  copyFormTableSetting: `${d1AdminServer}/admin/df/conf/replication`,

  dictList: `${d1AdminServer}/d1/dict/manage/domain`,
  updateDictList: `${d1AdminServer}/d1/dict/manage/domain-update`,
  deleteDictField: `${d1AdminServer}/d1/dict/manage/value`,
  dictStrategy: `${d1AdminServer}/admin/dictionary/build_process_def`,
  testDictStrategy: `${d1AdminServer}/admin/dictionary/build_process_def/test`,
  getDictExecutionRecords: `${d1AdminServer}/admin/dictionary/build_process_inst/query`,
  getDictStrategyByCat: `${d1AdminServer}/admin/dictionary/build_process_def/query_by_dictionary_category`,
  // getDictStrategy: `${d1AdminServer}/admin/dictionary/build_process_def`,
  getDictConfiguration: `${d1AdminServer}/d1/form-dict-configuration`,

  defaultStrategy: `${d1AdminServer}/admin/defaults/build_process_def`,
  // getDefaultValStrategy: `${d1AdminServer}/d1/defaults-configuration`,
  getDefaultValStrategyByCat: `${d1AdminServer}/admin/defaults/build_process_def/query_by_defaults_category`,
  testDefaultValStrategy: `${d1AdminServer}/admin/defaults/build_process_def/test`,
  getDefaultValExecutionRecords: `${d1AdminServer}/admin/defaults/build_process_inst/query`,

  getDataFacetTags: `${d1AdminServer}/general/tags/list`,
  testProcessLogicSql: `${d1AdminServer}/admin/ds/test_query_statement`,

  getDomainAndItemTree: `${d1AdminServer}/admin/dictionary/structures/list`,
  dictCategory: `${d1AdminServer}/admin/dictionary/categories`,
  // createDictDomain: `${d1AdminServer}/d1/def/dictionary/domain/create`,
  // updateDictDomain: `${d1AdminServer}/d1/def/dictionary/domain/update`,
  // deleteDictDomain: `${d1AdminServer}/d1/def/dictionary/domain/delete`,

  dictStructure: `${d1AdminServer}/admin/dictionary/structures`,
  // createDictItem: `${d1AdminServer}/d1/def/dictionary/item/create`,
  // updateDictItem: `${d1AdminServer}/d1/def/dictionary/item/update`,
  // deleteDictItem: `${d1AdminServer}/d1/def/dictionary/item/delete`,

  getDomainAndValueTree: `${d1AdminServer}/admin/dictionary/content/list`,
  dictContent: `${d1AdminServer}/admin/dictionary/content`,
  // createDictValue: `${d1AdminServer}/d1/conf/dictionary/create`,
  // updateDictValue: `${d1AdminServer}/d1/conf/dictionary/update`,
  // deleteDictValue: `${d1AdminServer}/d1/conf/dictionary/delete`

  // 默认值
  defaultValCategory: `${d1AdminServer}/admin/defaults/categories`,
  getDefaultValCategories: `${d1AdminServer}/admin/defaults/categories/list`,
  getDefaultValContents: `${d1AdminServer}/admin/defaults/content/list`,
  defaultValContent: `${d1AdminServer}/admin/defaults/content`,

  // 通用
  getJobFaildReson: `${d1AdminServer}/general/job_failed_reasons`,

  getIndexs: `${d1AdminServer}/admin/df/conf/indexes`,
  getResourceStructures: `${d1AdminServer}/admin/resource/structures/list`,

  // label
  getDictContentsByParent: `${d1AdminServer}/admin/dictionary/content/next_level`,
  getDictContentsByLabel: `${d1AdminServer}/admin/dictionary/content/query`,

  getOperations: '/operations/paging_query',
  getOperationDetail: '/operations/details'
}

const config = {
  getDictContentsByParent: {
    url: api.getDictContentsByParent,
    method: 'get',
    gateway: new Gateway(dictCatListJsonDef)
  },
  getDictContentsByLabel: {
    url: api.getDictContentsByLabel,
    method: 'get',
    gateway: new Gateway(dictCatListJsonDef)
  },
  getDataSourceList: {
    url: api.getDataSourceList,
    method: 'get',
    mock: false,
    paramsFields: ['show_df_only'],
    gateway: new Gateway(dataSourceTreeJsonDef)
  },
  getDataSource: {
    url: api.dataSource,
    method: 'get',
    paramsFields: ['uid']
  },
  addDataSource: {
    url: api.dataSource,
    method: 'post'
  },
  editDataSource: {
    url: api.dataSource,
    method: 'patch'
  },
  deleteD1DataSource: {
    url: api.dataSource,
    method: 'delete',
    paramsFields: ['uid']
  },
  refreshDataSource: {
    url: api.refreshDataSource,
    method: 'post',
    paramsFields: ['ds_uid']
  },
  refreshAllDataSource: {
    url: api.refreshAllDataSource,
    method: 'post'
  },
  // dfk
  addDfk: {
    url: api.dataFacet,
    method: 'put'
  },
  updateDfk: {
    url: api.dataFacet,
    method: 'put'
  },
  deleteDfk: {
    url: api.dataFacet,
    method: 'delete',
    paramsFields: ['uid']
  },
  getDfk: {
    url: api.dataFacet,
    method: 'get',
    paramsFields: ['uid'],
    mock: false
  },
  getFormTableSetting: {
    url: api.getFormTableSetting,
    method: 'get',
    paramsFields: ['df_uid'],
    mock: false,
    gateway: new Gateway(dfConfig)
  },
  refreshFormTableSetting: {
    url: api.refreshFormTableSetting,
    method: 'post',
    paramsFields: ['df_uid']
  },
  saveFormTableSetting: {
    url: api.saveFormTableSetting,
    method: 'put',
    gateway: new Gateway(dfConfig)
  },
  getDictList: {
    url: api.dictList,
    method: 'get'
  },
  saveDictList: {
    url: api.dictList,
    method: 'post'
  },
  deleteDictList: {
    url: api.dictList,
    method: 'delete'
  },
  updateDictList: {
    url: api.updateDictList,
    method: 'post'
  },
  deleteDictField: {
    url: api.deleteDictField,
    method: 'delete'
  },

  saveDictStrategy: {
    url: api.dictStrategy,
    method: 'post',
    gateway: new Gateway(saveDictStrategyJsonDef, true)
  },
  updateDictStrategy: {
    url: api.dictStrategy,
    method: 'patch',
    gateway: new Gateway(saveDictStrategyJsonDef, true)
  },
  testDictStrategy: {
    url: api.testDictStrategy,
    method: 'post',
    mock: false,
    gateway: new Gateway(testDictStrategyJsonDef, true, true)
  },
  getDictStrategy: {
    url: api.dictStrategy,
    method: 'get',
    gateway: new Gateway(getDictStrategyJsonDef)
  },
  getDictStrategyByCat: {
    url: api.getDictStrategyByCat,
    method: 'get',
    gateway: new Gateway(getDictStrategyJsonDef)
  },
  getDictConfiguration: {
    url: api.getDictConfiguration,
    method: 'get',
    paramsFields: ['field_form_df_key', 'field_form_field_key']
  },
  getDictExecutionRecords: {
    url: api.getDictExecutionRecords,
    method: 'get',
    mock: false,
    paramsFields: ['process_def_uid'],
    gateway: new Gateway(getDictStrategyRecordsJsonDef)
  },

  saveDefaultValStrategy: {
    url: api.defaultStrategy,
    method: 'post',
    gateway: new Gateway(saveDictStrategyJsonDef, true)
  },
  updateDefaultValStrategy: {
    url: api.defaultStrategy,
    method: 'patch',
    gateway: new Gateway(saveDictStrategyJsonDef, true)
  },
  getDefaultValStrategy: {
    url: api.defaultStrategy,
    method: 'get',
    // paramsFields: ['process_def_uid'],
    gateway: new Gateway(getDictStrategyJsonDef)
  },
  getDefaultValStrategyByCat: {
    url: api.getDefaultValStrategyByCat,
    method: 'get',
    // paramsFields: ['defaults_category_uid'],
    gateway: new Gateway(getDictStrategyJsonDef)
  },
  testDefaultValStrategy: {
    url: api.testDefaultValStrategy,
    method: 'post',
    gateway: new Gateway(testDictStrategyJsonDef, true, true)
  },
  getDefaultValExecutionRecords: {
    url: api.getDefaultValExecutionRecords,
    method: 'get',
    mock: false,
    paramsFields: ['process_def_uid'],
    gateway: new Gateway(getDictStrategyRecordsJsonDef)
  },

  getDataFacetTags: {
    url: api.getDataFacetTags,
    method: 'get',
    cache: true,
    mock: false
  },
  testProcessLogicSql: {
    url: api.testProcessLogicSql,
    method: 'post',
    dataFields: ['ds_uid', 'query_statement']
  },
  testDataSourceConnection: {
    url: api.testDataSourceConnection,
    method: 'post',
    dataFields: ['ds_type', 'connection_profile_props'],
    disableMessage: true
  },

  getDomainAndItemTree: {
    url: api.getDomainAndItemTree,
    method: 'get',
    cache: true,
    mock: false,
    gateway: new Gateway(dictCatListJsonDef)
  },
  getDictCategories: {
    url: api.dictCategory,
    method: 'get'
  },
  createDictDomain: {
    url: api.dictCategory,
    method: 'post'
  },
  updateDictDomain: {
    url: api.dictCategory,
    method: 'patch',
    gateway: new Gateway(dictCatJsonDef, true)
  },
  deleteDictDomain: {
    url: api.dictCategory,
    method: 'delete',
    paramsFields: ['uid']
  },
  createDictItem: {
    url: api.dictStructure,
    method: 'post',
    gateway: new Gateway(dictStructureJsonDef, true)
  },
  updateDictItem: {
    url: api.dictStructure,
    method: 'patch',
    gateway: new Gateway(dictStructureJsonDef, true)
  },
  deleteDictItem: {
    url: api.dictStructure,
    method: 'delete',
    paramsFields: ['uid']
  },

  getDomainAndValueTree: {
    url: api.getDomainAndValueTree,
    method: 'get',
    cache: true,
    mock: false,
    gateway: new Gateway(dictCatListJsonDef)
  },
  createDictValue: {
    url: api.dictContent,
    method: 'post',
    gateway: new Gateway(saveDictValueJsonDef, true)
  },
  getDictValue: {
    url: api.dictContent,
    method: 'get',
    paramsFields: ['dictionary_category_uid'],
    gateway: new Gateway(dictCatListJsonDef)
  },
  updateDictValue: {
    url: api.dictContent,
    method: 'patch',
    gateway: new Gateway(saveDictValueJsonDef, true)
  },
  deleteDictValue: {
    url: api.dictContent,
    method: 'delete',
    paramsFields: ['uid']
  },

  createDefaultValCategory: {
    url: api.defaultValCategory,
    method: 'post'
  },
  updateDefaultValCategory: {
    url: api.defaultValCategory,
    method: 'patch',
    gateway: new Gateway({
      reqBody: {
        id: {
          type: 'number',
          property: 'uid'
        }
      }
    }, true)
  },
  deleteDefaultValCategory: {
    url: api.defaultValCategory,
    method: 'delete',
    paramsFields: ['uid']
  },
  getDefaultValCategories: {
    url: api.getDefaultValCategories,
    method: 'get',
    gateway: new Gateway({
      id: {
        type: 'number',
        property: 'uid'
      },
      children: {
        type: 'array',
        items: 'tree'
      }
    })
  },
  getDefaultValContents: {
    url: api.getDefaultValContents,
    method: 'get',
    cache: true,
    gateway: new Gateway({
      id: {
        type: 'number',
        property: 'uid'
      },
      children: {
        type: 'array',
        items: 'tree'
      }
    })
  },
  getDefaultValContent: {
    url: api.defaultValContent,
    method: 'get',
    paramsFields: ['defaults_category_uid'],
    gateway: new Gateway({
      id: {
        type: 'number',
        property: 'uid'
      },
      children: {
        type: 'array',
        items: 'tree'
      }
    })
  },
  createDefaultValContent: {
    url: api.defaultValContent,
    method: 'post',
    gateway: new Gateway(saveDefaultValContentJsonDef, true)
  },
  updateDefaultValContent: {
    url: api.defaultValContent,
    method: 'patch',
    gateway: new Gateway(saveDefaultValContentJsonDef, true)
  },
  deleteDefaultValContent: {
    url: api.defaultValContent,
    method: 'delete',
    paramsFields: ['uid']
  },
  copyFormTableSetting: {
    url: api.copyFormTableSetting,
    method: 'post',
    paramsFields: ['source_df_uid', 'destination_df_uid'],
    gateway: new Gateway(dfConfig)
  },

  getJobFaildReson: {
    url: api.getJobFaildReson,
    method: 'get',
    paramsFields: ['job_uid', 'job_category'],
    mock: false,
    cache: true
  },
  getIndexs: {
    url: api.getIndexs,
    method: 'post',
    paramsFields: ['df_uid'],
    disableMessage: true
  },
  getResourceStructures: {
    url: api.getResourceStructures,
    method: 'get',
    cache: true,
    mock: false
  },

  getOperations: {
    url: api.getOperations
  },
  getOperationDetail: {
    url: api.getOperationDetail,
    paramsFields: ['request_id']
  }
}

export default config
