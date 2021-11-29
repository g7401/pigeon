const api = {
  diProcessInstance: '/kapok-dis/master/executions/di_process_instances',
  getDiProcessInstance: '/kapok-dis/master/executions/di_process_instance',
  importDiTaskInstance: '/kapok-dis/master/executions/di_task_instances/import',
  extractDiTaskInstance: '/kapok-dis/master/executions/di_task_instances/extract',
  transformDiTaskInstance: '/kapok-dis/master/executions/di_task_instances/transform',
  publishDiTaskInstance: '/kapok-dis/master/executions/di_task_instances/publish',
  queryOutputData: '/kapok-dis/master/executions/di_task_instances/query_output_data',
  cancelProcessInstance: '/kapok-dis/master/executions/di_task_instances/state_controls/cancel',
  deleteProcessInstance: '/kapok-dis/master/executions/di_task_instances/state_controls/delete',
  resumeProcessInstance: '/kapok-dis/master/executions/di_task_instances/state_controls/resume',
  rollbackProcessInstance: '/kapok-dis/master/executions/di_task_instances/state_controls/rollback',
  suspendProcessInstance: '/kapok-dis/master/executions/di_task_instances/state_controls/suspend',
  getDefinitionTables: '/kapok-dis/master/executions/di_processor_definition_tables'
}

const config = {
  cancelProcessInstance: {
    url: api.cancelProcessInstance,
    method: 'post',
    paramsFields: ['di_process_instance_id']
  },
  deleteProcessInstance: {
    url: api.deleteProcessInstance,
    method: 'post',
    paramsFields: ['di_process_instance_id']
  },
  resumeProcessInstance: {
    url: api.resumeProcessInstance,
    method: 'post',
    paramsFields: ['di_process_instance_id']
  },
  rollbackProcessInstance: {
    url: api.rollbackProcessInstance,
    method: 'post',
    paramsFields: ['di_process_instance_id']
  },
  suspendProcessInstance: {
    url: api.suspendProcessInstance,
    method: 'post',
    paramsFields: ['di_process_instance_id']
  },
  getImportDiTaskInstance: {
    url: api.importDiTaskInstance,
    method: 'get',
    paramsFields: ['di_process_instance_id'],
    mock: true
  },
  getExtractDiTaskInstance: {
    url: api.extractDiTaskInstance,
    method: 'get',
    paramsFields: ['di_process_instance_id'],
    mock: true
  },
  getTransformDiTaskInstance: {
    url: api.transformDiTaskInstance,
    method: 'get',
    paramsFields: ['di_process_instance_id'],
    mock: true
  },
  getPublishDiTaskInstance: {
    url: api.publishDiTaskInstance,
    method: 'get',
    paramsFields: ['di_process_instance_id'],
    mock: true
  },
  createProcessInstance: {
    url: api.diProcessInstance,
    method: 'post',
    dataFields: ['di_process_definition_id', 'ext'],
    mock: true
  },
  getProcessInstance: {
    url: api.getDiProcessInstance,
    method: 'get',
    paramsFields: ['di_process_instance_id']
  },
  queryTaskOutputData: {
    url: api.queryOutputData,
    method: 'get',
    paramsFields: ['di_task_instance_id', 'output_table_name']
  },
  getDefinitionTables: {
    url: api.getDefinitionTables,
    method: 'get',
    paramsFields: ['di_process_definition_id', 'di_task_type']
  }
}

export default config
