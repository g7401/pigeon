<template>
  <a-card style="min-height:480px">
    <h2> List Of Objects in Feature Repository</h2>
    <a-row style="margin-bottom:36px">
      <a-col :span="8">
        <a-select
          style="width: 100%"
          v-model="dataStoreId"
          :options="dataStore"
          :showSearch="true"
        ></a-select>
      </a-col>
      <a-col :span="16">
        <a-button
          class="btn-gutter"
          type="primary"
          @click="configureColumn"
          :disabled="!dataStoreId"
        >Confirm Selection</a-button>
        <a-button
          class="btn-gutter"
          type="primary"
          @click="initialCreate"
        >Or Initial Creation by Upload Feature Datasets</a-button>
      </a-col>
    </a-row>
    <h2 v-show="visiable">Data</h2>
    <p v-show="!visiable">None</p>
    <div v-show="visiable">
      <d1-vue-component
        ref="d1"
        :options="generateOption"
        @onToolbarButtonClick="handleToolbar"
      ></d1-vue-component>
    </div>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import { insertTask } from '@/api/modelBuilding'
import pageStateMixin from '@/views/mixins/pageState'
export default {
  name: 'FeatureStoreIndex',
  mixins: [pageStateMixin],
  components: {
    d1VueComponent
  },
  data () {
    return {
      dataStoreId: '',
      dataStore: [],
      visiable: false,
      dataFacetKey: '',
      generateOption: {
        dataFacetKey: 'model_definition_view_dfk',
        d1ClientBaseUrl: '/lotus-mbs',
        formSize: 'small',
        tableSize: 'small',
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        openform: true,
        loadFormTableOnCreate: false,
        loadTableDataAfterLoadFormTableAtOnce: false
      },
      dataDi: {}
    }
  },
  methods: {
    go () {
      this.$router.push({
        name: 'uploadFeatureDatasets'
      })
    },
    configureColumn () {
      this.getFeatureStoreDefaultKey()
    },
    async getAllTable () {
      const response = await this.$api.getSyncTable()
      if (response.success) {
        response.object.forEach(element => {
          const obj = {}
          obj.key = element.table_name
          obj.title = element.table_name
          this.dataDi[obj.key] = {
            ds_group: element.ds_group,
            di_process_definition_id: element.di_process_definition_id
          }
          this.dataStore.push(obj)
        })
      }
    },
    async getFeatureStoreDefaultKey () {
      const response = await this.$api.getFeatureStoreDefaultKey(this.dataStoreId)
      if (response.success) {
        this.generateOption.dataFacetKey = response.object
        this.generateOption.d1ClientBaseUrl = '/lotus-fss'
        this.generateOption.loadFormTableOnCreate = true
        this.generateOption.loadTableDataAfterLoadFormTableAtOnce = true
        this.generateOption.collapse = false
        if (this.$route.query.di_process_instance_id) {
          this.generateOption.handleForm = form => {
            form.forEach(item => {
              if (item.db_field_name === 'dpiid') {
                item.field_value = JSON.stringify([this.$route.query.di_process_instance_id])
              }
            })
            return form
          }
        }
        if (this.dataDi[this.dataStoreId].ds_group === 'UPLOAD_FEATURE_DATASETS') {
          this.generateOption.toolbarButtonList = [
            {
              label: 'Additional Upload Feature Datasets', // 按钮显示的名称
              type: 'primary', // 按钮的类型,默认空,支持  primary，success，info，warning，danger
              name: 'addFeatureDatasets', // 用户点击时返回的组件
              elColWidth: 4 // 按钮的占位
            }
          ]
        }
        this.$refs.d1.initOptions(this.generateOption)
        this.visiable = true
      }
    },
    initialCreate () {
      // 跳转路由,清除datastore以判断是新增还是更新
      this.dataStoreId = null
      this.$router.push({
        name: 'uploadFeatureDatasets'
      })
    },
    handleToolbar (toolbarName) {
      if (toolbarName === 'addFeatureDatasets') {
        this.$router.push({
          name: 'additionalUploadFeatureDatasets',
          query: {
            definition_id: this.dataDi[this.dataStoreId]['di_process_definition_id']
          }
        })
      }
    },
    async checkTaskStatus (id) {
      return new Promise(async (resolve, reject) => {
        const resp = await this.$api.getTask(id)
        if (!resp || !resp.success) reject(resp.err_message)
        if (resp.object.is_down) {
          resolve('DONE')
        } else if (resp.object.failed_at) {
          resolve('FAIL')
        } else {
          setTimeout(() => {
            resolve(this.checkTaskStatus(id))
          }, 5000)
        }
      })
    }
  },
  async mounted () {
    const tableName = this.$route.query.table_name
    const diProcessInstanceId = this.$route.query.di_process_instance_id
    if (tableName && diProcessInstanceId) {
      const task = await insertTask({ table_name: tableName, di_process_instance_id: diProcessInstanceId })
      if (task.success) {
        this.$loading.show()
        try {
          const status = await this.checkTaskStatus(task.object.id)
          if (status === 'DONE') {
            await this.getAllTable()
            if (!this.dataDi[tableName]) {
              this.dataDi[tableName] = {
                di_process_definition_id: diProcessInstanceId,
                ds_group: task.object.ds_group
              }
              this.dataStore.push({ key: tableName, title: tableName })
            }
            this.dataStoreId = tableName
            this.getFeatureStoreDefaultKey()
            this.$loading.hide()
          }
        } catch (error) {
          this.$loading.hide()
        }
        if (status === 'FAIL') {
          this.$loading.hide()
          this.getAllTable()
        }
      }
    } else if (tableName) {
      await this.getAllTable()
      this.dataStoreId = this.$route.query.table_name
      this.getFeatureStoreDefaultKey()
    } else {
      this.getAllTable()
    }
  }
}
</script>

<style lang="less" scoped>
.btn-gutter {
  margin-left: 12px;
}
.bottom-gutter {
  margin-bottom: 12px;
}
</style>
