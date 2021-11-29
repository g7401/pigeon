<template>
  <span>
    <a-modal
      title="Export Data"
      v-model="confirmToExportDialogVisible"
    >
      <div class="dialog">{{ exportDialogTip }}</div>
      <span
        slot="footer"
        class="dialog-footer"
      >
        <a-button
          type="primary"
          @click="handleExportExcel()"
        >Yes</a-button>
        <a-button
          type="danger"
          @click="confirmToExportDialogVisible = false"
        >No</a-button>
      </span>
    </a-modal>
    <a-button
      v-if="downloadLoading"
      loading
    >Downloading</a-button>
    <a-button
      v-else
      :disabled="!totalRecord"
      type="primary"
      icon="download"
      @click="handleExport"
    >Export</a-button>
  </span>
</template>

<script>
export default {
  props: {
    totalRecord: {
      type: Number,
      default: 0
    },
    exportApi: {
      type: String,
      default: 'getExportStatus'
    },
    exportFileApi: {
      type: String,
      default: 'getExportFile'
    },
    params: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      downloadLoading: false,
      confirmToExportDialogVisible: false
    }
  },
  computed: {
    exportDialogTip () {
      let tip = ''

      if (this.totalRecord === 1) {
        tip = 'Confirm to export 1 record?'
      } else {
        tip = 'Confirm to export ' + this.totalRecord + ' records?'
      }

      return tip
    }
  },
  methods: {
    handleExport () {
      if (this.totalRecord === 0) {
        this.$message.warning('No record to export')
      } else {
        this.confirmToExportDialogVisible = true
      }
    },
    async handleExportExcel () {
      this.downloadLoading = true
      this.confirmToExportDialogVisible = false
      const resp = await this.$api[this.exportApi](undefined, this.params).finally(() => {
        this.downloadLoading = false
      })
      if (resp && resp.success) {
        this.downloadLoading = true
        this.loadExportSummary(resp.object.uid)
      }
    },
    async loadExportSummary (taskId, requestCount = 0) {
      const resp = await this.$api[this.exportFileApi](taskId, this.params)
      requestCount++
      if (resp && resp.object && resp.object.status === 'DONE' && resp.object.file_id) {
        window.location.href = `${this.$api.config.download}?file_id=${resp.object.file_id}`
        this.downloadLoading = false
      } else if (resp && resp.object && ['RUNNING', 'WAITING'].includes(resp.object.status)) {
        setTimeout(() => {
          this.loadExportSummary(taskId, requestCount)
        }, 5000)
      } else {
        this.downloadLoading = false
        this.$message.warning('Service is busy, please try again later')
      }
    }
  }
}
</script>

<style>
</style>
