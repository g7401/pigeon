<template>
  <div class="task-instance-info">
    <KeyValueInfo
      v-if="taskInstance"
      :config="{title: 'Task Information', bordered: true}"
      :options="detailOptions"
      :obj="taskInstance.basic"
    >
      <template #di_task_instance_executed_by="{ val }">
        <template v-if="val">
          {{ val.di_processor_id }}, {{ val.name }}, {{ val.version }}
        </template>
      </template>
    </KeyValueInfo>
    <KeyValueInfo
      style="margin-top: 20px"
      v-if="taskInstance && type === 'import'"
      :config="{title: 'Task Output', bordered: true}"
      :options="taskOutputOptions"
      :obj="taskInstance"
    >
    </KeyValueInfo>
    <template v-if="taskInstance && taskInstance.output_tables">
      <h3 class="ant-descriptions-title">Task Output</h3>
      <a-tabs
        type="card"
        v-model="activeTab"
      >
        <a-tab-pane
          v-for="(table, index) in taskInstance.output_tables"
          :key="'' + index"
          :tab="table.table_name"
        >
          <TaskInstanceOutputTable
            :key="table.table_name"
            :table="table"
            :taskInstanceId="taskInstance.basic.di_task_instance_id"
          ></TaskInstanceOutputTable>
        </a-tab-pane>
      </a-tabs>
    </template>
  </div>
</template>

<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'
import TaskInstanceOutputTable from '@/components/DIDefinitions/TaskInstanceOutputTable'
export default {
  components: { KeyValueInfo, TaskInstanceOutputTable },
  props: {
    type: {
      type: String,
      default () {
        return 'import'
      }
    },
    taskInstance: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  data () {
    return {
      activeTab: '0',
      detailOptions: [
        {
          key: 'di_task_instance_id',
          label: 'Task Instance ID'
        },
        {
          key: 'di_task_instance_created_by',
          label: 'Task Created By'
        },
        {
          key: 'di_task_instance_executed_by',
          label: 'Task Executed By'
        },
        {
          key: 'di_task_instance_status',
          label: 'Task Instance Status'
        },
        {
          key: 'di_task_instance_create_time',
          label: 'Task Created Time'
        },
        {
          key: 'di_task_instance_started_time',
          label: 'Task Started Time'
        },
        {
          key: 'di_task_instance_done_time',
          label: 'Task Done Time'
        },
        {
          key: 'di_task_instance_duration_in_millis',
          label: 'Task Duration',
          type: 'time'
        },
        {
          key: 'di_task_instance_failed_time',
          label: 'Task Failed Time'
        },
        {
          key: 'di_task_instance_failure_reason',
          label: 'Task Failure Reason'
        }
      ],
      taskOutputOptions: [
        {
          key: 'data_file_name',
          label: 'Data File Name'
        },
        {
          key: 'data_file_size_in_bytes',
          label: 'Data File Size',
          type: 'fileSize'
        },
        {
          key: 'data_file_id',
          label: 'Data File Link',
          isLink: true
        }
      ]
    }
  }
}
</script>

<style lang="less" scoped>
.ant-descriptions-title {
  margin-top: 30px;
}
/deep/ .ant-descriptions-item-label {
  width: 300px;
}
</style>
