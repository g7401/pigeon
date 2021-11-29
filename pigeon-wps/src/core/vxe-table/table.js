import Vue from 'vue'
import XEUtils from 'xe-utils'
import VXETablePluginAntd from 'vxe-table-plugin-antd'
import VXETablePluginVirtualTree from 'vxe-table-plugin-virtual-tree'

import {
  VXETable,
  Table,
  Icon,
  Column,
  Header,
//   Footer,
  Edit,
  Grid,
  // Toolbar,
  Pager,
  // Checkbox
  // Radio,
  Input,
  // Button,
  Tooltip,
  // Form,
  Select
  // Switch
} from 'vxe-table'
import zhCNLocat from 'vxe-table/lib/locale/lang/zh-CN'
// 按需加载的方式默认是不带国际化的，需要自行导入
VXETable.setup({
  i18n: (key, args) => XEUtils.template(XEUtils.get(zhCNLocat, key), args, { tmplRE: /\{([.\w[\]\s]+)\}/g }),
  table: {
    border: true,
    resizable: true,
    // scrollX: {
    //   gt: 10
    // },
    scrollY: {
      gt: 20
    }
  }
})
VXETable.renderer.add('NullStr', {
    // 默认显示模板
    renderDefault (h, renderOpts, params) {
      const { row, column } = params
      const nullStr = renderOpts.attrs.nullStr
      return [
        <span class="vxe-cell--label">{row[column.property] === null ? nullStr : row[column.property]}</span>
      ]
    }
  })
  VXETable.renderer.add('Replace', {
    // 默认显示模板
    renderDefault (h, renderOpts, params) {
      const { row, column } = params
      const valueMap = renderOpts.attrs.valueMap || {}
      return [
        <span class="vxe-cell--label">{valueMap[row[column.property]] || row[column.property]}</span>
      ]
    }
  })

VXETable.use(VXETablePluginAntd)

// 先安装依赖模块
Vue.use(Icon)
Vue.use(Column)
Vue.use(Header)
// Vue.use(Footer)
Vue.use(Edit)
Vue.use(Tooltip)

// Vue.use(Toolbar)
Vue.use(Pager)
Vue.use(Grid)
// Vue.use(Form)
// Vue.use(Checkbox)
// Vue.use(Radio)
// Vue.use(Switch)
Vue.use(Input)
Vue.use(Select)
// Vue.use(Button)

// 再安装核心库
Vue.use(Table)

VXETable.use(VXETablePluginVirtualTree)
