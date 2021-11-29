<template>
  <div class="output-table">
    <a-card
      class="search-box"
      :bordered="false"
      size="small"
    >
      <a-icon
        class="tree-loading-icon"
        type="loading"
        v-if="settingLoading"
      />
      <a-row
        type="flex"
        justify="end"
        class="toggle-row"
      >
        <toggle-list
          :list="formItems"
          :showLenth="12"
          @change="visibleFormItems=$event"
        ></toggle-list>
      </a-row>
      <a-row
        v-if="formItems.length"
        type="flex"
        justify="space-between"
      >
        <a-col :span="20">
          <a-form-model
            layout="vertical"
            :colon="false"
            :model="params"
            :labal-col="{ span: 6 }"
          >
            <a-row :gutter="10">
              <a-col
                :span="formItem.span"
                :key="formItem.key"
                v-for="formItem in visibleFormItems"
              >
                <a-form-model-item :label="formItem.label">
                  <a-input
                    v-model="params[formItem.key]"
                    v-if="formItem.type === 'singleInput'"
                  ></a-input>
                  <a-row
                    :gutter="24"
                    v-if="formItem.type === 'rangeInput'"
                  >
                    <a-col
                      :span="12"
                      class="center-line"
                    >
                      <a-input
                        v-model="params[formItem.key][0]"
                        allowClear
                      ></a-input>
                    </a-col>
                    <a-col :span="12">
                      <a-input
                        v-model="params[formItem.key][1]"
                        allowClear
                      ></a-input>
                    </a-col>
                  </a-row>
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-form-model>
        </a-col>
        <a-col :span="4">
          <div class="search-btn-group">
            <a-button
              class="mg-r-5"
              type="primary"
              icon="search"
              :loading="queryLoading"
              @click="handleSearch"
            >Search</a-button>
            <a-button
              icon="reload"
              :loading="queryLoading"
              @click="reset"
            >Reset</a-button>
          </div>
        </a-col>
      </a-row>
    </a-card>
    <a-row
      v-if="hasExport"
      class="toolbar-box"
    >
      <ExportButton
        :totalRecord="pagination.total"
        :params="params"
      ></ExportButton>
    </a-row>
    <a-table
      class="more-small-table"
      v-if="columns.length > 1"
      bordered
      :scroll="scroll"
      :columns="columns"
      :data-source="tableData"
      :loading="queryLoading"
      :pagination="pagination"
      size="small"
      @change="handleTableChange"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i+(pagination.current - 1) * pagination.pageSize + 1 }}
      </template>
    </a-table>
  </div>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import ExportButton from '@/components/common/ExportButton'
import ToggleList from '@/components/common/ToggleList'
export default {
  components: { ExportButton, ToggleList },
  props: {
    settingApiName: {
      type: String,
      default: 'getEntityQuerySetting'
    },
    queryApiName: {
      type: String,
      default: 'getEntityQueryData'
    },
    extraData: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      visibleFormItems: [],
      hasExport: true,
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
      settingLoading: false,
      queryLoading: false,
      exportLoading: false,
      setting: null,
      fields: null,
      columnNameMap: {}
    }
  },
  computed: {
    formItems () {
      if (!this.setting || !this.setting.form) return []
      return this.setting.form
        .filter(x => x.query)
        .map(field => {
          const item = {
            label: field.name,
            key: field.name
          }
          if (['RANGE_MATCH'].includes(field.query_filter_type)) {
            item.span = 8
            item.type = 'rangeInput'
          } else {
            item.span = 4
            item.type = 'singleInput'
          }
          return item
        })
    },
    columns () {
      const fields = this.fields || (this.setting && this.setting.table)
      if (!fields) return []
      const columns = fields
        .filter(x => x.list)
        .map(field => ({
          title: field.name,
          dataIndex: field.uid,
          key: field.uid
        }))
      const serialColumn = {
        title: 'No.',
        dataIndex: 'serial',
        key: 'serial',
        scopedSlots: { customRender: 'serial' }
      }
      columns.unshift(serialColumn)
      return columns
    },
    scroll () {
      return {
        x: this.columns.length > 6 ? 'max-content' : false
      }
    }
  },
  async created () {
    console.log('this.apiName', this.apiName)
    await this.loadSetting()
    this.loadTableData()
  },
  methods: {
    handleSearch () {
      this.pagination.current = 1
      this.loadTableData()
    },
    initParams () {
      this.setting.form
        .filter(x => x.query)
        .forEach(item => {
          if (['RANGE_MATCH'].includes(item.query_filter_type)) {
            this.$set(this.params, item.name, [undefined, undefined])
          } else {
            this.$set(this.params, item.name, undefined)
          }
        })
      console.log('init params: ', this.params)
    },
    async loadSetting () {
      this.settingLoading = true
      const resp = await this.$api[this.settingApiName]({
        ...this.extraData
      }).finally(() => (this.settingLoading = false))
      if (!resp || !resp.object) return
      this.setting = resp.object
      this.initParams()
    },
    handleTableChange (pagination) {
      this.pagination = pagination
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      const params = cloneDeep(this.params)
      Object.keys(params).forEach(key => {
        if (Array.isArray(params[key]) && (params[key][0] === undefined || params[key][1] === undefined)) {
          delete params[key]
        }
      })
      const resp = await this.$api[this.queryApiName]({
        page: this.pagination.current - 1,
        size: this.pagination.pageSize,
        ...params,
        ...this.extraData
      }).finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object.content
      this.fields = resp.object.fields
      this.pagination.total = resp.object.total
    },
    reset () {
      this.initParams()
      this.loadTableData()
    }
  }
}
</script>

<style lang="less" scoped>
/deep/ .toolbar-box {
  border-top: 1px solid #e8e8e8;
}
/deep/ .toolbar-box {
  padding: 10px 0;
}
.search-btn-group {
  text-align: right;
  padding: 0;
  position: absolute;
  bottom: 0;
  right: 0;
}
/deep/ .ant-form-item {
  margin-bottom: 0;
}
.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}
/deep/ .ant-form-vertical .ant-form-item-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0;
}
/deep/ .center-line:after {
  position: absolute;
  width: 24px;
  content: '~';
  line-height: 32px;
  text-align: center;
  float: left;
}
/deep/ .toolbar-box {
  border-top: 1px solid #e8e8e8;
}
/deep/ .toolbar-box {
  padding: 10px 0;
}
/deep/ .ant-form label {
  font-size: 12px;
}
</style>
