export const api = {
  getDealerCascade: '/rp-ms/supplier/cascade',
  getSalemanList: '/rp-ms/rp_plan_instance/status',
  getMapStoreList: '/rp-ms/rp_plan_instance/map_result',
  restoreMapStoreList: '/rp-ms/rp_plan_instance/default_map_result',
  saveMapStoreList: '/rp-ms/rp_plan_instance/map_result',
  createPlanInstance: '/rp-ms/rp_plan_instance',
  getPlanInstance: '/rp-ms/rp_plan_instance/status',
  savePlanAttrs: '/rp-ms/rp_plan_instance/configure_attr',
  confirmPlan: '/rp-ms/rp_plan_instance/confirm',
  getPlanStatistics: '/rp-ms/rp_plan_instance/statistics',
  saveLastStep: '/placeholder11',
  startCompute: '/rp-ms/rp_plan_instance/start_calc',
  getComputeStatus: '/rp-ms/rp_plan_instance/query_calc_status',
  getMapChoices: '/rp-ms/rp_plan_instance/drop_down_list',
  exportMapResult: '/rp-ms/rp_plan_instance/export/map_result',
  getWorkDays: '/rp-ms/rp_plan_instance/workdays'
}

const config = {
  exportMapResult: {
    url: api.exportMapResult,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id', 'type']
  },
  getMapChoices: {
    url: api.getMapChoices,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id', 'type']
  },
  getDealerCascade: {
    url: api.getDealerCascade,
    method: 'get',
    mock: true
  },
  getSalemanList: {
    url: api.getSalemanList,
    method: 'get',
    mock: true
  },
  getMapStoreList: {
    url: api.getMapStoreList,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id', 'type']
  },
  restoreMapStoreList: {
    url: api.restoreMapStoreList,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id', 'type']
  },
  saveMapStoreList: {
    url: api.saveMapStoreList,
    method: 'post',
    mock: true,
    paramsFields: ['plan_instance_id', 'type'],
    dataFields: ['stores']
  },
  createPlanInstance: {
    url: api.createPlanInstance,
    method: 'post',
    mock: true
  },
  getPlanInstance: {
    url: api.getPlanInstance,
    method: 'get',
    // mock: true,
    paramsFields: ['plan_instance_id']
  },
  savePlanAttrs: {
    url: api.savePlanAttrs,
    method: 'post',
    mock: true,
    paramsFields: ['plan_instance_id']
  },
  confirmPlan: {
    url: api.confirmPlan,
    method: 'post',
    paramsFields: ['plan_instance_id'],
    mock: true
  },
  getPlanStatistics: {
    url: api.getPlanStatistics,
    method: 'get',
    mock: false,
    paramsFields: ['plan_instance_id', 'type']
  },
  saveLastStep: {
    url: api.saveLastStep,
    method: 'post',
    paramsFields: ['plan_instance_id'],
    dataFields: ['last_step', 'step_status'],
    mock: true
  },
  startCompute: {
    url: api.startCompute,
    method: 'post',
    paramsFields: ['plan_instance_id', 'step_name'],
    mock: true
  },
  getComputeStatus: {
    url: api.getComputeStatus,
    method: 'get',
    paramsFields: ['plan_instance_id', 'step_name', 'step_task_id'],
    mock: true
  },
  getWorkDays: {
    url: api.getWorkDays,
    method: 'get',
    paramsFields: ['start_date'],
    mock: true
  }
}

export default config
