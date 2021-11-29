<template>
  <div>
    <a-modal
      :title="config.title"
      :ok-text="config.okText"
      :cancel-text="config.cancelText"
      :visible="value"
      :confirm-loading="config.confirmLoading"
      :width="config.width"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-table
        :bordered="true"
        class="more-small-table"
        v-if="columns.length > 1"
        rowKey="uid"
        :columns="columns"
        :data-source="tableData"
        :loading="queryLoading"
        :pagination="pagination"
        size="small"
        :custom-row="customRow"
        :row-class-name="rowClassName"
        @change="handleTableChange"
      >
        <template
          slot="serial"
          slot-scope="text, record, i"
        >
          {{ i+(pagination.current - 1) * pagination.pageSize + 1 }}
        </template>
        <template slot="plugin" slot-scope="text">
          {{ text ? '是' : '否' }}
        </template>
      </a-table>
      <div class="detail">
        <div
          v-if="processor"
          class="processor-detail"
        >
          <KeyValueInfo
            v-if="processor"
            :config="{title: detailTitle, bordered: true}"
            :options="detailOptions"
            :obj="processor"
          ></KeyValueInfo>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'
export default {
  components: { KeyValueInfo },
  props: {
    value: {
      type: Boolean,
      default () {
        return false
      }
    },
    modelConfig: {
      type: Object,
      default () {
        return this.baseConfig
      }
    },
    options: {
      type: Object,
      default () {
        return {}
      }
    },
    type: {
      type: String,
      default: 'EXTRACT'
    }
  },
  data () {
    return {
      baseConfig: {
        title: '',
        okText: 'Confirm',
        cancelText: 'Cancel',
        width: 1000,
        confirmLoading: false,
        handleCancel: () => {},
        handleOk: () => {}
      },
      config: {},
      tableData: [],
      params: {},
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      detailOptions: [
        { label: 'Name', key: 'name' },
        { label: 'Version', key: 'version' },
        // { label: 'Owner', key: 'owner' },
        { label: 'Entry Class Name', key: 'entry_class_name' },
        { label: 'Program Package File Id', key: 'program_package_file_id' },
        { label: 'Program Package File Name', key: 'program_package_file_name' },
        { label: 'Program Package File Path', key: 'program_package_file_path' },
        { label: 'Description', key: 'description' }
      ],
      columns: [
        {
          title: 'No.',
          key: 'serial',
          dataIndex: 'serial',
          scopedSlots: { customRender: 'serial' }
        },
        {
          title: 'Name',
          key: 'name',
          dataIndex: 'name'
        },
        {
          title: 'Version',
          key: 'version',
          dataIndex: 'version'
        },
        {
          title: 'Is Plugin',
          key: 'plugin',
          dataIndex: 'plugin',
          scopedSlots: { customRender: 'plugin' }
        }
      ],
      queryLoading: false,
      activeInstanceId: null,
      processor: null
    }
  },
  computed: {
    detailTitle () {
      if (!this.processor) return ''
      return `Below is the details of selected processor (${this.processor.name})`
    }
  },
  created () {
    this.config = Object.assign(this.baseConfig, this.modelConfig)
    this.loadTableData()
  },
  methods: {
    handleOk (e) {
      this.$emit('input', false)
      this.$emit('selected', this.processor)
      this.config.handleOk()
    },
    handleCancel (e) {
      this.$emit('input', false)
      this.config.handleCancel()
    },
    handleTableChange (pagination) {
      this.pagination.current = pagination.current
      this.loadTableData()
    },
    async loadTableData () {
      this.queryLoading = true
      const resp = await this.$api
        .getProcessorList(this.type.toUpperCase(), {
          page: this.pagination.current - 1,
          size: this.pagination.pageSize,
          ...this.params
        })
        .finally(() => (this.queryLoading = false))
      if (!resp || !resp.object) return
      this.tableData = resp.object.content
      this.pagination.total = resp.object.total
      if (this.tableData.length && !this.processor) {
        this.clickRow(this.tableData[0])
      }
    },
    async loadProcessorDetail (processorUid) {
      this.processor = null
      const resp = await this.$api.getProcessorDetail(processorUid)
      if (resp && resp.object) {
        this.processor = resp.object
        this.$emit('selecting', this.processor)
      }
    },
    reset () {
      this.params = {}
      this.loadTableData()
    },
    clickRow (record) {
      this.activeInstanceId = record.uid
      this.loadProcessorDetail(record.uid)
    },
    rowClassName (record) {
      if (this.activeInstanceId && this.activeInstanceId === record.uid) {
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
.processor-detail {
  margin: 0 auto;
}
/deep/ .active-row {
  background: #bae7ff;
  cursor: default;
}
/deep/ .ant-table-row {
  cursor: pointer;
}
</style>
