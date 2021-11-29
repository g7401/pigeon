<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <d1-vue-component
      v-if="visible"
      :options="generateOption"
      :apiType="apiType"
      ref="table"
    ></d1-vue-component>
  </a-card>
</template>

<script>
import d1VueComponent from '@/components/d1/D1VueComponent'
import Vue from 'vue'
export default {
  name: 'D1PreviewView',
  components: {
    d1VueComponent
  },
  props: {
    dfk: {
      type: String,
      default: ''
    },
    apiType: {
      type: String,
      default: 'openapi'
    }
  },
  created () {
    let dfk = this.dfk
    if (!dfk) {
      dfk = Vue.ls.get('ACTIVE_DFK')
    } else {
      Vue.ls.set('ACTIVE_DFK', dfk)
    }
    this.generateOption.dataFacetKey = dfk
  },
  data () {
    return {
      visible: true,
      generateOption: {
        toolbarBtnSize: 'small',
        fieldChooseVisible: true,
        dataFacetKey: '',
        d1ClientBaseUrl: null,
        showToolbarButtonList: true
        // toolbarButtonList: [
        //     {
        //         label: 'Export ', //按钮显示的名称
        //         type: 'primary',  //按钮的类型,默认空,支持  primary，success，info，warning，danger
        //         name: 'export', // 用户点击时返回的组件
        //         elColWidth: 3, //按钮的占位
        //     }
        // ],
        // tableOperationButtonList: [
        //     {
        //         label: 'download', //按钮显示的名称
        //         type: '',  //按钮的类型,默认空,支持  primary，success，info，warning，danger
        //         name: 'download', // 按钮点击时间触发的函数名称
        //         width: 150
        //     }
        //  ],
        // tableCellDataLink: [{
        //     db_field_name: 'batch_id', //需要增加a标签的字段名
        //     field_label: [""], //a标签显示的限制值
        //     dialogDisplaysValueFromFields: '', // 对话框的说明来源于字段
        //     needCustomProcess: true,  //如果设置为ture,请监听onTableCellDataClick函数
        //     name:''
        // },
        // ]
      }
    }
  },
  methods: {
    refreshTable () {
      this.$refs.table.refreshTable()
    },
    refresh () {
      this.visible = false
      this.$nextTick(() => {
        this.visible = true
      })
    }
  }
}
</script>

<style scoped>
</style>
