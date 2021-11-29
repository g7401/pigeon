<template>
  <div class="output-table">
    <a-card
      v-if="formItems.length"
      class="search-box"
      :bordered="false"
      size="small"
    >
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
                class="mg-t-5"
                :span="4"
                :key="formItem.key"
                v-for="formItem in visibleFormItems"
              >
                <a-form-model-item :label="formItem.label">
                  <a-input v-model="params[formItem.key]"></a-input>
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
    <!-- <a-row
      class="toolbar-box"
      v-if="columns.length > 1"
    >
      <a-button
        type="primary"
        icon="download"
        :loading="exportLoading"
        @click="exportTable"
      >Export</a-button>
    </a-row> -->
    <a-table
      class="more-small-table"
      v-if="columns.length > 1"
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
import ToggleList from '@/components/common/ToggleList'
export default {
  components: { ToggleList },
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
    },
    apiName: {
      type: String,
      default: 'queryTaskOutputData'
    }
  },
  data () {
    return {
      visibleFormItems: [],
      tableData: [],
      params: {},
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      queryLoading: false,
      exportLoading: false
    }
  },
  computed: {
    formItems () {
      return this.table.fields
        .filter(x => x.query)
        .map(field => ({
          label: field.table_column_comment || field.table_column_name,
          key: field.table_column_name
        }))
    },
    columns () {
      const columns = this.table.fields
        .filter(x => x.list)
        .map(field => ({
          title: field.table_column_comment || field.table_column_name,
          dataIndex: field.table_column_name,
          key: field.table_column_name
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
  created () {
    console.log('this.apiName', this.apiName)
    this.loadTableData()
  },
  methods: {
    handleSearch () {
      this.pagination.current = 1
      this.loadTableData()
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      if (!this.taskInstanceId) return
      this.queryLoading = true
      const resp = await this.$api[this.apiName](this.taskInstanceId, this.table.table_name, {
          page: this.pagination.current - 1,
          size: this.pagination.pageSize,
          ...this.params
        })
        .finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object.content
      this.pagination.total = resp.object.total_elements || resp.object.total
    },
    reset () {
      this.params = {}
      this.loadTableData()
    },
    exportTable () {
      this.exportLoading = true
      this.exportLoading = false
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
.ant-form-vertical /deep/ .ant-form-item {
  padding-bottom: 0;
}
/deep/ .ant-form-item-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0;
}
/deep/ .ant-form label {
  font-size: 12px;
}
</style>
