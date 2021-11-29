<template>
  <a-card :bordered="false" size="small">
    <d1-vue-component
      :options="modelOptions"
      @handleTableRowClick="handleSelectModel"
    >
    </d1-vue-component>
    <template v-if="activeModel">
      <h3 class="title">Below is the serving process instances of selected model:({{ activeModel.model_name }})</h3>
      <d1-vue-component
        :options="servingOptions"
        @handleTableRowClick="handleSelect"
      >
        <template #status="{ val }">
          {{ statusMap[val] }}
        </template>
      </d1-vue-component>
    </template>

    <div
      class="task-instance-box"
      v-if="activeInstance"
    >
      <h3 class="title">Below is the pipeline of selected serving process instance:({{ activeInstance.model_name }}, {{ activeInstance.model_prediction_process_instance_id }})</h3>
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
            <ModelTrainingInfo
              :trainingDetail="trainingDetail"
              :type="'Serving ' + tabKey"
            >
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
  name: 'ModelServing',
  components: {
    d1VueComponent,
    ModelTrainingInfo
  },
  data () {
    return {
      activeTab: 'Prepare Dataset',
      tabs: ['Prepare Dataset', 'Predict', 'Commit'],
      statusMap: modelStatusMap,
      activeInstance: null,
      activeModel: null,
      trainingDetail: null,
      modelOptions: {
        dataFacetKey: 'model_definition_view_in_serving_dfk',
        d1ClientBaseUrl: '/lotus-mbs',
        serialNumberWidth: 40,
        showHlightCurrentRow: true,
        asyncExport: true,
        showExportButton: true,
        showToolbarButtonList: true,
        serialNumber: true
      },
      servingOptions: {
        dataFacetKey: 'model_prediction_process_instance_dfk',
        d1ClientBaseUrl: '/lotus-mts',
        serialNumberWidth: 40,
        showHlightCurrentRow: true,
        asyncExport: true,
        showExportButton: true,
        showToolbarButtonList: true,
        serialNumber: true,
        preFilters: {}
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
        'Prepare Dataset': 'getPreparePredictionSet',
        Predict: 'getPredictTask',
        Commit: 'getPredictCommitTask'
      }
      const apiKey = apiKeyMap[this.activeTab]
      const resp = await this.$api[apiKey](this.activeInstance.model_prediction_process_instance_id)
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
    handleSelectModel (item) {
      this.activeModel = null
      this.activeInstance = null
      this.servingOptions.preFilters = {
        model_name: item.model_name
      }
      this.$nextTick(() => {
        this.activeModel = item
      })
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
