<template>
  <a-card
    class="agent-work-time"
    :bordered="false"
    size="small"
  >
    <DtrDefCreateModal v-if="editVisible" v-model="editVisible" :dtrDefUid="activeDtrDefUid" @closed="handleClosed" @saved="handleSaved"></DtrDefCreateModal>
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
              <a-col :span="8">
                <a-form-model-item
                  label="Data Target Name"
                >
                  <a-input
                    v-model="params.dt_name"
                    placeholder="Data Target Name" />
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
              @click="handleSearch"
              style="margin-right: 5px"
            >Serach</a-button>
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
        @click="editVisible = true"
      >Create DTR Process Def</a-button>
    </a-row>
    <a-table
      :bordered="true"
      class="more-small-table mg-b-15"
      rowKey="dtr_process_def_uid"
      :scroll="scroll"
      :columns="columns"
      :components="getComponents(columns, scroll)"
      :data-source="tableData"
      :loading="queryLoading"
      :pagination="pagination"
      :transformCellText="renderCellFn"
      @change="handleTableChange"
      size="small"
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
          <a-button
            size="small"
            type="primary"
            icon="edit"
            @click="updateItem(record)"
          >Edit</a-button>
          <a-popconfirm
            title="confirm delete?"
            cancel-text="No"
            ok-text="Yes"
            :loading="deleteLoading"
            @confirm="deleteItem(record)"
          >
            <a-button
              size="small"
              type="danger"
              icon="delete"
            >Delete</a-button>
          </a-popconfirm>
          <a-button size="small" icon="setting" @click="goConfigPage(record)">Configure</a-button>
        </a-button-group>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'
import DtrDefCreateModal from '@/components/StoreCenter/DtrDefCreateModal'
import tableScrollMixin from '@/components/StoreCenter/mixins/tableScrollMixin'
import tableResizeMixin from '@/components/StoreCenter/mixins/tableResizeMixin'
// import { cutStrByFullLength } from '@/components/_util/util'
export default {
  mixins: [tableScrollMixin, tableResizeMixin],
  components: { DSTreeSelect, DtrDefCreateModal },
  name: 'DtrProcessDef',
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
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      editVisible: false,
      activeDtrDefUid: null,
      tableData: [],
      params: {
        dt_name: undefined
      },
      queryLoading: false,
      deleteLoading: false,
      operationWidth: 250,
      rawScroll: {
        x: 1650
      },
      rawColumns: [
        {
          title: 'Data target Name',
          dataIndex: 'dt_name',
          width: 150,
          ellipsis: true
        },
        {
          title: 'DTR Process Def ID',
          dataIndex: 'dtr_process_def_uid',
          width: 250,
          ellipsis: true
        },
        {
          title: 'DTR Process Def Name',
          dataIndex: 'dtr_process_def_name',
          width: 200,
          ellipsis: true
        },
        {
          title: 'DTR Process Def Description',
          dataIndex: 'dtr_process_def_description',
          width: 200,
          ellipsis: true
        },
        {
          title: 'Create Time',
          dataIndex: 'create_time',
          width: 150
        },
        {
          title: 'Create By',
          dataIndex: 'create_by',
          width: 100
        },
        {
          title: 'Last Update Time',
          dataIndex: 'last_update_time',
          key: 'last_update_time',
          width: 150
        },
        {
          title: 'Last Update By',
          dataIndex: 'last_update_by',
          key: 'last_update_by'
        }
      ]
    }
  },
  created () {
    this.loadTableData()
  },
  methods: {
    renderCellFn ({ text }, maxLength = 30) {
      return text
      // if (typeof text !== 'string') return text
      // return cutStrByFullLength(text, maxLength)
    },
    handleSearch () {
      this.pagination.current = 1
      this.loadTableData()
    },
    handleClosed () {
      this.activeDtrDefUid = null
    },
    handleSaved () {
      this.loadTableData()
    },
    updateItem (item) {
      this.activeDtrDefUid = item.dtr_process_def_uid
      this.editVisible = true
    },
    async deleteItem (item) {
      this.deleteLoading = true
      await this.$api.deleteDtrProcessDef(item.dtr_process_def_uid).finally(() => (this.deleteLoading = false))
      this.loadTableData()
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      this.tableData = []
      const resp = await this.$api.getDtrDefList(this.params.dt_name, {
          page: this.pagination.current - 1,
          size: this.pagination.pageSize
        }).finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object.content || []
      this.pagination.total = resp.object.total
    },
    reset () {
      this.params = {}
      this.loadTableData()
    },
    goConfigPage (item) {
      this.$router.push({
        name: 'DtrProcessConfig',
        query: {
          dtr_process_def_uid: item.dtr_process_def_uid
        }
      })
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
// /deep/ td {
//   text-overflow: ellipsis;
//   overflow: hidden;
// }
</style>
