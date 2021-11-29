<template>
  <a-card
    :bordered="false"
    size="small"
  >
    <CreateInstanceModal
      v-model="showCreateModal"
      @done="handleDone"
    ></CreateInstanceModal>

    <div class="output-table">
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
                <a-col :span="6">
                  <a-form-model-item label="Data Source">
                    <DSTreeSelect
                      v-model="params.ds_uid"
                      @input="handleSearch"
                    ></DSTreeSelect>
                  </a-form-model-item>
                </a-col>
                <a-col :span="6">
                  <a-form-model-item label="DAQ Process Instance Status">
                    <a-select
                      allowClear
                      v-model="params.status"
                      @change="handleSearch"
                      :options="statusOptions"
                    ></a-select>
                  </a-form-model-item>
                </a-col>
                <a-col :span="6">
                  <a-form-model-item label="Create Time">
                    <SRangePicker
                      v-model="params.create_time"
                      @input="handleSearch"
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
        class="toolbar-box"
        v-if="columns.length > 1"
      >
        <a-button
          class="pull-right"
          type="primary"
          icon="plus-circle"
          @click="showCreateModal = true"
        >Create DAQ Process Instance</a-button>
      </a-row>
      <a-table
        :bordered="true"
        class="selectable-table more-small-table"
        v-if="columns.length > 1"
        :scroll="scroll"
        :columns="columns"
        :components="getComponents(columns, scroll)"
        :data-source="tableData"
        :loading="queryLoading"
        :pagination="pagination"
        size="small"
        :custom-row="customRow"
        :row-class-name="rowClassName"
        rowKey="daq_process_inst_uid"
        @change="handleTableChange"
      >
        <template
          slot="serial"
          slot-scope="text, record, i"
        >
          {{ i+(pagination.current - 1) * pagination.pageSize + 1 }}
        </template>
        <template
          slot="operation"
          slot-scope="text, record"
        >
          <a-button-group>
            <a-popconfirm
              v-for="button in buttons"
              :key="button.operationType"
              :title="'confirm ' + button.operationType + ' ?'"
              cancel-text="No"
              ok-text="Yes"
              @confirm="changeStatus(record, button.operationType)"
            >
              <a-button
                v-if="button.showStatus.includes(record.daq_process_inst_status)"
                :type="button.type || 'default'"
                size="small"
                :ghost="button.isGhost"
                :loading="statusLoading"
              >{{ firstLetterUpper(button.operationType) }}</a-button>
            </a-popconfirm>
          </a-button-group>
        </template>
      </a-table>
    </div>
    <div
      class="task-instance-box"
      v-if="activeInstanceId"
    >
      <h3 class="title">Below is the DAQ Process Pipeline of selected DAQ Process Instance:({{ activeInstanceId }})</h3>
      <a-tabs
        type="card"
        size="large"
        v-model="activeTab"
        @change="handleTabChange"
      >
        <a-tab-pane
          v-for="tabKey in tabs"
          :key="tabKey"
          :tab="firstLetterUpper(tabKey)"
        >
          <template v-if="taskInstance">
            <TaskInstanceInfo
              :type="tabKey"
              :taskInstance="taskInstance"
              apiName="getDaqOutTableData"
            >
            </TaskInstanceInfo>
          </template>
          <template v-else>
            <template v-if="!taskLoading">
              <a-divider dashed>no {{ activeTab }}</a-divider>
            </template>
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-card>
</template>

<script>

import { firstLetterUpper } from '@/components/_util/util'
import TaskInstanceInfo from '@/components/StoreCenter/TaskInstanceInfo'
import CreateInstanceModal from '@/components/StoreCenter/CreateInstanecModal'
import TransformTaskInstance from '@/components/DIDefinitions/TransformTaskInstance'
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'
import tableScrollMixin from '@/components/StoreCenter/mixins/tableScrollMixin'
import tableResizeMixin from '@/components/StoreCenter/mixins/tableResizeMixin'
import SRangePicker from '@/components/common/SRangePicker'
export default {
  name: 'DaqExecutions',
  mixins: [tableScrollMixin, tableResizeMixin],
  components: {
    TaskInstanceInfo,
    TransformTaskInstance,
    CreateInstanceModal,
    DSTreeSelect,
    SRangePicker
  },
  data () {
    return {
      tableData: [],
      selectedDateRange: null,
      params: {},
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      queryLoading: false,
      buttons: [
        // { operationType: 'cancel', showStatus: ['WAITING'], type: 'primary', isGhost: false },
        // { operationType: 'suspend', showStatus: ['RUNNING'], type: 'primary', isGhost: false },
        // { operationType: 'resume', showStatus: ['SUSPENDED'], type: 'primary', isGhost: false },
        // { operationType: 'rollback', showStatus: ['DONE'], type: 'primary', isGhost: false },
        { operationType: 'delete', showStatus: ['CANCELED', 'SUSPENDED', 'FAILED'], type: 'danger' }
      ],
      statusOptions: [
        {
          label: 'ALL',
          value: undefined
        },
        {
          label: 'WAITING',
          value: 'WAITING'
        },
        {
          label: 'RUNNING',
          value: 'RUNNING'
        },
        {
          label: 'DONE',
          value: 'DONE'
        },
        {
          label: 'CANCELED',
          value: 'CANCELED'
        },
        {
          label: 'FAILED',
          value: 'FAILED'
        },
        {
          label: 'SUSPENDED',
          value: 'SUSPENDED'
        }
        // {
        //   label: 'ROLLBACKED',
        //   value: 'ROLLBACKED'
        // }
      ],
      operationWidth: 100,
      rawScroll: {
        x: 1550
      },
      rawColumns: [
        {
          title: 'Data Source Name',
          dataIndex: 'ds_name',
          width: 150,
          ellipsis: true
        },
        {
          title: 'DAQ Process Definition ID',
          dataIndex: 'daq_process_def_uid',
          width: 250,
          ellipsis: true
        },
        {
          title: 'DAQ Process Instance ID',
          dataIndex: 'daq_process_inst_uid',
          width: 250,
          ellipsis: true
        },
        {
          title: 'DAQ Process Instance Status',
          dataIndex: 'daq_process_inst_status',
          width: 150
        },
        {
          title: 'Create Time',
          key: 'create_time',
          dataIndex: 'create_time',
          width: 150,
          ellipsis: true
        },
        {
          title: 'Create By',
          key: 'create_by',
          dataIndex: 'create_by',
          width: 150,
          ellipsis: true
        },
        {
          title: 'Last Update Time',
          key: 'last_update_time',
          dataIndex: 'last_update_time',
          width: 150,
          ellipsis: true
        },
        {
          title: 'Last Update By',
          key: 'last_update_by',
          dataIndex: 'last_update_by'
        }
      ],
      taskLoading: false,
      statusLoading: false,
      showCreateModal: false,
      activeTab: 'connect',
      tabs: ['connect', 'extract', 'load', 'publish'],
      activeInstanceId: null,
      taskInstance: null
    }
  },
  created () {
  },
  methods: {
    handleSearch () {
      this.pagination.current = 1
      this.loadTableData()
    },
    firstLetterUpper,
    datePickChange (value) {
      this.selectedDateRange = value
      if (value) {
        this.params.create_time = this.selectedDateRange.format('YYYY-MM-DD')
      } else {
        this.params.create_time = undefined
      }
      this.handleSearch()
    },
    async loadTask () {
      if (!this.activeInstanceId) {
        return
      }
      this.taskInstance = null
      const apiKey = `getDaq${firstLetterUpper(this.activeTab)}Detail`
      this.taskLoading = true
      const resp = await this.$api[apiKey](this.activeInstanceId).finally(() => {
        this.taskLoading = false
      })
      if (resp.success && resp.object) {
        this.taskInstance = resp.object
      }
    },
    handleTabChange () {
      this.loadTask()
    },
    async changeStatus (item, type) {
      this.statusLoading = true
      // const apiKey = `${type}ProcessInstance`
      const resp = await this.$api.deleteDaqProcessInst(item.daq_process_inst_uid).finally(() => (this.statusLoading = false))
      if (resp && resp.success) {
        this.loadTableData()
      }
    },
    handleDone (instance) {
      this.loadTableData()
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      if (!this.params.ds_uid) return
      this.queryLoading = true
      console.log('this.params', this.params)
      const resp = await this.$api
        .getDaqExecList({
          page: this.pagination.current - 1,
          size: this.pagination.pageSize,
          ...this.params
        })
        .finally(() => (this.queryLoading = false))
      this.queryLoading = false
      if (!resp || !resp.object) return
      this.tableData = resp.object.content || []
      this.pagination.total = resp.object.total
      if (this.tableData.length) {
        this.clickRow(this.tableData[0])
      } else {
        this.activeInstanceId = null
      }
    },
    reset () {
      this.params = Object.assign({}, { ds_uid: this.params.ds_uid })
      this.selectedDateRange = null
      this.tableData = []
      this.handleSearch()
    },
    clickRow (record) {
      console.log('record', record)
      this.activeInstanceId = record.daq_process_inst_uid
      this.loadTask()
    },
    rowClassName (record) {
      if (this.activeInstanceId && this.activeInstanceId === record.daq_process_inst_uid) {
        return 'active-row'
      }
    },
    customRow (record) {
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
.title {
  text-align: center;
  font-size: 20px;
  margin-top: 30px;
  margin-bottom: 30px;
}
.task-instance-box {
  margin-top: 20px;
}
/deep/ .ant-divider {
  font-weight: 300;
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
/deep/ .active-row {
  background: #bae7ff;
  cursor: default;
}
/deep/ .selectable-table .ant-table-row {
  cursor: pointer;
}
</style>
