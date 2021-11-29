// import { axios } from '@/utils/request'
import Gateway from '@/api/gateway'

const dfkListJsonDefine = {
  response: {
    content: {
      type: 'array',
      property: '',
      items: {
        type: 'object',
        properties: {
          name: {
            type: 'string',
            property: 'df_name'
          },
          key: {
            type: 'string',
            property: 'df_key'
          },
          description: {
            type: 'string',
            property: 'df_description'
          },
          uid: {
            type: 'string',
            property: 'df_uid'
          }
        }
      }
    },
    total: {
      type: 'number',
      property: 'total_elements'
    }
  }
}

const serverConfig = require('serverConfig')
const fileUrl = serverConfig.VUE_APP_FILE_API_BASE_URL || process.env.VUE_APP_FILE_API_BASE_URL

export const api = {
  userFormTableSetting: '/df/conf',
  exportExcelTableData: '/df/async_export_jobs/excel',
  exportCsvTableData: '/df/async_export_jobs/csv',
  exportCsvAndCompressTableData: '/df/async_export_jobs/csv_and_compress',
  exportResult: '/df/async_export_jobs',
  queryExportJobs: '/df/async_export_jobs/query',
  cancelAsyncExportJob: `/df/async_export_jobs/cancel`,
  queryTable: '/df/content/query',
  download: `${fileUrl}/fs/storage/download`,
  upload: `${fileUrl}/fs/storage/upload`,
  getUserDataFacets: '/deployment/df/query',
  getAllTags: '/general/tags/list',
  getGroupByFields: '/df/conf/table/group_by_fields',
  getChooseFields: '/df/conf/table',
  getGraphData: '/df/content/query/graph',
  getUserDictContentByParent: '/dictionary/content/next_level',
  getUserDictContentByLabel: '/dictionary/content/query',
  getUserDictContentBySelect: '/dictionary/content/with_selected'
}

const config = {
  getUserDictContentBySelect: {
    url: api.getUserDictContentBySelect,
    method: 'get',
    apiTypes: ['deployment', 'openapi']
  },
  getUserDictContentByParent: {
    url: api.getUserDictContentByParent,
    method: 'get',
    apiTypes: ['deployment', 'openapi']
  },
  getUserDictContentByLabel: {
    url: api.getUserDictContentByLabel,
    method: 'get',
    apiTypes: ['deployment', 'openapi']
  },
  getAllTags: {
    url: api.getAllTags,
    method: 'get',
    cache: true
  },
  getUserDataFacets: {
    url: api.getUserDataFacets,
    method: 'get',
    mock: false,
    gateway: new Gateway(dfkListJsonDefine, true)
  },
  getFormTableSettings: {
    url: api.userFormTableSetting,
    method: 'get',
    paramsFields: ['df_key'],
    apiTypes: ['deployment', 'openapi']
  },
  getGroupByFields: {
    url: api.getGroupByFields,
    method: 'get',
    paramsFields: ['df_key'],
    apiTypes: ['deployment', 'openapi']
  },
  getChooseFields: {
    url: api.getChooseFields,
    method: 'get',
    paramsFields: ['df_key'],
    apiTypes: ['deployment', 'openapi']
  },
  getGraphData: {
    url: api.getGraphData,
    method: 'get',
    paramsFields: ['df_key', 'kpi_field_name', 'dimension_field_name'],
    apiTypes: ['deployment', 'openapi']
  },
  queryTable: {
    url: api.queryTable,
    method: 'post',
    paramsFields: ['df_key'],
    argsMode: 'paramsAndData',
    apiTypes: ['deployment', 'openapi'],
    disableMessage: true
  },
  exportExcelTableData: {
    url: api.exportExcelTableData,
    method: 'post',
    paramsFields: ['df_key'],
    argsMode: 'paramsAndData',
    apiTypes: ['deployment', 'openapi']
  },
  exportCsvTableData: {
    url: api.exportCsvTableData,
    method: 'post',
    paramsFields: ['df_key'],
    argsMode: 'paramsAndData',
    apiTypes: ['deployment', 'openapi']
  },
  exportCsvAndCompressTableData: {
    url: api.exportCsvAndCompressTableData,
    method: 'post',
    paramsFields: ['df_key'],
    argsMode: 'paramsAndData',
    apiTypes: ['deployment', 'openapi']
  },
  getExportResult: {
    url: api.exportResult,
    method: 'get',
    paramsFields: ['uid'],
    apiTypes: ['deployment', 'openapi']
  },
  deleteAsyncExportJob: {
    url: api.exportResult,
    method: 'delete',
    paramsFields: ['uid'],
    apiTypes: ['deployment', 'openapi']
  },
  cancelAsyncExportJob: {
    url: api.cancelAsyncExportJob,
    method: 'patch',
    paramsFields: ['uid'],
    apiTypes: ['deployment', 'openapi']
  },
  queryExportJobs: {
    url: api.queryExportJobs,
    method: 'get',
    apiTypes: ['deployment', 'openapi']
  },
  download: api.download,
  upload: api.upload
}

export default config
