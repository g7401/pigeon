import { axios } from '@/utils/request'

export const api = {
  diProcessDefinitions: '/kapok-dis/master/definitions/di_process_definitions',
  definitionGroups: '/kapok-dis/master/definitions/ds_groups',
  processors: '/kapok-dis/master/processors/',
  processorParsings: '/kapok-dis/master/processors/processor_parsings',
  diTask: '/kapok-dis/master/definitions/di_task_definitions',
  diTaskBatch: '/kapok-dis/master/definitions/di_task_definition_batch',
  upload: '/kapok-dis/file_transfer/upload',
  groupDetails: '/kapok-dis/master/definitions/ds_group_details',
  getDemoPlugin: '/kapok-dis/master/plugin/button/demo-plugin-fileId'
}

const config = {
  getDemoPlugin: {
    url: api.getDemoPlugin,
    method: 'get'
  },
  getDsgroupDetails: {
    url: api.groupDetails,
    method: 'get',
    mock: true,
    paramsFields: [{ key: 'enabled', default: true }]
  }
}

export default config

export function getDIProcessDefinition (definitionId) {
  return axios({
    url: api.diProcessDefinitions,
    method: 'get',
    params: { di_process_definition_id: definitionId }
  })
}

export function createDIProcessDefinition (parameter) {
  return axios({
    url: api.diProcessDefinitions,
    method: 'post',
    data: parameter
  })
}

export function updateDIProcessDefinition (parameter) {
  return axios({
    url: api.diProcessDefinitions,
    method: 'patch',
    data: parameter
  })
}

export function deleteDIProcessDefinition (id, parameter) {
  return axios({
    url: api.diProcessDefinitions,
    method: 'delete',
    params: { di_process_definition_id: id, ...parameter }
  })
}

export function getDefinitionGroups () {
  return axios({
    url: api.definitionGroups,
    method: 'get'
  })
}

export function createProcessor (diTaskType, programPackageFileId) {
  const data = {
    'di_task_type': diTaskType,
    'program_package_file_id': programPackageFileId
  }
  return axios({
    url: api.processors,
    method: 'post',
    data
  })
}

export function parseProcessor (diTaskType, programPackageFileId) {
  const data = {
    'di_task_type': diTaskType,
    'program_package_file_id': programPackageFileId
  }
  return axios({
    url: api.processorParsings,
    method: 'post',
    data
  })
}

export function getProcesssorDetail (id) {
  return axios({
    url: api.processors,
    method: 'get',
    params: { id: id }
  })
}

export function saveDiTaskDefinition (data) {
  return axios({
    url: api.diTask,
    method: 'post',
    data
  })
}

export function updateDiTaskDefinition (data) {
  return axios({
    url: api.diTask,
    method: 'patch',
    data
  })
}

export function getDiTaskDefinition (data) {
  return axios({
    url: api.diTask,
    method: 'get',
    params: data
  })
}

export function saveDiTaskDefinitionBatch (data) {
  return axios({
    url: api.diTaskBatch,
    method: 'post',
    data
  })
}

export function downloadFile (sharedFileId, url) {
  return axios({
    url: url || api.download,
    method: 'get',
    params: { shared_file_id: sharedFileId }
  })
}
