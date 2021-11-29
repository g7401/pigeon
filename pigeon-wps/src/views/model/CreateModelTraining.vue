<template>
  <a-card
    size="small"
    :bordered="false"
    class="create-model-training"
    v-if="model"
  >
    <a-row
      type="flex"
      justify="end"
    >
      <a-button
        class="button-gap"
        type="primary"
        :loading="createLoading"
        @click="createTraining"
      >Confirm</a-button>
      <a-button @click="back">Cancel</a-button>
    </a-row>
    <KeyValueInfo
      :obj="model"
      :options="infoOptions"
    >
      <template #button>
        <a-button @click="goConfPage">View Detail</a-button>
      </template>
    </KeyValueInfo>
    <template v-if="tableOptions.dataFacetKey">
      <h3 class="title">Configure Dataset Filters to Select Target Data</h3>
      <d1VueComponent
        :options="tableOptions"
        @paramsChange="handleParamChange"
      ></d1VueComponent>
    </template>
  </a-card>
</template>

<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
export default {
  name: 'CreateModelTraining',
  components: { KeyValueInfo, d1VueComponent },
  data () {
    return {
      model: null,
      params: null,
      createLoading: false,
      type: 'training',
      infoOptions: [
        { key: 'model_type_name', label: 'Model Type Name' },
        { key: 'model_name', label: 'Model Name' },
        { key: 'button', label: 'Feature Repository Object' }
      ],
      tableOptions: {
        dataFacetKey: '',
        d1ClientBaseUrl: '/lotus-fss',
        serialNumberWidth: 40,
        asyncExport: true,
        showExportButton: true,
        showToolbarButtonList: true,
        serialNumber: true,
        watchForm: true
      }
    }
  },
  async created () {
    this.type = this.$route.name === 'createModelServing' ? 'serving' : 'training'
    await this.loadModel(this.$route.query.model_id)
    this.tableOptions.dataFacetKey = this.type === 'training' ? this.model.training_feature_store_reference_key : this.model.prediction_feature_store_reference_key
  },
  methods: {
    async loadModel (id) {
      const resp = await this.$api.getModelDefinition(id)
      if (!resp || !resp.object) return
      this.model = resp.object
    },
    goConfPage () {
      this.$router.push({
        name: `${this.type}ConfigureDatasets`,
        query: {
          dataFacetKey: this.tableOptions.dataFacetKey,
          mode: 'view'
        }
      })
    },
    handleParamChange (params) {
      this.params = params
    },
    back () {
      this.$multiTab.remove(this.$route.fullPath)
      this.$router.back()
    },
    async createTraining () {
      this.createLoading = true
      const apiKeyMap = {
        training: 'createModelTraining',
        serving: 'createModelServing'
      }
      const resp = await this.$api[apiKeyMap[this.type]](this.model.model_id, this.params).catch(
        () => (this.createLoading = false)
      )
      this.createLoading = false
      if (resp && resp.success) {
        this.back()
      }
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  font-size: 20px;
  margin-top: 30px;
}
/deep/ .button-gap {
  margin-right: 5px;
}
/deep/ .ant-descriptions-row {
  line-height: 30px;
}
/deep/ .ant-descriptions-item-label {
  width: 210px;
  text-align: right;
}
</style>
