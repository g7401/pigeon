<template>
  <div class="task-instance-info">
    <KeyValueInfo
      v-if="trainingDetail"
      :config="{title: 'Basic Information', bordered: true}"
      :options="basicOptions"
      :obj="trainingDetail"
    >
      <template #task_instance_executed_by="{ val }">
        <template v-if="val">
          {{ val.processor_id }}, {{ val.name }}, {{ val.version }}
        </template>
      </template>
      <template #task_instance_status="{ val }">
        {{ stateMap[val] }}
      </template>
    </KeyValueInfo>
    <KeyValueInfo
      style="margin-top: 20px"
      v-if="trainingDetail"
      :config="{title: 'Advanced Information', bordered: true}"
      :options="advancedOptionsMap[type]"
      :obj="trainingDetail"
    >
    </KeyValueInfo>
  </div>
</template>

<script>
import { modelStatusMap } from '@/assets/model/map'
import KeyValueInfo from '@/components/common/KeyValueInfo'

const trainOptions = [
  {
    key: 'model_instance_filename',
    label: 'Model File Name'
  },
  {
    key: 'model_instance_file_size',
    label: 'Model File Size',
    type: 'fileSize'
  },
  {
    key: 'model_instance_file_id',
    label: 'Model File Link',
    isLink: true
  },
  {
    key: 'model_accuracy_metrics_filename',
    label: 'Model Accuracy Metrics File Name'
  },
  {
    key: 'model_accuracy_metrics_file_size',
    label: 'Model Accuracy Metrics File Size',
    type: 'fileSize'
  },
  {
    key: 'model_accuracy_metrics_file_id',
    label: 'Model Accuracy Metrics File Link',
    isLink: true
  }
]
const commitOptions = [
  {
    key: 'model_instance_id',
    label: 'Model Instance Id'
  },
  ...trainOptions
]

const prepareOptions = [
  {
    key: 'model_conf_filename',
    label: 'Model Configuration File Name'
  },
  {
    key: 'model_conf_file_size',
    label: 'Model Configuration File Size',
    type: 'fileSize'
  },
  {
    key: 'model_conf_file_id',
    label: 'Model Configuration File Link',
    isLink: true
  },
  {
    key: 'model_feature_store_reference',
    label: 'Feature Repository Object'
  },
  {
    key: 'model_data_set_filters',
    label: 'Dataset Filters',
    type: 'object'
  },
  {
    key: 'model_data_set_filename',
    label: 'Dataset File Name'
  },
  {
    key: 'model_data_set_file_size',
    label: 'Dataset File Size',
    type: 'fileSize'
  },
  {
    key: 'model_data_set_file_id',
    label: 'Dataset File Link',
    isLink: true
  }
]

const predictAndCommitOptions = [
  {
    key: 'model_instance_id',
    label: 'Model Instance Id'
  },
  {
    key: 'model_instance_filename',
    label: 'Model File Name'
  },
  {
    key: 'model_instance_file_size',
    label: 'Model File Size',
    type: 'fileSize'
  },
  {
    key: 'model_instance_file_id',
    label: 'Model File Link',
    isLink: true
  },
  {
    key: 'predict_result_filename',
    label: 'Prediction Result File Name'
  },
  {
    key: 'predict_result_file_size',
    label: 'Prediction Result File Size',
    type: 'fileSize'
  },
  {
    key: 'predict_result_file_id',
    label: 'Prediction Result File Link',
    isLink: true
  },
  {
    key: 'model_accuracy_metrics_filename',
    label: 'Prediction Result Accuracy Metrics File Name'
  },
  {
    key: 'model_accuracy_metrics_file_size',
    label: 'Prediction Result Accuracy Metrics File Size',
    type: 'fileSize'
  },
  {
    key: 'model_accuracy_metrics_file_id',
    label: 'Prediction Result Accuracy Metrics File Link',
    isLink: true
  }
]

export default {
  components: { KeyValueInfo },
  props: {
    type: {
      type: String,
      default () {
        return 'import'
      }
    },
    trainingDetail: {
      type: Object,
      default () {
        return null
      }
    }
  },
  data () {
    return {
      activeTab: '0',
      stateMap: modelStatusMap,
      basicOptions: [
        {
          key: 'task_instance_id',
          label: 'Task Instance ID'
        },
        {
          key: 'task_instance_status',
          label: 'Task Instance Status'
        },
        {
          key: 'task_instance_create_time',
          label: 'Task Created Time'
        },
        {
          key: 'task_instance_start_time',
          label: 'Task Started Time'
        },
        {
          key: 'task_instance_done_time',
          label: 'Task Done Time'
        },
        {
          key: 'task_instance_duration',
          label: 'Task Duration',
          type: 'time'
        },
        {
          key: 'task_instance_failed_time',
          label: 'Task Failed Time'
        },
        {
          key: 'task_instance_failed_reason',
          label: 'Task Failure Reason'
        }
      ],
      advancedOptionsMap: {
        'Training Prepare Dataset': prepareOptions,
        'Training Train': trainOptions,
        'Training Commit': commitOptions,
        'Serving Prepare Dataset': prepareOptions,
        'Serving Predict': predictAndCommitOptions,
        'Serving Commit': predictAndCommitOptions
      }
    }
  }
}
</script>

<style lang="less" scoped>
.ant-descriptions-title {
  margin-top: 30px;
}
/deep/ .ant-descriptions-item-label.ant-descriptions-item-colon {
  width: 280px;
}
</style>
