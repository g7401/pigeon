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
import { deleteModelType } from '@/api/modelBuilding.js'
export default {
  name: 'ModelTypeManagement',
  components: {
    d1VueComponent
  },
  data () {
    return {
      generateOption: {
        d1ClientBaseUrl: '/lotus-mbs',
        dataFacetKey: 'model_type_dfk',
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        serialNumberWidth: 70,
        showTableOperationButton: true,
        openform: true,
        toolbarButtonList: [
          {
            label: 'Create Model Type', // 按钮显示的名称
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
      createPageName: 'createModelType',
      editPageName: 'editModelType',
      viewPageName: 'viewModelType'
    }
  },
  methods: {
    async delete (item) {
      const resp = await deleteModelType([item.model_type_id])
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
          model_type_id: item.model_type_id
        }
      })
    },
    goViewPage (item) {
      this.$router.push({
        name: this.viewPageName,
        query: {
          model_type_id: item.model_type_id
        }
      })
    },
    handleOperation (item, operationName) {
      const operationMap = {
        delete: this.delete,
        edit: this.goEditPage,
        view: this.goViewPage
      }
      const operationFn = operationMap[operationName]
      operationFn && operationFn(item)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
