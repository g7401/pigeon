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
                  :value="params[formItem.key]"
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
        v-if="hasExport"
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
    <a-table
      class="more-small-table"
      bordered
      rowKey="_key"
      :scroll="scroll"
      :columns="columns"
      :data-source="tableData"
      :loading="queryLoading"
      :pagination="hidePagination ? false : pagination"
      size="small"
      :custom-row="customRow"
      :row-class-name="rowClassName"
      v-bind="$attrs"
      :transformCellText="transformCellText"
      @change="handleTableChange"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i + (pagination.current - 1) * pagination.pageSize + 1 }}
      </template>
    </a-table>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import ExportButton from '@/components/common/ExportButton'
import SRangePicker from '@/components/common/SRangePicker'
import SUpload from '@/components/common/SUpload'
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
    hasExport: {
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
    }
  },
  data () {
    return {
      tableData: [],
      params: {},
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showTotal: total => `${total} items`,
        pageSizeOptions: ['10', '20', '30', '40', '50', '60', '70', '80', '90', '100']
      },
      queryLoading: false,
      exportLoading: false,
      activeInstanceId: null,
      activeKey: null,
      cacheComValMap: {}
    }
  },
  computed: {
    scroll () {
      return Object.assign({
        x: this.columns.length > 6 ? 'max-content' : false
      }, this.$attrs.scroll || {})
    }
  },
  async created () {
    this.isLoad && this.loadTableData()
  },
  methods: {
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
      this.pagination.current = 1
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
    handleTableChange (pagination) {
      this.pagination = pagination
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
          page: this.pagination.current - 1,
          size: this.pagination.pageSize
        })
      }
      const resp = await this.$api[this.queryApiName](requestData).finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = this.handleTableData(resp.object.content || resp.object.rows || resp.object || [])
      this.$emit('loaded', this.tableData)
      if (this.isSelect) {
        if (this.tableData.length) {
          this.clickRow(this.tableData[0])
        } else {
          this.clickRow(null)
        }
      }
      if (!this.hidePagination) {
        this.pagination.total = resp.object.total
      }
    },
    handleTableData (data) {
      const rows = data.slice()
      rows.forEach(row => {
        row._key = Math.random().toString()
      })
      if (this.sorter) {
        rows.sort(this.sorter)
      }
      return rows
    },
    reset () {
      this.pagination.current = 1
      this.initParams()
      this.loadTableData()
    },
    clickRow (record) {
      this.activeInstanceId = record ? record[this.rowKey] : null
      this.activeKey = record ? record._key : null
      this.$emit('selected', record)
    },
    rowClassName (record) {
      if (this.isSelect && this.activeKey && this.activeKey === record._key) {
        return 'active-row'
      }
      if (this.isSelect) {
        return 'selectable-row'
      }
    },
    customRow (record) {
      if (!this.isSelect) return false
      return {
        on: {
          click: () => {
            this.clickRow(record)
          }
        }
      }
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
</style>
