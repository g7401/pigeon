<template>
  <div class="task-instance-info">
    <KeyValueInfo
      v-if="taskInstance && type !== 'eb'"
      :config="{title: 'Task Process Information', bordered: true}"
      :options="detailOptions"
      :obj="taskInstance"
    >
    </KeyValueInfo>
    <KeyValueInfo
      v-if="taskInstance && type === 'eb'"
      :config="{title: 'Task Process Information', none: true}"
    >
    </KeyValueInfo>
    <template v-if="taskInstance && type === 'eb'">
      <div class="ant-descriptions-title mg-t-20">Task Input Information</div>
      <p>Triggered by Data Source</p>
      <KeyValueInfo
        class="mg-l-20"
        :config="{bordered: true}"
        :options="taskInputOptions"
        :obj="taskInstance"
      >
      </KeyValueInfo>
    </template>
    <KeyValueInfo
      class="mg-t-20"
      v-if="taskInstance && type === 'connect'"
      :config="{title: 'Task Output Information', bordered: true}"
      :options="connectOptions"
      :obj="taskInstance"
    >
    </KeyValueInfo>
    <template v-if="type === 'extract' && taskInstance && taskInstance.output && taskInstance.output !== 'null'">
      <h3 class="ant-descriptions-title">Task Output Information</h3>
      <a-table
        class="more-small-table"
        size="small"
        bordered
        :columns="taskOutputColumns"
        :dataSource="JSON.parse(taskInstance.output).items || []"
        :pagination="false"
        rowKey="fileName"
      >
        <template
          slot="fileSize"
          slot-scope="text"
        >
          {{ fileSize(text) }}
        </template>
        <template
          slot="dataFileLink"
          slot-scope="text"
        >
          <a
            :href="link(text)"
            target="_blank"
          >
            <Ellipsis
              :length="120"
              tooltip
            >{{ link(text) }}</Ellipsis>
          </a>
        </template>
      </a-table>
    </template>
    <template v-if="type === 'publish' && taskInstance && taskInstance.sds_df_pubs">
      <h3 class="ant-descriptions-title">Task Output Information</h3>
      <a-table
        class="more-small-table"
        size="small"
        bordered
        :columns="publishOutputTableColumns"
        :dataSource="taskInstance.sds_df_pubs || []"
        :pagination="false"
        rowKey="uid"
      >
        <template
          slot="serial"
          slot-scope="text, record, i"
        >
          {{ i + 1 }}
        </template>
      </a-table>
    </template>
    <KeyValueInfo
      class="mg-t-20"
      v-if="taskInstance && type === 'eb'"
      :config="{title: 'Task Output Information', none: true}"
    >
    </KeyValueInfo>
    <template v-if="taskInstance && taskInstance.output_tables">
      <h3 class="ant-descriptions-title">Task Output Information</h3>
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
            :taskInstanceId="taskInstance.task_inst_uid"
            :apiName="apiName"
          ></TaskInstanceOutputTable>
        </a-tab-pane>
      </a-tabs>
    </template>
  </div>
</template>

<script>
import KeyValueInfo from '@/components/common/KeyValueInfo'
import TaskInstanceOutputTable from '@/components/DIDefinitions/TaskInstanceOutputTable'
import Ellipsis from '@/components/Ellipsis'
import { fileSize, link } from '@/utils/format'
export default {
  components: { KeyValueInfo, TaskInstanceOutputTable, Ellipsis },
  props: {
    type: {
      type: String,
      default () {
        return 'extract'
      }
    },
    taskInstance: {
      type: Object,
      default () {
        return {}
      }
    },
    apiName: {
      type: String,
      default: 'getQaqOutTableData'
    }
  },
  data () {
    return {
      activeTab: '0',
      detailOptions: [
        {
          key: 'task_inst_uid',
          label: 'Task Instance ID'
        },
        {
          key: 'task_created_by',
          label: 'Task Created By'
        },
        {
          key: 'task_executed_by',
          label: 'Task Executed By'
        },
        {
          key: 'task_inst_status',
          label: 'Task Instance Status'
        },
        {
          key: 'task_created_time',
          label: 'Task Created Time'
        },
        {
          key: 'task_started_time',
          label: 'Task Started Time'
        },
        {
          key: 'task_done_time',
          label: 'Task Done Time'
        },
        {
          key: 'task_duration',
          label: 'Task Duration',
          type: 'time'
        },
        {
          key: 'task_failed_time',
          label: 'Task Failed Time'
        },
        {
          key: 'task_failed_reason',
          label: 'Task Failure Reason'
        }
      ],
      // taskOutputOptions: [
      //   {
      //     key: 'fileName',
      //     label: 'Data File Name'
      //   },
      //   {
      //     key: 'fileSize',
      //     label: 'Data File Size',
      //     type: 'fileSize'
      //   },
      //   {
      //     key: 'dataFileLink',
      //     label: 'Data File Link',
      //     isLink: true
      //   }
      // ],
      taskOutputColumns: [
        {
          key: 'fileName',
          dataIndex: 'fileName',
          title: 'Data File Name'
        },
        {
          key: 'fileSize',
          dataIndex: 'fileSize',
          title: 'Data File Size',
          scopedSlots: { customRender: 'fileSize' }
        },
        {
          key: 'dataFileLink',
          dataIndex: 'dataFileLink',
          title: 'Data File Link',
          scopedSlots: { customRender: 'dataFileLink' }
        }
      ],
      taskInputOptions: [
        {
          key: 'ds_uid',
          label: 'Data Source UID'
        },
        {
          key: 'ds_name',
          label: 'Data Source Name'
        },
        {
          key: 'df_uid',
          label: 'Data Fragment UID'
        },
        {
          key: 'create_time',
          label: 'Data Fragment Create Time'
        }
      ],
      connectOptions: [
        {
          key: 'required_connection',
          label: 'Need Connection',
          type: 'bool'
        },
        {
          key: 'connected',
          label: 'Connected',
          type: 'bool'
        }
      ],
      publishOutputTableColumns: [
        {
          dataIndex: 'serial',
          title: 'No.',
          scopedSlots: { customRender: 'serial' }
        },
        {
          dataIndex: 'daq_process_inst_uid',
          title: 'SDS DF Pub UID'
        },
        {
          dataIndex: 'ds_uid',
          title: 'DS UID'
        },
        {
          dataIndex: 'sds_uid',
          title: 'SDS UID'
        },
        {
          dataIndex: 'sds_name',
          title: 'SDS Name'
        },
        {
          dataIndex: 'rows',
          title: 'Rows'
        }
      ]
    }
  },
  methods: {
    fileSize,
    link
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
