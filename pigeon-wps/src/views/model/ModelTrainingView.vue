<template>
  <a-card :bordered="false" size="small">
    <d1-vue-component
      :options="options"
      @handleTableRowClick="handleSelect"
    >
      <template #status="{ val }">
        {{ statusMap[val] }}
      </template>
      <template #operation="{ record }">
        <a-button-group>
          <a-button
            v-if="['waiting', 'wip'].includes(record.di_process_instance_status)"
            size="small"
            @click="cancel"
          >Cancel</a-button>
          <a-button
            v-if="record.di_process_instance_status === 'wip'"
            size="small"
            @click="suspend"
          >Suspend</a-button>
          <a-button
            v-if="record.di_process_instance_status === 'done'"
            size="small"
            @click="rollback"
          >Rollback</a-button>
          <a-popconfirm
            title="confirm deletion？"
            cancel-text="No"
            ok-text="Yes"
            @confirm="deleteInstance"
          >
            <a-button
              v-if="['canceled', 'failed', 'rollbacked', 'suspended'].includes(record.di_process_instance_status)"
              type="danger"
              size="small"
              @click="deleteInstance"
            >Delete</a-button>
          </a-popconfirm>
        </a-button-group>
      </template>
    </d1-vue-component>
    <div
      class="task-instance-box"
      v-if="activeInstance"
    >
      <h3 class="title">Below is the pipeline of selected training process instance:({{ activeInstance.model_name }}, {{ activeInstance.model_training_process_instance_id }})</h3>
      <a-tabs
        type="card"
        size="large"
        v-model="activeTab"
        @change="handleTabChange"
      >
        <a-tab-pane
          v-for="tabKey in tabs"
          :key="tabKey"
          :tab="tabKey"
        >
          <template v-if="trainingDetail">
            <ModelTrainingInfo :trainingDetail="trainingDetail" :type="'Training ' + tabKey">
            </ModelTrainingInfo>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import ModelTrainingInfo from '@/components/model/ModelTrainingInfo'
import { deleteDIProcessDefinition } from '@/api/definition'
import { modelStatusMap } from '@/assets/model/map'

export default {
  name: 'ModelTraining',
  components: {
    d1VueComponent,
    ModelTrainingInfo
  },
  data () {
    return {
      activeTab: 'Prepare Dataset',
      tabs: ['Prepare Dataset', 'Train', 'Commit'],
      activeInstance: null,
      trainingDetail: null,
      statusMap: modelStatusMap,
      options: {
        dataFacetKey: 'model_training_process_instance_dfk',
        d1ClientBaseUrl: '/lotus-mts',
        showToolbarButtonList: true,
        showHlightCurrentRow: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        // showTableOperationButton: true,
        openform: true,
        // operationWidth: 150,
        serialNumberWidth: 60
      }
    }
  },
  methods: {
    async loadTask () {
      if (!this.activeInstance) {
        return
      }
      this.trainingDetail = null
      const apiKeyMap = {
        'Prepare Dataset': 'getPrepareTrainingSet',
        Train: 'getTrainTask',
        Commit: 'getTrainCommitTask'
      }
      const apiKey = apiKeyMap[this.activeTab]
      const resp = await this.$api[apiKey](this.activeInstance.model_training_process_instance_id)
      if (resp.success && resp.object) {
        this.trainingDetail = resp.object
        console.log('this.trainingDetail', this.trainingDetail)
      }
    },
    handleTabChange () {
      this.loadTask()
    },
    async deleteInstance (item) {
      const resp = await deleteDIProcessDefinition(item.di_process_definition_id)
      if (resp && resp.success) {
        this.$refs.d1.runQuery()
      }
    },
    async cancel (item) {},
    async suspend (item) {},
    async resume (item) {},
    handleSelect (item) {
      this.activeInstance = item
      this.loadTask()
    },
    handleDone (instance) {
      this.$refs.d1.runQuery()
    }
  }
}
</script>

<style scoped>
.title {
  text-align: center;
  font-size: 20px;
  margin-top: 30px;
  margin-bottom: 30px;
}
.task-instance-box {
  margin-top: 20px;
}
</style>
