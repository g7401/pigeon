import { axios } from '@/utils/request'

const config = {
    createModelType: {
        url: '/lotus-mbs/model_type',
        method: 'post'
    },
    editModelType: {
        url: '/lotus-mbs/model_type',
        method: 'put'
    },
    getAllModelType: {
        url: '/lotus-mbs/model_type',
        method: 'get'
    },
    getModelType: {
        url: '/lotus-mbs/model_type/model_type_id',
        method: 'get',
        paramsFields: ['model_type_id']
    },
    downAmeeProcessorTemplate: {
        url: '/lotus-mbs/model_type/amee_processor_template',
        method: 'get'
    },
    downAmeeProcessorTemplateField: {
        url: '/lotus-mbs/common/storage/download',
        method: 'get',
        paramsFields: ['file_id']
    },
    createModel: {
        url: '/lotus-mbs/model_definition',
        method: 'post'
    },
    updateModel: {
        url: '/lotus-mbs/model_definition',
        method: 'put'
    },
    uploadInstance: {
        url: '/lotus-mms/model_instance/upload_instance',
        method: 'post'
    },
    deploy: {
        url: '/lotus-mms/model_instance/deploy',
        method: 'post'
    },
    getModel: {
        url: '/lotus-mbs/model_definition/id',
        method: 'get',
        paramsFields: ['model_id']
    },
    getAllTable: {
        url: '/lotus-fss/feature/store/query-all-table',
        method: 'get'
    },
    getSyncTable: {
        url: '/lotus-fss/feature/store/synchronization_table',
        method: 'get'
    },
    getTableSetting: {
        url: '/lotus-fss/feature/store/form-table-setting',
        method: 'get',
        paramsFields: ['table_name', 'data_facet_key']
    },
    saveTableSetting: {
        url: '/lotus-fss/feature/store/form-table-setting',
        method: 'post',
        paramsFields: ['table_name', 'data_facet_key']
    },
    getFeatureStoreDefaultKey: {
        url: '/lotus-fss/feature/store/feature-store-default-key',
        method: 'get',
        paramsFields: ['table_name']
    },
    getTask: {
        url: '/lotus-fss/feature/store/task',
        method: 'get',
        paramsFields: ['id']
    }
}

export default config

export function insertTask (params, data) {
    return axios({
        url: '/lotus-fss/feature/store/task',
        method: 'post',
        params
    })
}

export function deleteModelType (data) {
    return axios({
        url: '/lotus-mbs/model_type',
        method: 'delete',
        data: data
    })
}

export function saveTableSetting (params, data) {
    return axios({
        url: '/lotus-fss/feature/store/form-table-setting',
        method: 'post',
        params,
        data
    })
}

export function deleteModel (id) {
    return axios({
        url: '/lotus-mbs/model_definition/id',
        method: 'delete',
        params: { model_id: id }
    })
}

export function searchTableData (params) {
    return axios({
        url: '/lotus-fss/d1/query',
        method: 'get',
        params
    })
}
