<template>
  <a-card
    class="agent-work-time"
    :bordered="false"
    size="small"
  >
    <a-card
      class="search-box"
      :bordered="false"
      size="small"
    >
      <a-row
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
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-model-item
                  label="经销商"
                  prop="dealer"
                  ref="dealer"
                  :autoLink="false"
                >
                  <s-cascader
                    :options="dealerOptions"
                    expandTrigger="hover"
                    placeholder="经销商"
                    v-model="dealer"
                    :disabled="isDisabledCascader"
                    @change="() => { $refs.dealer.onFieldChange();handleCascaderChange() }"
                  />
                </a-form-model-item>
              </a-col>
              <a-col :span="12">
                <a-form-model-item
                  label="时间区间"
                  :autoLink="false"
                >
                  <a-range-picker
                    ref="dateRange"
                    v-model="dateRange"
                    @change="datePickChange"
                  />
                </a-form-model-item>
              </a-col>
            </a-row>
          </a-form-model>
        </a-col>
        <a-col :span="4">
          <div class="search-btn-group">
            <a-button
              type="primary"
              icon="search"
              :loading="queryLoading"
              @click="loadTableData"
              style="margin-right: 5px"
            >查询</a-button>
            <a-button
              icon="reload"
              :loading="queryLoading"
              @click="reset"
            >重置</a-button>
          </div>
        </a-col>
      </a-row>
    </a-card>
    <a-row
      class="toolbar-box"
      v-if="columns.length > 1"
    >
      <a-button
        type="primary"
        icon="download"
        :loading="exportLoading"
        @click="exportResult"
      >导出</a-button>
    </a-row>
    <h3 class="title">汇总</h3>
    <a-table
      class="more-small-table mg-b-15"
      v-if="columns.length > 1"
      :scroll="scroll"
      :columns="columns"
      :data-source="tableData"
      :loading="queryLoading"
      :pagination="false"
      size="small"
      :row-class-name="rowClassName"
      :custom-row="customRow"
    >
    </a-table>
    <h3 class="title">明细</h3>
    <a-table
      class="more-small-table"
      :columns="detailColumns"
      :data-source="detailTableData"
      :loading="queryLoading"
      :pagination="false"
      size="small"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i+1 }}
      </template>
    </a-table>
  </a-card>
</template>

<script>
import { mapGetters } from 'vuex'
import SCascader from '@/components/common/SCascader'
import D1Api from '@/api/d1'
export default {
  name: 'AgentWorkTime',
  components: {
    SCascader
  },
  props: {
    table: {
      type: Object,
      default () {
        return {}
      }
    },
    taskInstanceId: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      isDisabledCascader: false,
      tableData: [],
      dateRange: [],
      dealer: [],
      params: {
        supplier_name: undefined,
        start_date: undefined,
        end_date: undefined
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      queryLoading: false,
      exportLoading: false,
      columns: [
        {
          title: '经销商名称',
          dataIndex: 'supplier_name',
          key: 'supplier_name'
        },
        {
          title: '开始日期',
          dataIndex: 'query_start_date',
          key: 'query_start_date'
        },
        {
          title: '结束时间',
          dataIndex: 'query_end_date',
          key: 'query_end_date'
        },
        {
          title: '期间工作日数量',
          dataIndex: 'query_workday_num',
          key: 'query_workday_num'
        },
        {
          title: '业务员人数',
          dataIndex: 'query_regions',
          key: 'query_regions'
        },
        {
          title: '每个业务员每天在店时长',
          dataIndex: 'avg_time_in_store',
          key: 'avg_time_in_store'
        },
        {
          title: '每个业务员每天在途时长',
          dataIndex: 'avg_on_road_time',
          key: 'avg_on_road_time'
        },
        {
          title: '每个业务员每天工作时长',
          dataIndex: 'avg_work_time',
          key: 'avg_work_time'
        },
        {
          title: '每个业务员每天带店量',
          dataIndex: 'avg_store_count',
          key: 'avg_store_count'
        },
        // {
        //   title: '每个业务员每天Buffer (Hr)',
        //   dataIndex: 'query_end_date',
        //   key: 'query_end_date'
        // },
        {
          title: '每个业务员每天工作小时数',
          dataIndex: 'avg_work_hours',
          key: 'avg_work_hours'
        }
        // {
        //   title: '计划制定明细',
        //   dataIndex: 'query_end_date',
        //   key: 'query_end_date'
        // }
      ],
      detailColumns: [
        {
          title: 'No.',
          dataIndex: 'serial',
          key: 'serial',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: '经销商名称',
          dataIndex: 'supplier_name',
          key: 'supplier_name'
        },
        {
          title: '业务员编号',
          dataIndex: 'agent_code',
          key: 'agent_code'
        },
        {
          title: '业务员姓名',
          dataIndex: 'agent_name',
          key: 'agent_name'
        },
        {
          title: '开始日期',
          dataIndex: 'query_start_date',
          key: 'query_start_date'
        },
        {
          title: '结束日期',
          dataIndex: 'query_end_date',
          key: 'query_end_date'
        },
        {
          title: '期间工作日数量',
          dataIndex: 'query_workday_num',
          key: 'query_workday_num'
        },
        {
          title: '每天在店时长',
          dataIndex: 'avg_time_in_store',
          key: 'avg_time_in_store'
        },
        {
          title: '每天在途时长',
          dataIndex: 'avg_on_road_time',
          key: 'avg_on_road_time'
        },
        {
          title: '每天工作时长',
          dataIndex: 'avg_work_time',
          key: 'avg_work_time'
        },
        {
          title: '每天带店量',
          dataIndex: 'avg_store_count',
          key: 'avg_store_count'
        },
        {
          title: '每天工作小时数',
          dataIndex: 'avg_work_hours',
          key: 'avg_work_hours'
        }
      ],
      detailTableData: [],
      dealerOptions: [],
      activeInstanceId: null
    }
  },
  computed: {
    scroll () {
      return {
        x: this.columns.length > 6 ? 'max-content' : false
      }
    },
    ...mapGetters(['nickname'])
  },
  created () {
    if (this.nickname === 'xuefeng') {
      this.params.supplier_name = '雪峰食品配送中心'
      this.isDisabledCascader = true
    }
    this.loadDealers()
    this.loadTableData()
  },
  watch: {
    activeInstanceId (newVal) {
      if (newVal) {
        this.loadDetailTableData()
      }
    }
  },
  methods: {
    datePickChange () {
      if (this.dateRange.length) {
        const [start, end] = this.dateRange
        this.params.start_date = start.format('YYYY-MM-DD')
        this.params.end_date = end.format('YYYY-MM-DD')
        this.loadTableData()
      }
    },
    handleCascaderChange () {
      this.params.supplier_name = this.dealer.slice(-1)[0]
      console.log('this.params.supplier_name', this.params.supplier_name)
      this.loadTableData()
    },
    async loadDealers () {
      const resp = await this.$api.getDealerCascade()
      if (resp && resp.success) {
        this.dealerOptions = resp.object
      }
    },
    // handleTableChange (pagination) {
    //   this.pagination.current = pagination.current
    //   this.loadTableData()
    // },
    async loadTableData () {
      this.queryLoading = true
      const resp = await this.$api.getAgentWorkTimeReport(this.params.supplier_name, this.params.start_date, this.params.end_date).catch(() => (this.queryLoading = false))
      this.queryLoading = false
      if (!resp || !resp.object) return
      this.tableData = resp.object.rp_agent_report_summary_dtos
      if (this.tableData && this.tableData.length) {
        this.activeInstanceId = this.tableData[0].plan_instance_id
      } else {
        this.activeInstanceId = null
        this.detailTableData = []
      }
    },
    async loadDetailTableData () {
      this.queryLoading = true
      const resp = await this.$api.getAgentDetailReport(this.activeInstanceId).catch(() => (this.queryLoading = false))
      this.queryLoading = false
      if (!resp || !resp.object) return
      this.detailTableData = resp.object.rp_agent_report_details_dtos
    },
    reset () {
      this.params = {}
      this.loadTableData()
    },
    exportTable () {
      this.exportLoading = true
      this.exportLoading = false
    },
    rowClassName (record) {
      if (this.activeInstanceId && this.activeInstanceId === record.plan_instance_id) {
        return 'active-row'
      }
    },
    customRow (record) {
      return {
        on: {
          click: () => {
            this.activeInstanceId = record.plan_instance_id
          }
        }
      }
    },
    async exportResult () {
      this.exportLoading = true
      const resp = await this.$api.exportReport(this.activeInstanceId).finally(() => { this.exportLoading = false })
      if (resp.success && resp.object) {
        window.location.href = `${D1Api.download}?file_id=${resp.object}`
      }
    }
  }
}
</script>

<style lang="less" scoped>
.title {
  margin-bottom: 10px;
  font-size: 20px;
  font-weight: 500;
}
/deep/ .active-row {
  background: #1890ff;
  cursor: default;
}
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
.ant-form-vertical /deep/ .ant-form-item {
  padding-bottom: 0;
}
/deep/ .ant-form-item-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
