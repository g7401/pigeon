export const api = {
  getAgentWorkTimeReport: '/rp-ms/rp_plan_instance/report/agent_work_time',
  getAgentDetailReport: '/rp-ms/rp_plan_instance/report/agent_detail',
  exportReport: '/rp-ms/rp_plan_instance/report/export'
}

const config = {
  getAgentWorkTimeReport: {
    url: api.getAgentWorkTimeReport,
    method: 'get',
    mock: true,
    paramsFields: ['supplier_name', 'start_date', 'end_date']
  },
  getAgentDetailReport: {
    url: api.getAgentDetailReport,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id']
  },
  exportReport: {
    url: api.exportReport,
    method: 'get',
    mock: true,
    paramsFields: ['plan_instance_id']
  }
}

export default config
