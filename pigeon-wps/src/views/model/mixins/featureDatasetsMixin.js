import { polling } from '@/utils/util'
export default {
  props: {
    lastRouteName: {
      type: String,
      default: 'featureStoreIndex'
    }
  },
  data () {
    return {
      instanceId: '',
      instanceStatus: '',
      table: null,
      taskInstanceId: null,
      badgeMap: {
        'WAITING': 'warning',
        'RUNNING': 'processing',
        'DONE': 'success',
        'FAILED': 'error'
      }
    }
  },
  methods: {
    handleStop (resp) {
      // eslint-disable-next-line camelcase
      return ['DONE', 'FAILED'].includes(resp?.object?.di_process_instance_status)
    },
    handleData (resp) {
      this.instanceStatus = resp.object.di_process_instance_status
      return this.instanceStatus
    },
    checkInstanceStatus (instanceId) {
      return polling(this.$api.getProcessInstance, 5000, this.handleStop, this.handleData)(instanceId)
    },
    async queryOutTableData () {
      this.$nextTick(async () => {
        await this.$refs.ouputTable.loadTableData()
      })
      this.hideLoading()
    },
    showLoading () {
      this.$loading.show()
    },
    hideLoading () {
      this.$loading.hide()
    },
    async loadTaskInstanceId (processInstanceId) {
      const apiKey = `getPublishDiTaskInstance`
      const resp = await this.$api[apiKey](processInstanceId)
      if (resp.success && resp.object) {
        return resp.object.basic.di_task_instance_id
      }
    }
  }
}
