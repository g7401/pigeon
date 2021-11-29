<template>
  <a-card
    class="agent-work-time"
    :bordered="false"
    size="small"
  >
    <DqaDefCreateModal
      v-if="editVisible"
      v-model="editVisible"
      :item="activeItem"
      @closed="handleClosed"
      @saved="handleSaved"
    ></DqaDefCreateModal>
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
                  label="Data Source Name"
                  prop="dealer"
                  ref="dealer"
                  :autoLink="false"
                >
                  <a-input v-model="params.ds_name"></a-input>
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
      >Create DAQ Process Def</a-button>
    </a-row>
    <a-table
      ref="table"
      :bordered="true"
      class="more-small-table mg-b-15"
      rowKey="uid"
      :scroll="scroll"
      :columns="columns"
      :components="getComponents(columns, scroll)"
      :data-source="tableData"
      :loading="queryLoading"
      :pagination="pagination"
      size="small"
      @change="handleTableChange"
      :transformCellText="renderCellFn"
    >
      <template
        slot="serial"
        slot-scope="text, record, i"
      >
        {{ i + (pagination.current - 1) * pagination.pageSize + 1 }}
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
              icon="delete"
              size="small"
              type="danger"
            >Delete</a-button>
          </a-popconfirm>
          <a-button
            size="small"
            icon="setting"
            @click="goConfigPage(record)"
          >Configure</a-button>
        </a-button-group>
      </template>
    </a-table>
  </a-card>
</template>

<script>
import DSTreeSelect from '@/components/StoreCenter/DSTreeSelect'
import DqaDefCreateModal from '@/components/StoreCenter/DqaDefCreateModal'
import tableScrollMixin from '@/components/StoreCenter/mixins/tableScrollMixin'
import tableResizeMixin from '@/components/StoreCenter/mixins/tableResizeMixin'
// import { cutStrByFullLength } from '@/components/_util/util'
export default {
  mixins: [tableScrollMixin, tableResizeMixin],
  components: { DSTreeSelect, DqaDefCreateModal },
  name: 'DaqProcessDef',
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
      activeItem: null,
      editVisible: false,
      tableData: [],
      params: {
        dsUid: undefined
      },
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0,
        // showSizeChanger: true,
        showTotal: total => `${total} items`
        // pageSizeOptions: ['10', '20', '30', '40', '50', '60', '70', '80', '90', '100']
      },
      queryLoading: false,
      deleteLoading: false,
      operationWidth: 250,
      rawScroll: {
        x: 1800
      },
      rawColumns: [
        {
          title: 'Data Source Name',
          dataIndex: 'ds_name',
          key: 'ds_name',
          width: 200,
          ellipsis: true
        },
        {
          title: 'DAQ Process Def ID',
          dataIndex: 'uid',
          width: 280,
          ellipsis: true
        },
        {
          title: 'DAQ Process Def Name',
          dataIndex: 'name',
          width: 180,
          ellipsis: true
        },
        {
          title: 'DAQ Process Def Description',
          dataIndex: 'description',
          width: 200,
          ellipsis: true
        },
        {
          title: 'Create Time',
          dataIndex: 'create_time',
          key: 'create_time',
          width: 180,
          ellipsis: true
        },
        {
          title: 'Create By',
          dataIndex: 'create_by',
          key: 'create_by',
          width: 150
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
  async created () {
    await this.loadTableData()
  },
  methods: {
    renderCellFn ({ text }, maxLength = 30) {
      // if (typeof text !== 'string') return text
      // return cutStrByFullLength(text, maxLength)
      return text
    },
    handleClosed () {
      this.activeItem = null
    },
    handleSaved () {
      this.loadTableData()
    },
    updateItem (item) {
      this.activeItem = item
      this.editVisible = true
    },
    async deleteItem (item) {
      this.deleteLoading = true
      await this.$api.deleteDaqProcessDef(item.uid).finally(() => (this.deleteLoading = false))
      this.loadTableData()
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      this.tableData = []
      const resp = await this.$api
        .getDaqProcessDefs(this.params.ds_name, { page: this.pagination.current - 1, size: this.pagination.pageSize })
        .finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object.content || []
      this.pagination.total = resp.object.total
    },
    search () {
      this.pagination.current = 1
      this.loadTableData()
    },
    reset () {
      this.params = {}
      this.tableData = []
      this.loadTableData()
    },
    goConfigPage (item) {
      this.$router.push({
        name: 'DaqProcessConfig',
        query: {
          uid: item.uid
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
