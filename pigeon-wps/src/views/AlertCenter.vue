<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <d1-vue-component
      :options="options"
      @onTableOperationButtonClick="handleOperation"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent.vue'
const baseOptions = {
  showPagination: false,
  showForm: false,
  serialNumber: true,
  showTableOperationButton: true,
  openform: true
}
export default {
  name: 'AlertCenter',
  components: {
    d1VueComponent
  },
  data () {
    return {
      options: {
        dataFacetKey: 'di_process_definition_dfk',
        tableOperationButtonList: [
          {
            label: 'Read', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'read', // 按钮点击时间触发的函数名称
            width: 60,
            show: (item) => item.status === 'unread'
          },
          {
            label: 'Unread', // 按钮显示的名称
            type: 'primary', // 按钮的类型,默认空,支持  defualt primary dashed danger link
            name: 'Unread', // 按钮点击时间触发的函数名称
            ghost: true,
            width: 60,
            show: (item) => item.status === 'read'
          }
        ],
        ...baseOptions
      }
    }
  },
  methods: {
    async read (item) {
    },
    async unread (item) {
    },
    handleOperation (item, operationName) {
      const operationFn = this[operationName]
      operationFn && operationFn(item)
    }
  }
}
</script>
