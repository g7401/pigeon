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
      @onToolbarButtonClick="handleToolbar"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import { deleteModel } from '@/api/modelBuilding'
export default {
  name: 'ModelManagement',
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
        operationWidth: 150,
        serialNumberWidth: 60,
        toolbarButtonList: [
          {
            label: 'Create Model', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  primary，success，info，warning，danger
            name: 'create', // 用户点击时返回的组件
            elColWidth: 3 // 按钮的占位
          }
        ],
        tableOperationButtonList: [
          {
            label: 'View', // 按钮显示的名称
            type: 'default', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'view', // 按钮点击时间触发的函数名称
            width: 60
          },
          {
            label: 'Edit', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'edit', // 按钮点击时间触发的函数名称
            width: 60
          },
          {
            label: 'Delete', // 按钮显示的名称
            type: 'danger', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'delete', // 按钮点击时间触发的函数名称
            width: 60
          }
        ]
      },
      createPageName: 'createModelManagement',
      editPageName: 'editModelManagement',
      viewPageName: 'viewModelManagement'
    }
  },
  methods: {
    async delete (item) {
      const resp = await deleteModel(item.model_id)
      if (resp && resp.success) {
        this.$refs.d1.runQuery()
      }
    },
    goCreateDefinitionPage () {
      this.$router.push({
        name: this.createPageName
      })
    },
    handleToolbar (toolbarName) {
      if (toolbarName === 'create') {
        this.goCreateDefinitionPage()
      }
    },
    goEditPage (item) {
      this.$router.push({
        name: this.editPageName,
        query: {
          model_id: item.model_id
        }
      })
    },
    goViewPage (item) {
      this.$router.push({
        name: this.viewPageName,
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
        delete: this.delete,
        edit: this.goEditPage,
        view: this.goViewPage,
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
