import cloneDeep from 'lodash.clonedeep'

export default {
  data () {
    return {
      scroll: {},
      columns: []
    }
  },
  computed: {
  },
  watch: {
    tableData: {
      deep: true,
      immediate: true,
      handler (val) {
        if (!val || !val.length) return
        this.setScroll()
      }
    }
  },
  created () {
    Object.assign(this.scroll, this.rawScroll || {})
    this.columns = this.generateColumns(this.rawColumns)
  },
  methods: {
    setScroll () {
      this.$nextTick(() => {
        this.scroll = {
          x: this.isScrollX() ? (this.rawScroll ? this.rawScroll.x : 'max-content') : false,
          y: this.rawScroll ? this.rawScroll.y : false
        }
      })
    },
    isScrollX (el) {
      const element = el || this.$el
      const scrollEle = element.querySelector('.ant-table-body')
      if (!scrollEle) return false
      return scrollEle.scrollWidth > scrollEle.clientWidth
    },
    generateColumns (columns) {
      const serailColumn = {
        title: 'No.',
        dataIndex: 'serial',
        scopedSlots: { customRender: 'serial' },
        width: this.serailWidth || 60
      }
      const operationColumn = {
        title: 'Operation',
        dataIndex: 'operation',
        fixed: this.scroll.x ? 'right' : false,
        scopedSlots: { customRender: 'operation' },
        width: this.operationWidth || 150
      }
      const newColumns = cloneDeep(columns)
      if (!columns.find(x => x.dataIndex === 'serial')) {
        newColumns.unshift(serailColumn)
      }
      if (!columns.find(x => x.dataIndex === 'operation')) {
        newColumns.push(operationColumn)
      }
      return newColumns
    }
  }
}
