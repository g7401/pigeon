<template>
  <a-card
    size="small"
    :bordered="false"
  >
    <a-icon v-if="identifyTypeLoading" type="loading" />
    <a-form-model-item v-if="!identifyTypeLoading" label="Type Of Product Identity">
      <a-select
        v-model="identifyType"
        :options="identifyTypeOptions"
      ></a-select>
    </a-form-model-item>
    <RelationGraph
      class="mg-t-10 mg-b-10"
      v-if="graphSetting"
      :nodes="graphSetting.nodes"
      :edges="graphSetting.edges"
    ></RelationGraph>
    <div class="base-table">
      <a-row
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
              <a-col :span="6">
                <a-form-model-item label="Type of Code">
                  <a-select
                    v-model="params.query_type_code"
                    :options="typeOfCodeOptions"
                  ></a-select>
                </a-form-model-item>
              </a-col>
              <a-col :span="6">
                <a-form-model-item label="Value of Code">
                  <a-input v-model="code" />
                </a-form-model-item>
              </a-col>
            </a-row>
            <a-row
              v-if="identifyType !== 'Material Code'"
              :gutter="24"
            >
              <a-col :span="12">
                <a-form-model-item label="Filter Advanced Rules">
                  <a-select
                    v-model="params.rule_name"
                    :options="advancedRulesOptions"
                    allowClear
                    placeholder="None"
                  ></a-select>
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
      <!-- <a-row
        class="toolbar-box toolbar-box-top"
      >
        <ExportButton
          :totalRecord="pagination.total"
          :exportApi="exportApi"
          :exportFileApi="exportFileApi"
          :params="params"
        ></ExportButton>
      </a-row> -->
      <a-table
        class="more-small-table"
        bordered
        rowKey="_key"
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
          {{ i + (pagination.current - 1) * pagination.pageSize + 1 }}
        </template>
      </a-table>
    </div>
  </a-card>
</template>

<script>
import cloneDeep from 'lodash.clonedeep'
import RelationGraph from '@/components/common/RelationGraph'
import ExportButton from '@/components/common/ExportButton'
const labelCfg = {
  refY: 5,
  style: {
    fill: '#108ee9',
    lineWidth: 2,
    fontSize: 8
  }
}
export default {
  components: {
    RelationGraph,
    ExportButton
  },
  data () {
    return {
      identifyType: null,
      identifyTypeLoading: false,
      typeOfCodeLoading: false,
      headerLoading: false,
      identifyTypeOptions: [],
      typeOfCodeOptions: [],
      advancedRulesOptions: [],
      code: undefined,

      columns: [],
      tableData: [],
      params: {
        query_type_code: undefined,
        rule_name: undefined
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        showSizeChanger: true,
        showTotal: total => `${total} items`,
        pageSizeOptions: ['10', '20', '30', '40', '50', '60', '70', '80', '90', '100']
      },
      queryLoading: false,
      exportLoading: false
    }
  },
  computed: {
    scroll () {
      return {
        x: this.columns.length > 6 ? 'max-content' : false
      }
    },
    extraData () {
      return {
        product_id_key: this.identifyType
      }
    },
    graphSetting () {
      if (!this.identifyType || this.identifyType === 'Material Code') return null
      return {
        nodes: [
          {
            id: 'rsu1',
            label: this.identifyType,
            node_type: 'main'
          },
          {
            id: 'material1',
            label: 'Material Code',
            node_type: 'main'
          },
          {
            id: 'material2',
            label: 'Material Code',
            node_type: 'main'
          },
          {
            id: 'rsu2',
            label: this.identifyType,
            node_type: 'main'
          },
          {
            id: 'material3',
            label: 'Material Code',
            node_type: 'main'
          }
        ],
        edges: [
          {
            source: 'rsu1',
            target: 'material1',
            label: '1         N',
            labelCfg
          },
          {
            source: 'material2',
            target: 'rsu2',
            label: '1         1',
            labelCfg
          },
          {
            source: 'rsu2',
            target: 'material3',
            label: '1         N',
            labelCfg
          }
        ]
      }
    }
  },
  watch: {
    identifyType: {
      immediate: true,
      handler () {
        this.identifyType && this.onIdentifyChange()
      }
    },
    'params.query_type_code': {
      immediate: true,
      handler () {
        this.identifyType !== 'Material Code' && this.params.query_type_code && this.onTypeOfCodeChange()
      }
    }
  },
  async created () {
    this.getIdentifyTypeOptions()
    this.getIdentifyTableHeader()
  },
  methods: {
    async onIdentifyChange () {
      this.typeOfCodeLoading = true
      const resp = await this.$api.getTypeOfCodeOptions(this.identifyType).finally(() => {
        this.typeOfCodeLoading = false
      })
      if (!resp || !resp.object) return
      this.typeOfCodeOptions = resp.object.map(x => ({
        label: x,
        value: x
      }))
      this.params.query_type_code = resp.object[0]
      this.params.rule_name = undefined
      !this.tableData.length && this.loadTableData()
    },
    async onTypeOfCodeChange () {
      const resp = await this.$api.getAdvancedRulesOptions(this.identifyType, this.params.query_type_code)
      const options = resp.object.map(x => ({
        label: x,
        value: x
      }))
      options.unshift({
        label: 'None',
        value: undefined
      })
      this.advancedRulesOptions = options
    },
    async getIdentifyTypeOptions () {
      this.identifyTypeLoading = true
      const resp = await this.$api.getIdentifyTypeOptions().finally(() => {
        this.identifyTypeLoading = false
      })
      if (!resp || !resp.object) return
      this.identifyTypeOptions = resp.object.map(x => ({
        label: x,
        value: x
      }))
      this.identifyType = resp.object[0]
    },
    async getIdentifyTableHeader () {
      this.columns = [
        {
          dataIndex: 'serial',
          title: 'No.',
          scopedSlots: { customRender: 'serial' }
        }
      ]
      this.headerLoading = true
      const resp = await this.$api.getIdentifyTableHeader().finally(() => {
        this.headerLoading = false
      })
      if (!resp || !resp.object) return
      this.columns = this.columns.concat(
        resp.object.map(x => ({
          dataIndex: x.uid,
          title: x.name
        }))
      )
    },

    search () {
      this.pagination.current = 1
      this.loadTableData()
    },
    initParams () {
      this.params.rule_name = undefined
      this.code = undefined
    },
    handleTableChange (pagination) {
      this.pagination = pagination
      this.loadTableData()
    },
    async loadTableData () {
      if (!this.identifyType) return
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
      const resp = await this.$api
        .getIdentifyTableData(this.code ? [this.code] : undefined, requestData)
        .finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = this.handleTableData(resp.object.content || resp.object.rows || resp.object || [])
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
      return rows
    },
    reset () {
      this.pagination.current = 1
      this.initParams()
      this.loadTableData()
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  text-align: center;
  font-size: 20px;
  margin-top: 30px;
  margin-bottom: 30px;
}
/deep/ .graph-box {
  height: 250px;
}

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
