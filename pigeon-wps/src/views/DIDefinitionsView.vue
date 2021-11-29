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
    >
    </d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
import { deleteDIProcessDefinition } from '@/api/definition'
import { startupTypeMap } from '@/assets/definition/map'
export default {
  name: 'DiDefinitions',
  components: {
    d1VueComponent
  },
  data () {
    return {
      generateOption: {
        valueMap: {
          startup_type: startupTypeMap
        },
        formSize: 'small',
        tableSize: 'small',
        dataFacetKey: 'di_process_definition_dfk',
        pageSize: 10,
        showToolbarButtonList: true,
        asyncExport: true,
        showExportButton: true,
        serialNumber: true,
        showTableOperationButton: true,
        openform: true,
        toolbarButtonList: [
          {
            label: 'Create DI Process Definition', // 按钮显示的名称
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
      }
    }
  },
  methods: {
    async delete (item) {
      const resp = await deleteDIProcessDefinition(item.di_process_definition_id)
      if (resp && resp.success) {
        this.$refs.d1.runQuery()
      }
    },
    goCreateDefinitionPage () {
      this.$router.push('/di-definition/create')
    },
    handleToolbar (toolbarName) {
      if (toolbarName === 'create') {
        this.goCreateDefinitionPage()
      }
    },
    goPage (item, mode = 'edit') {
      this.$router.push({
        path: `/di-definition/${mode}`,
        query: {
          definition_id: item.di_process_definition_id
        }
      })
    },
    handleOperation (item, operationName) {
      const operationMap = {
        delete: this.delete,
        edit: this.goPage,
        view: (item) => this.goPage(item, 'view')
      }
      const operationFn = operationMap[operationName]
      operationFn && operationFn(item)
    }
  }
}
</script>

<style lang="less" scoped>
</style>
