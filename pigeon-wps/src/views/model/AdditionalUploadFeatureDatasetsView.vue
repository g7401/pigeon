<template>
  <a-card
    size="small"
    class=""
  >
    <a-row>
      <SUpload
        v-model="fileId"
        @input="afterUpload"
        :config="{btnText: 'Upload Data File'}"
      ></SUpload>
    </a-row>
    <a-row class="instance-info">
      <p>
        <template v-if="instanceId">{{ instanceId }}</template>
        <template v-if="instanceStatus">,
          <a-badge
            :status="badgeMap[instanceStatus]"
            :text="instanceStatus"
          /></template>
      </p>
    </a-row>
    <TaskInstanceOutputTable
      v-if="table"
      ref="ouputTable"
      :table="table"
      :taskInstanceId="taskInstanceId"
    ></TaskInstanceOutputTable>
  </a-card>
</template>

<script>
import SUpload from '@/components/common/SUpload'
import TaskInstanceOutputTable from '@/components/DIDefinitions/TaskInstanceOutputTable'
import featureDatasetsMixin from './mixins/featureDatasetsMixin'
export default {
  name: 'AdditionalUploadFeatureDatasets',
  mixins: [featureDatasetsMixin],
  components: {
    SUpload,
    TaskInstanceOutputTable
  },
  data () {
    return {
      taskLoading: false,
      statusLoading: false,
      fileId: null
    }
  },
  props: {
    definitionId: {
      type: String,
      default: ''
    }
  },
  async created () {
    this.table = await this.getTable()
    this.$store.commit('savePage', {
      name: this.lastRouteName,
      state: {
        table_name: this.table.table_name
      }
    })
  },
  methods: {
    async afterUpload () {
      this.showLoading()
      this.instanceId = await this.getProcessorInstanceId().catch(() => this.hideLoading())
      if (!this.instanceId) {
        this.hideLoading()
        return
      }
      this.$store.commit('savePage', {
        name: this.lastRouteName,
        update: true,
        state: {
          di_process_instance_id: this.instanceId
        }
      })
      const status = await this.checkInstanceStatus(this.instanceId)
      if (status === 'FAILED') {
        this.hideLoading()
        return
      }
      this.taskInstanceId = await this.loadTaskInstanceId(this.instanceId).catch(() => this.hideLoading())
      this.queryOutTableData()
    },
    async getTable () {
      this.taskLoading = true
      const resp = await this.$api.getDefinitionTables(this.definitionId, 'PUBLISH')
      this.taskLoading = false
      if (resp.success && resp.object && resp.object.output_tables) {
        return resp.object.output_tables[0]
      }
    },

    async getProcessorInstanceId () {
      const resp = await this.$api.createProcessInstance(this.definitionId, this.fileId)
      if (!resp || !resp.object) return
      return resp.object.di_process_instance_id
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .instance-info {
  height: 40px;
  margin-top: 20px;
}
/deep/ .top-row {
  margin-bottom: 10px;
}
</style>
