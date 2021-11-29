<template>
  <div class="base-table">
    <a-row
      v-if="formItems.length"
      type="flex"
      justify="space-between"
      :gutter="5"
    >
      <a-col :span="19">
        <a-form-model
          layout="vertical"
          :colon="false"
          :model="params"
          :labal-col="{ span: 6 }"
        >
          <a-row :gutter="24">
            <a-col
              :span="formItem.span || 6"
              :key="formItem.key"
              v-for="formItem in formItems"
            >
              <a-form-model-item :label="formItem.label">
                <a-cascader
                  v-if="formItem.type === 'cascader'"
                  v-bind="formItem"
                  v-model="cacheComValMap[formItem.key]"
                  @change="cacheComValMap[formItem.key] = $event;params[formItem.key] = $event[$event.length - 1]"
                  v-on="formItem.on || {}"
                />
                <component
                  v-else
                  :is="formItem.type ? ('a-' + formItem.type) : 'a-input'"
                  :key="formItem.key"
                  v-model.trim="params[formItem.key]"
                  v-bind="formItem"
                  v-on="formItem.on || {}"
                ></component>
              </a-form-model-item>
            </a-col>
          </a-row>
        </a-form-model>
      </a-col>
      <a-col :span="5">
        <div class="search-btn-group">
          <a-button
            type="primary"
            icon="search"
            :loading="queryLoading"
            @click="search"
            style="margin-right: 5px"
          >Search</a-button>
          <a-button
            icon="reload"
            :loading="queryLoading"
            @click="reset"
          >Reset</a-button>
        </div>
      </a-col>
    </a-row>
    <a-row
      class="toolbar-box"
      :class="{ 'toolbar-box-top': formItems.length }"
    >
      <ExportButton
        v-if="!hideExport"
        :totalRecord="pagination.total"
        :exportApi="exportApi"
        :exportFileApi="exportFileApi"
        :params="params"
        :extraData="exportExtraData"
      ></ExportButton>
      <div class="pull-right">
        <a-button-group>
          <template v-for="button in toolbarButtonList">
            <SUpload
              :key="button.key"
              v-if="button.key === 'upload'"
              :config="{ btnText: button.label, hideList: true }"
              @input="handleGeneralButtonClick(button.key, button, {fileId: $event})"
            ></SUpload>
            <a-button
              v-else
              :key="button.key"
              :type="button.type"
              :loading="button.loading"
              @click="handleGeneralButtonClick(button.key, button)"
            >
              <a-icon
                v-if="button.icon"
                :type="button.icon"
              />{{ button.label }}
            </a-button>
          </template>
        </a-button-group>
      </div>
    </a-row>
    <vxe-grid
      :ref="randomRef"
      class="more-small-table mini-scrollbar"
      :class="{'row--selectable': isSelect}"
      show-overflow
      show-header-overflow
      :data="tableData"
      :loading="queryLoading"
      :merge-cells="mergeCells"
      :columns="completeColumns"
      :size="operationButtonList.length ? 'small' : $attrs.size || 'mini'"
      v-bind="$attrs"
      :seq-config="{startIndex: (pagination.currentPage - 1) * pagination.pageSize}"
      :pager-config="hidePagination ? false : pagination"
      :highlight-current-row="isSelect"
      @page-change="handleTableChange"
      @current-change="currentChangeEvent"
    >
      <template #operation="{ row }">
        <a-button-group>
          <template v-for="buttionItem in operationButtonList">
            <template v-if="!buttionItem.show || buttionItem.show(row)">
              <a-popconfirm
                :key="buttionItem.key"
                :title="'confirm ' + buttionItem.label + '?'"
                cancel-text="No"
                ok-text="Yes"
                @confirm="operationButtonClick(row, buttionItem)"
                v-if="buttionItem.confirm"
              >
                <a-button
                  :key="buttionItem.key"
                  :type="buttionItem.type"
                  :size="buttionItem.size || 'small'"
                  :disabled="buttionItem.disabled"
                  :loading="buttionItem.loading"
                  :ghost="buttionItem.ghost"
                >{{ buttionItem.label }}</a-button>
              </a-popconfirm>
              <a-button
                :key="buttionItem.key"
                :type="buttionItem.type"
                @click.native="operationButtonClick(row, buttionItem)"
                :size="buttionItem.size || 'small'"
                :disabled="buttionItem.disabled"
                :loading="buttionItem.loading"
                :ghost="buttionItem.ghost"
                v-else
              >{{ buttionItem.label }}</a-button>
            </template>
          </template>
        </a-button-group>
      </template>
    </vxe-grid>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import ExportButton from '@/components/common/ExportButton'
import SRangePicker from '@/components/common/SRangePicker'
import SUpload from '@/components/common/SUpload'

const getSpanRowNum = (row, list, columnKeys, temp) => {
  let spanRow = 0
  let columnKey
  for (let i = 0; i < columnKeys.length; i++) {
    if (row[columnKeys[i]]) {
      columnKey = columnKeys[i]
    }
  }
  if (!columnKey) return spanRow
  if (row[columnKey] !== temp[columnKey]) {
    temp[columnKey] = row[columnKey]
    list.forEach(x => {
      if (x[columnKey] === temp[columnKey]) {
        spanRow++
      }
    })
  }
  return spanRow
}

const getSpanRowMergeCells = (list, columnIndex, columnKeys) => {
  const temp = {}
  return list
    .map((x, i) => ({
      row: i,
      col: columnIndex,
      rowspan: getSpanRowNum(list[i], list, columnKeys, temp),
      colspan: 1
    }))
    .filter(x => x.rowspan > 1)
}

export default {
  components: { ExportButton, ASRangePicker: SRangePicker, SUpload },
  props: {
    isSelect: {
      type: Boolean,
      default: false
    },
    rowKey: {
      type: String,
      default: ''
    },
    queryApiName: {
      type: String,
      default: 'getEntityQueryData'
    },
    exportApi: {
      type: String,
      default: 'getExportStatus'
    },
    exportFileApi: {
      type: String,
      default: 'getExportFile'
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    },
    exportExtraData: {
      type: Object,
      default () {
        return {}
      }
    },
    formItems: {
      type: Array,
      default () {
        return []
      }
    },
    columns: {
      type: Array,
      default () {
        return []
      }
    },
    toolbarButtonList: {
      type: Array,
      default () {
        return []
      }
    },
    isLoad: {
      type: Boolean,
      default: true
    },
    hidePagination: {
      type: Boolean,
      default: false
    },
    hideExport: {
      type: Boolean,
      default: true
    },
    nullStr: {
      type: String,
      default: ''
    },
    sorter: {
      type: Function,
      default () {
        return null
      }
    },
    operationButtonList: {
      type: Array,
      default () {
        return []
      }
    },
    operationWidth: {
      type: Number,
      default: 120
    },
    serial: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      randomRef: Math.random().toString(),
      mergeCells: [],
      tableData: [],
      params: {},
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 100,
        align: 'right',
        pageSizes: [10, 20, 50, 100],
        layouts: ['Total', 'PrevPage', 'JumpNumber', 'NextPage', 'FullJump', 'Sizes'],
        border: true
      },
      queryLoading: false,
      exportLoading: false,
      activeInstanceId: null,
      activeKey: null,
      cacheComValMap: {},
      pagerLoading: true,
      rawTableData: []
    }
  },
  computed: {
    completeColumns () {
      const columns = [...this.columns]
      columns.forEach((x) => x => {
        x.slots = {
          default: x.field
        }
      })
      if (this.serial) {
        columns.unshift({
          title: 'No.',
          type: 'seq'
        })
      }
      if (this.operationButtonList.length) {
        columns.push({
          title: 'Operation',
          field: 'operation',
          width: this.operationWidth,
          slots: {
            default: 'operation'
          },
          fixed: 'right'
        })
      }
      return columns
    }
  },
  async created () {
    this.isLoad && this.loadTableData()
  },
  methods: {
    operationButtonClick (row, button) {
      this.$emit('onOperationButtonClick', row, button.key, button)
    },
    getMergeCells () {
      const fn = this.getMergeCellsFn()
      return fn(this.columns)
    },
    getMergeCellsFn () {
      const temp = []
      let index = 0
      const list = this.tableData
      return function getMergeCellsByColumns (columns) {
        columns.forEach(column => {
          if (column.children) {
            getMergeCellsByColumns(column.children)
          } else {
            column.spanRowColumnKeys && temp.push(...getSpanRowMergeCells(list, index, column.spanRowColumnKeys))
            index++
          }
        })
        return temp
      }
    },
    handleGeneralButtonClick (name, button, extraData = {}) {
      this.$emit('onToolbarButtonClick', name, button, extraData)
    },
    transformCellText ({ text, column }) {
      if (this.nullStr && text === null) {
        return this.nullStr
      } else {
        if (column.valueMap) {
          return column.valueMap[text]
        }
        return text
      }
    },
    search () {
      this.pagination.currentPage = 1
      this.loadTableData()
    },
    initParams () {
      const arrayCom = ['cascader']
      this.formItems.forEach(item => {
        if (['rangeInput'].includes(item.type)) {
          this.$set(this.params, item.key, [undefined, undefined])
        } else {
          this.$set(this.params, item.key, undefined)
        }
        if (arrayCom.includes(item.type)) {
          this.$set(this.cacheComValMap, item.key, [])
        }
      })
    },
    handleTableChange ({ currentPage, pageSize }) {
      this.pagination.currentPage = currentPage
      this.pagination.pageSize = pageSize
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      this.tableData = []
      const params = cloneDeep(this.params)
      Object.keys(params).forEach(key => {
        if (Array.isArray(params[key]) && (params[key][0] === undefined || params[key][1] === undefined)) {
          delete params[key]
        }
      })
      const requestData = {
        ...params,
        ...this.extraData
      }
      if (!this.hidePagination) {
        Object.assign(requestData, {
          page: this.pagination.currentPage - 1,
          size: this.pagination.pageSize
        })
      }
      const result = await this.$api[this.queryApiName](requestData).finally(() => (this.queryLoading = false))
      this.rawTableData = result.content || []
      this.tableData = this.handleTableData(this.rawTableData)
      const mergeCells = this.getMergeCells()
      this.$nextTick(() => {
        this.$refs[this.randomRef].setMergeCells(mergeCells)
      })
      this.$emit('loaded', this.tableData)
      if (this.isSelect) {
        if (this.tableData.length) {
          this.handleClickRow(this.tableData[0])
        } else {
          this.handleClickRow(null)
        }
      }
      if (!this.hidePagination) {
        this.pagination.total = result.total
        this.pagerLoading = false
      }
    },
    handleTableData (rows) {
      rows = cloneDeep(rows)
      if (this.sorter) {
        rows.sort(this.sorter)
      }
      return rows
    },
    reset () {
      this.pagination.currentPage = 1
      this.initParams()
      this.loadTableData()
    },
    currentChangeEvent ({ row }) {
      if (!this.isSelect) return
      this.handleClickRow(row)
    },
    handleClickRow (record) {
      if (record) {
        this.$refs[this.randomRef].setCurrentRow(record)
      } else {
        this.$refs[this.randomRef].clearCurrentRow()
      }
      this.activeInstanceId = record ? record[this.rowKey] : null
      this.activeKey = record ? record._key : null
      this.$emit('selected', record)
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .toolbar-box-top {
  border-top: 1px solid #e8e8e8;
  padding-top: 10px;
}
/deep/ .toolbar-box {
  margin-bottom: 10px;
}
.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 10px;
  right: 0;
}
/deep/ .ant-form-item {
  margin-bottom: 0;
}
.ant-form-vertical /deep/ .ant-form-item {
  padding-bottom: 10px;
}
/deep/ .ant-form-item-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/deep/ .center-line:after {
  position: absolute;
  width: 24px;
  content: '~';
  line-height: 32px;
  text-align: center;
  float: left;
}
/deep/ .active-row {
  background: #bae7ff;
  cursor: default;
}
/deep/ .selectable-row {
  cursor: pointer;
}
/deep/ .vxe-pager--sizes {
  margin-right: 0;
}
/deep/ .row--selectable .vxe-body--row {
  cursor: pointer;
  &.row--current {
    cursor: default;
  }
}
</style>
