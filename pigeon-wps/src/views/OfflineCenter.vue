<template>
  <a-tabs v-model="activeTab">
    <a-tab-pane
      key="export"
      tab="导出任务"
    >
      <BaseVxeGrid
        ref="export"
        queryApiName="queryExportJobs"
        :columns="columns"
        :toolbarButtonList="toolbarButtonList"
        :operationButtonList="operationButtonList"
        @onOperationButtonClick="handleOperation"
        @onToolbarButtonClick="handleToolbar"
      >
        <template #reason="{ row }">
          <a-button size="small" v-if="row.status === 'FAILED'" :loading="row.loading" @click="loadJobFaildReson(row)">详情</a-button>
        </template>
      </BaseVxeGrid>
    </a-tab-pane>
  </a-tabs>
</template>

<script>
import BaseVxeGrid from '@/components/common/BaseVxeGrid'

export default {
  name: 'OfflineCenter',
  components: {
    BaseVxeGrid
  },
  data () {
    return {
      activeTab: 'export',
      columns: [{
        title: '任务名称',
        field: 'name',
        width: 150
      }, {
        title: '状态',
        field: 'status',
        width: 80
      }, {
        title: '文件名',
        field: 'file_name',
        width: 250
      }, {
        title: '行数',
        field: 'rows',
        width: 70
      }, {
        title: '文件大小',
        field: 'formatted_file_length',
        width: 70
      }, {
        title: '总记录数',
        field: 'total',
        width: 70
      }, {
        title: '文件生成进度',
        field: 'progress_percentage',
        width: 90
      }, {
        title: '创建时间',
        field: 'create_timestamp',
        width: 150
      }, {
        title: '开始时间',
        field: 'start_timestamp',
        width: 150
      }, {
        title: '完成时间',
        field: 'done_timestamp',
        width: 150
      }, {
        title: '失败时间',
        field: 'failed_timestamp',
        width: 150
      }, {
        title: '失败原因',
        field: 'reason',
        width: 100,
        align: 'center'
      }],
      toolbarButtonList: [
        {
          label: '刷新',
          type: 'primary',
          key: 'refresh'
        }
      ],
      operationButtonList: [
        {
          label: '取消',
          type: 'primary',
          key: 'cancelExportJob',
          width: 70,
          show: (item) => item.status === 'WAITING'
        },
        {
          label: '下载',
          type: 'primary',
          key: 'downloadExportJob',
          width: 70,
          show: (item) => item.shared_file_id
        },
        {
          label: '删除',
          type: 'danger',
          key: 'deleteExportJob',
          confirm: true,
          width: 70,
          show: (item) => ['FAILED', 'CANCELED'].includes(item.status)
        }
      ]
    }
  },
  methods: {
    async loadJobFaildReson (row) {
      this.$set(row, 'loading', true)
      const result = await this.$api.getJobFaildReson(row.uid, 'async_export_job').finally(() => {
        row.loading = false
      })
      this.$info({
        title: '失败原因',
        content: result?.content,
        okText: '关闭'
      })
    },
    async downloadExportJob (item) {
      window.location.href = `${this.$api.config.download}?file_id=${item.shared_file_id}`
    },
    async cancelExportJob (item) {
      await this.$api.cancelAsyncExportJob(item.uid)
      this.$refs.export.loadTableData()
    },
    async deleteExportJob (item) {
      await this.$api.deleteAsyncExportJob(item.uid)
      this.$refs.export.loadTableData()
    },
    handleOperation (item, operationName) {
      this[operationName](item)
    },
    handleToolbar () {
      this.$refs.export.loadTableData()
    }
  }
}
</script>
