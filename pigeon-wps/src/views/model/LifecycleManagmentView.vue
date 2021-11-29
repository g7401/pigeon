<template>
  <a-card
    class="definition-view"
    size="small"
    :bordered="false"
  >
    <d1-vue-component
      ref="d1"
      :options="generateOption"
      @onTableOperationButtonClick="handleOperation"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import { deleteModel } from '@/api/modelBuilding'
export default {
  name: 'LifecycleManagment',
  components: {
    d1VueComponent
  },
  data () {
    return {
      generateOption: {
        d1ClientBaseUrl: '/lotus-mbs',
        formSize: 'small',
        tableSize: 'small',
        dataFacetKey: 'model_definition_view_dfk',
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        showTableOperationButton: true,
        openform: true,
        operationWidth: 300,
        tableOperationButtonList: [
          {
            label: 'Manage Model Instances', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'edit', // 按钮点击时间触发的函数名称
            width: 60,
            show (item) {
              return item.required_model_instance === 'true'
            }
          },
          {
            label: 'Create Model Training', // 按钮显示的名称
            type: 'default', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'training', // 按钮点击时间触发的函数名称
            width: 60,
            show (item) {
              return item.required_model_instance === 'true'
            }
          },
          {
            label: 'Create Model Serving', // 按钮显示的名称
            type: 'default', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'serving', // 按钮点击时间触发的函数名称
            width: 60
          }
        ]
      }
    }
  },
  methods: {
    async delete (item) {
      const resp = await deleteModel(item.model_id)
      if (resp && resp.success) {
        this.$refs.d1.runQuery()
      }
    },
    goEditPage (item) {
      this.$router.push({
        name: 'manageModelInstances',
        query: {
          model_id: item.model_id
        }
      })
    },
    goTrainingPage (item) {
      this.$router.push({
        name: 'createModelTraining',
        query: {
          model_id: item.model_id
        }
      })
    },
    goServingPage (item) {
      this.$router.push({
        name: 'createModelServing',
        query: {
          model_id: item.model_id
        }
      })
    },
    handleOperation (item, operationName) {
      const operationMap = {
        edit: this.goEditPage,
        training: this.goTrainingPage,
        serving: this.goServingPage
      }
      const operationFn = operationMap[operationName]
      operationFn && operationFn(item)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
